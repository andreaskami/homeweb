package controlLayer.eventing;

import java.io.IOException;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.StringRepresentation;

import controlLayer.Core;

/**
 * container to wrap a registration of a client. the registration holds the 
 * callback, the leasetime.<br/>
 * as soon as the registration is posted, a timer gets started. upon elapsed 
 * leasetime, the registration gets removed from the registrations.
 * @author sawielan
 *
 */
public class Registration extends Hashable implements Runnable {
	
	/** the worker thread used to measure the lease time. */
	private Thread thread = null;
	
	/** the helper that manages the actual work on the lease time. */
	private LeaseTimer timer = null;
	
	/** how long this registration is valid. */
	private long leaseTime;
	
	/** handle to the parent container where this registration is stored. */
	private final Registrations parent;
	
	/** helper to contact a server. */
	private Client client = null;
	
	/** the events to be dispatched. */
	private Queue<Event> events = new ConcurrentLinkedQueue<Event> ();
	
	/** if set to false then stop execution. */
	private boolean doRun = true;
	
	/** how many events to queue at maximum until congestion drop. */
	public static final int MAX_QUEUE_LEN = 100;
	
	/**
	 * constructor to setup a new client registration.
	 * @param parent the parent that maintains this client registration.
	 * @param leaseTime the leasetime that this registration is valid.
	 * @param callback the callback where to send the events to.
	 */
	public Registration(Registrations parent, long leaseTime, String callback) {
		super(parent.getKeyWord(), callback);
		
		this.leaseTime = leaseTime;
		this.parent = parent;
		Core.getInstance().submitToThreadPool(this);
		System.err.println("created new registration.");
	}
	
	/**
	 * notify the client that posted this registration about a new event.
	 * @param event the event to be dispatched to the client.
	 */
	public void notifyEvent(Event event) {
		synchronized (events) {
			if (events.size() > MAX_QUEUE_LEN) {
				// drop event
				System.err.println("drop event");
				return;
			}
			events.add(event);
			events.notifyAll();
			System.err.println("event added.");
		}	
	}
		
	/**
	 * start the timer thread that will remove the registration after some 
	 * time.
	 */
	public synchronized void startTimer() {
		if (thread != null) {
			synchronized (thread) {
				// do not remove the event
				timer.setMode(false);
				// kill the thread.
				thread.interrupt();
			}
		}
		
		timer = new LeaseTimer();
		thread = new Thread(timer);
		synchronized (thread) {
			thread.start();
		}
	}
	
	/**
	 * set a new leasetime for this event registration. attention: this also 
	 * restarts the timer thread.
	 * @param leaseTime the new leasetime.
	 */
	public void renewLeaseTime(long leaseTime) {
		System.err.println(String.format("refreshed lease time to %d.", leaseTime));
		this.leaseTime = leaseTime;
		startTimer();
	}
	
	/**
	 * deregister this registration from the parent container.
	 */
	private void deregister() {
		doRun = false;
		parent.deregister(this);
	}
		
	/**
	 * the lease timer just waits for lease time milliseconds and then quits.
	 * if the move is set to true, then the registration gets removed from 
	 * the registrations. if set to false not.
	 * @author sawielan
	 *
	 */
	protected class LeaseTimer implements Runnable {

		// flag whether the registration shall be removed after timeout.
		boolean remove = true;
				
		/**
		 * flag if the registration shall be deleted upon timeout.
		 * @param remove if true remove registration, false leave registration.
		 */
		public void setMode(boolean remove) {
			this.remove = remove;
		}
		
		/**
		 * execute the worker thread.
		 */
		public void run() {
			try {
				synchronized (thread) {
					thread.wait(leaseTime);
				}
			} catch (InterruptedException e) {
			}
			
			if (remove) {
				synchronized (thread) {
					thread = null;
				}
				timer = null;
				
				deregister();
				System.err.println("removed registration");
			} else {
				System.err.println("leave registration");
			}
		}
		
	}
	
	/**
	 * dispatch requests multithreaded...
	 */
	private void dispatch() {
		while (doRun) {
			try {
				Event event = null;
				synchronized (events) {
					while (events.size() == 0) {
						events.wait();
					}
					event = events.remove();						
				}
				// create the json object.
				JSONObject json = Event.encodeToJSON(event);
				
				URL url = new URL(callback);
				String target = url.toString();
				Response response = client.post(
						target, 
						new StringRepresentation(json.toString())
						);
				
				if (response.getStatus().getCode() != Status.SUCCESS_OK.getCode()) {
					System.err.println(String.format(
							"could not deliver event to URL %s\n %s",
							url.toString(),
							response.getStatus().getDescription()));
				}
				System.err.println("event sent.");
				
			} catch (IOException e) {
				System.err.println(e.getMessage());
			} catch (JSONException e) {
				System.err.println(e.getMessage());
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
	}
	
	public void run() {
		client = parent.getEventing().getClient();
		
		// create additional dispatchers.
		for (int i=0; i<MAX_QUEUE_LEN / 10; i++) {
			Runnable r = new Runnable() {

				public void run() {
					dispatch();
				}
				
			};
			Core.getInstance().submitToThreadPool(r);
		}
		dispatch();
	}
}
