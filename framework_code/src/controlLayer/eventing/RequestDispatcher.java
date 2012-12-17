package controlLayer.eventing;

import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * the request dispatcher takes a socket, retrieves an event on this socket, 
 * closes the socket and delivers the event to the eventing module.
 * @author sawielan
 *
 */
public class RequestDispatcher implements Runnable {

	/** handle to the eventing module. */
	private Eventing eventing = null;
	
	/** the socket where to receive the event from. */
	private Socket socket = null;
	
	/**
	 * constructor.
	 * @param eventing a handle to the eventing module.
	 */
	public RequestDispatcher(Eventing eventing, Socket socket) {
		this.socket = socket;
		this.eventing = eventing;
	}
	
	/**
	 * execute the thread.
	 */
	public void run() {
		try {
			ObjectInputStream in = new ObjectInputStream(
					socket.getInputStream());
			Object obj = in.readObject();
			if (obj instanceof Event) {
				eventing.notifyEvent((Event) obj);
			}
			socket.close();
		} catch (Exception e) {
			System.err.println("could not receive the event on the socket:\n" + 
					e.getMessage());
		}
	}

}
