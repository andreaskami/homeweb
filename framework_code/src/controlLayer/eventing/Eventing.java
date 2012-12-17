package controlLayer.eventing;

import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.data.Status;

import controlLayer.Core;
import controlLayer.libraryCode.Constants;
import deviceLayer.Device;
import deviceLayer.Devices;
import deviceLayer.GatewayDevice;

/**
 * eventing plugin that allows the registration for device/gateway events.
 * @author sawielan
 *
 */
public class Eventing extends java.util.Observable implements java.util.Observer {

	/** helper client to connect to other gateways. */
	protected Client client = null;
	
	/** a queue holding the event descriptors. */
	protected Queue<EventDescriptor> queue = 
		new ConcurrentLinkedQueue<EventDescriptor> ();
	
	/** 
	 * the event dispatcher that is responsible to receive and dispatch events.
	 */
	private EventMgmt eventMgmt = new EventMgmt(this);
	
	/** a list of all the event registrations currently on the gateway. */
	protected Map<String, Registrations> registrations = 
		new ConcurrentHashMap<String, Registrations> (); 

	/**
	 * constructor for the eventing plugin.
	 */
	public Eventing() {

		// start the worker thread that processes the request queue.
		Core.getInstance().submitToThreadPool(new QueueProcessor(this));
	}

	/**
	 * put a new registration.
	 * @param e discriptor of the event type the registration is for.
	 */
	public void register(EventDescriptor e) {
		synchronized (queue) {
			queue.add(e);
			queue.notifyAll();
		}
	}
	
	/**
	 * the queue processor waits on the request queue for new registrations 
	 * to be postet. if a new post arrives this post gets removed from the 
	 * request queue and gets processed.
	 * @author sawielan
	 *
	 */
	protected class QueueProcessor implements Runnable {

		protected Eventing eventing = null;
		
		public QueueProcessor(Eventing eventing) {
			this.eventing = eventing;
		}
		
		public void run() {
			
			while (true) {
				
				EventDescriptor e = null;
					// wait for a new event to be posted to the request queue.
				while (queue.size() == 0) {
					synchronized (queue) {
						try {
							queue.wait();
						} catch (InterruptedException e1) {
							// log the error...
							System.err.println(e1.toString());
						}
					}
				}
				synchronized (queue) {
					e = queue.remove();
					//log.debug("queue size: " + queue.size());
				}
				
				Registrations r = registrations.get(e.getKeyword()); 
				if (r == null) {
					r = new Registrations(
							eventing, 
							e.getKeyword()
							);
					registrations.put(e.getKeyword(), r);					
				}
				r.register(e);
				
				URL url = Core.getInstance().getServer().getHostURI();
				String callback = url.getProtocol() + "://" + url.getHost();
				callback += ":" + url.getPort() + Constants.EVENTING_SUBMIT_EVENT;
				// create a new form as we need to change the callback.
				Form form = new Form();
				form.add("leasetime", String.format("%d", e.getLeaseTime()));
				form.add("callback", callback);
				form.add("keyword", e.getKeyword());
				
				System.err.println("Sending registration request for "+ e.getKeyword() + " to available Gateways");
				
				Collection<Device> devs  = Core.getInstance().getDevices().getAll().values();
				for(Device dw : devs) {
					// if device is local then handle it differently from remote host.
					
					if (dw instanceof GatewayDevice) {
						// only add if remote host is not the one that 
						// started registration
						GatewayDevice gw = (GatewayDevice) dw;
						if (!e.getCallback().trim().
								equalsIgnoreCase(
										gw.getHost().toString().trim())) {
							System.err.println("handle gateway candidate: " 
									+ gw.getDeviceName());
							
							System.err.println("calling host: " + gw.getHost());
							
							Response response = getClient().post(
									gw.getHost() + 
										Constants.EVENTING_REGISTRATION,
									form.getWebRepresentation()
									);
						
							if (response.getStatus().getCode() 
									!= Status.SUCCESS_OK.getCode()) {
								System.err.println("could not register event...");
								
							}
						}
					} 
				}
			}			
		}
		
	}
	
	public Client getClient() {
		if (null == client) {
			client = new Client(
					Core.getInstance().getServer().getRestApplication()
						.getContext().createChildContext(), 
						Protocol.HTTP
					);
		}
		
		return client;
	}
	
	/**
	 * deregisters a registration.
	 * @param r the registration to be removed.
	 */
	public void deregister(Registrations r) {
		System.err.println("removed registrations for keyword: " + r.getKeyWord());
		registrations.remove(r);
	}
	
	/**
	 * tests whether a registration is present for the specified keyword.
	 * @param keyword the keyword to look for.
	 * @return true if registration present, false otherwise.
	 */
	public boolean contains(String keyword) {
		return registrations.containsKey(keyword);
	}
	
	/**
	 * tests whether a registration is present for a list of specified keywords.
	 * @param keywords the list of keywords to test.
	 * @return a list of keywords that are contained.
	 */
	public List<String> contains(List<String> keywords) {
		List<String> cont = new LinkedList<String> ();
		for (String kwrd : keywords) {
			if (contains(kwrd)) {
				cont.add(kwrd);
			}
		}
		return cont;
	}
	
	/**
	 * notify the registrations about a new event.
	 * @param event the event that arrived.
	 */
	public void notifyEvent(Event event) {
		try {
			Registrations r = registrations.get(event.getKeyword());
			if (null != r) {
				r.notifyEvent(event);
			}
			
			// notify all the events if someone registered in "*"
			if (contains("*")) {
				registrations.get("*").notifyEvent(event);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// notify about the new event
		setChanged();
		notifyObservers(event);
	}

	/**
	 * update method that will be called by plugins this plugin in dependent on.
	 * @param arg0 the observable that triggered the update.
	 * @param arg1 the object that changed.
	 */
	public void update(Observable arg0, Object arg1) {
		if ((arg1 == null) || (arg0 == null)) {
			return;
		}

		System.err.println("received update from plugin.");
		if ((arg0 instanceof Devices) && (arg1 instanceof Device)) {
			Device device = (Device) arg1;
			eventMgmt.register(device);
		}
	}
	
}
