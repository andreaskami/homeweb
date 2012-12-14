package controlLayer.eventing;

import java.util.Observer;

import org.restlet.Component;
import org.restlet.data.Protocol;

import controlLayer.Core;

/**
 * Helper that allows to receive asynchronous events. the receiver tries to 
 * bind to a specific port you provide. however if this port is occupied, 
 * retries with randomized port numbers until a free port is found or the 
 * specified retry-count is reached. as a user you should wait for the receiver 
 * to finish the setup such that you can receive the port number (call the 
 * method <code>waitAndGetPort</code>).<br/><br/>
 * Code example how to use the event receiver:<br/>
 * <code>AsynchronousEventReceiver receiver = </code><br/> 
 * <code>&nbsp;&nbsp;&nbsp;new AsynchronousEventReceiver(port);</code><br/>
 * <code>new Thread(receiver).start();</code><br/>
 * <code>int port = receiver.waitAndGetPort(this);</code><br/>
 * <code>if (port == AsynchronousEventReceiver.ERROR_PORT) {</code><br/>
 * <code>&nbsp;&nbsp;&nbsp;throw new Exception("no free port found!");</code><br/>
 * <code>}</code>
 * @author sawielan
 *
 */
public class AsynchronousEventReceiver extends AbstractEventReceiver {

	/** how many times a random port is searched... */
	public static int PORT_SEARCH_RETRY_TIMES = 10;
	
	/** default port selected when no valid port was found. */
	public static int ERROR_PORT = -1;
	
	/** the port where the event receiver listens for events. */
	private int port = -1;
	
	/** how many times to retry finding another port. */
	private int numRetry = PORT_SEARCH_RETRY_TIMES;
				
	/**
	 * constructor to generate an event receiver.
	 * @param desiredPort the port where the receiver shall listen. please 
	 * notice that this port is just a hint which port to select. if the port 
	 * is already selected, the event receiver will look out for a free port. 
	 * have a look at the javadoc of the whole class for an example how to use 
	 * the event receiver preferrably.
	 */
	public AsynchronousEventReceiver(int desiredPort) {
		this.port = desiredPort;
	}
	
	/**
	 * constructor to generate an event receiver.
	 * @param desiredPort the port where the receiver shall listen. please 
	 * notice that this port is just a hint which port to select. if the port 
	 * is already selected, the event receiver will look out for a free port. 
	 * have a look at the javadoc of the whole class for an example how to use 
	 * the event receiver preferrably.
	 * @param numRetry how many times to retry finding a free port.
	 */
	public AsynchronousEventReceiver(int desiredPort, int numRetry) {
		this.port = desiredPort;
		this.numRetry = numRetry;
	}
	
	/**
	 * execute the event receiver.
	 */
	public void run() {
		
		// for security wait for 3 seconds to allow the client to register.
		try {
			synchronized (this) {
				this.wait(3000);
			}
		} catch (InterruptedException e1) {
			System.err.println(e1.getMessage());
		}
		
		int i=0;
		server = null;
		while (i < numRetry) {
			server = startComponent();
			if (server != null) {
				break;
			}
			
			i++;
		}
		
		if (server == null) {
			// still no success...
			port = ERROR_PORT;
			synchronized(lock) {
				lock.notifyAll();
			}
			return;
		}
		
		keepRunning("asynchronous event receiver");
	}
	
	/**
	 * try to create a new server component and try to start it.
	 * @return a server component.
	 */
	private Component startComponent() {
		try {
			Component server = new Component();
			server.getServers().add(Protocol.HTTP, port);
			// attach the restlets...
			attach(server);
			
			server.start();
			return server;
		} catch (Exception e) {
			System.err.println(String.format("Port '%d' was occupied.",port));
			addRandomJitter(0, 10);
		}
		return null;
	}
	
	/**
	 * add a jitter value to the port within the given range [lower, upper].
	 * @param lower the lower bound.
	 * @param upper the upper bound.
	 */
	private void addRandomJitter(int lower, int upper) {
		port += Core.getInstance().getRandomGenerator().nextInt(upper) + lower;
		System.err.println("selecting new port: " + port);
	}
			
	/**
	 * wait for the event receiver to be started. you will then obtain the 
	 * port where the receiver listens to incoming events.
	 * @return the port where the receiver listens for events. if no port could 
	 * be found -1 is returned!
	 * @param observer the observer to be informed about incoming messages.
	 */
	public int waitAndGetPort(Observer observer) {
		addObserver(observer);
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return port;
	}
	
	/**
	 * @return the port of the event sink.
	 */
	public int getPort() {
		return port;
	}

}
