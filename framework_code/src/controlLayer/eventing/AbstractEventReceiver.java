/**
 * 
 */
package controlLayer.eventing;

import java.io.IOException;
import java.util.Observable;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import controlLayer.libraryCode.Constants;

/**
 * abstract base for event receivers (event sink). two sample implementations 
 * are {@link AsynchronousEventReceiver} and 
 * {@link AsynchronousEventReceiverNPorts}.
 * @author sawielan
 *
 */
public abstract class AbstractEventReceiver extends Observable implements
		Runnable {

	
	/** lock for thread synchronization. */
	protected Integer lock = new Integer(1234);
		
	/** execute the event sink. */
	protected boolean doRun = true;
	
	/** the server component. */
	protected Component server = null;
	
	/**
	 * attach the receiver unit to the server.
	 * @param server the server component where to attach the receiver.
	 */
	protected void attach(Component server) {
		// create an application that will handle the restlet with the 
		// event receiver.
		Application app = new Application() {
			/**
		    * Creates a root Restlet that will receive all incoming calls.
			*/
			@Override
			public synchronized Restlet createRoot() {
				
				// Create a router Restlet that routes each call
				Router router = new Router(getContext());
				
				Restlet receiver = new Restlet() {
					public void handle(Request request, Response response) {
						Object obj;
						try {
							obj = request.getEntity().getText();
						} catch (IOException e) {
							response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
							System.err.println(e.getMessage());
							return;
						}
						if (obj instanceof String) {
							try {
								Event event = 
									Event.decodeFromJSON(
											new JSONObject(
													(String) obj
													)
											);
								setChanged();
								notifyObservers(event);
							} catch (JSONException e) {
								response.setStatus(
										Status.CLIENT_ERROR_BAD_REQUEST
								);
								System.err.println(e.getMessage());
								return;
							}
						}
					}
				};
				
				router.attach(Constants.EVENTING_SUBMIT_EVENT, receiver);

				return router;
			}
		};
		
		// attach to the default host.
		server.getDefaultHost().attach(app);
	}
	
	/**
	 * stop the event sink.
	 */
	public void stop() {
		System.err.println("call stop.");
		doRun = false;
		synchronized(lock) {
			lock.notifyAll();
		}
	}
	
	/**
	 * keeps the receiver running as long as not stop is requested.
	 * @param name an optional name for the sink. if null set to "event sink".
	 */
	protected void keepRunning(String name) {
		
		if (null == name) {
			name = "event sink.";
		}
		
		// now we can do something with the server...
		server.getLogger().setUseParentHandlers(false);
		synchronized(lock) {
			lock.notifyAll();
		}
		System.err.println(name + " started.");

		try {
			while (doRun) {
				synchronized (lock) {
					lock.wait();
				}

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (null != server) {
			try {
				System.err.println(String.format("stopping %s.", name));
				server.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.err.println(String.format("leaving %s.", name));
	}
}
