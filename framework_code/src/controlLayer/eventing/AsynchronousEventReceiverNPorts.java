package controlLayer.eventing;

import java.util.Observer;

import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 * Helper that allows to receive asynchronous events. the receiver tries to 
 * bind to all the specified ports you provide. Notice: if any of the ports is 
 * occupied, then it fails!.<br/><br/>
 * Code example how to use the n-port event receiver:<br/>
 * <code>int [] ports = new int[]{8080, 8181};
 * <code>AsynchronousEventReceiverNPorts receiver = </code><br/> 
 * <code>&nbsp;&nbsp;&nbsp;new AsynchronousEventReceiverNPorts(ports);</code><br/>
 * <code>new Thread(receiver).start();</code><br/>
 * <code>int f = receiver.waitFor(this);</code><br/>
 * <code>if (f == AsynchronousEventReceiver.ERROR_PORT) {</code><br/>
 * <code>&nbsp;&nbsp;&nbsp;throw new Exception("no free port found!");</code><br/>
 * <code>}</code>
 * @author sawielan
 *
 */
public class AsynchronousEventReceiverNPorts extends AbstractEventReceiver {
	
	/** the port where the event receiver listens for events. */
	private int [] ports = null;
	
	/**
	 * constructor to generate an event receiver.
	 * @param desiredPorts the ports where the receiver shall listen. 
	 */
	public AsynchronousEventReceiverNPorts(int [] desiredPorts) {
		this.ports = desiredPorts;
	}
	
	/**
	 * execute the event receiver.
	 */
	public void run() {
		
		// for security wait for 3 seconds to allow the client to register.
		try {
			synchronized (this) {
				this.wait(2000);
			}
		} catch (InterruptedException e1) {
			System.err.println(e1.getMessage());
		}
		
		server = startComponent();
		
		if (server == null) {
			// still no success...
			ports = null;
			synchronized(lock) {
				lock.notifyAll();
			}
			return;
		}
		
		keepRunning("n-port asynchronous event receiver");
	}
	
	/**
	 * try to create a new server component and try to start it.
	 * @return a server component.
	 */
	private Component startComponent() {
		try {
			Component server = new Component();
			for (int i=0; i<ports.length; i++) {
				System.err.println("try to bind to port: " + ports[i]);
				server.getServers().add(Protocol.HTTP, ports[i]);
			}
			// attach the restlets...
			attach(server);
			
			server.start();
			return server;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("could not start sink(s)");
		}
		return null;
	}
	
	/**
	 * wait for the event receiver to be started. you will then obtain the 
	 * port where the receiver listens to incoming events.
	 * @return the port where the receiver listens for events. if no port could 
	 * be found -1 is returned!
	 * @param observer the observer to be informed about incoming messages.
	 */
	public int waitFor(Observer observer) {
		addObserver(observer);
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (null == ports) {
			return AsynchronousEventReceiver.ERROR_PORT;
		}
		return 0;
	}
}
