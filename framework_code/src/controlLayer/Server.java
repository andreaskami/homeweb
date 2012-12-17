package controlLayer;

import java.net.URL;

import org.restlet.Component;
import org.restlet.data.Protocol;

import presentationLayer.rest.RESTApplication;

/**
 * the server class maintains all the HTTP server stuff. aside that it contains 
 * the main class for the whole gateway architecture.
 *
 */
public class Server implements Runnable {
	
	/** the HTTP port for this gateway. */
	private int httpPort = -1;
	
	/** the component that provides the REST server. */
	private Component restServer;
	
	/** the rest application that is responsible for the restlets. */
	private RESTApplication restApplication;
	
	/** the address of this gateway. */
	private String localHostAddress = null;

	
	/**
	 * constructor.
	 */
	public Server(String address, int port) {
		this.httpPort = port;
		this.localHostAddress = address;
	}
	
	/**
	 * creates a HTTP URL for this gateway with the form:
	 * http://IP:PORT.
	 * @return a HTTP URL for this gateway with the form: http://IP:PORT.
	 */
	public URL getHostURI() {
		URL url = null;
		try {
			url = new URL(
					"http://" + localHostAddress + ":" + getHTTPPort()
					);
		} catch (Exception e) { 
			return null;
		}
		
		return url;
	}
	
	/**
	 * returns the HTTP port for this gateway.
	 * @return the HTTP port for this gateway.
	 */
	public int getHTTPPort() {
		return httpPort;
	}
	
	/**
	 * @return the IP address of this gateway.
	 */
	public String getLocalAddress() {
		return localHostAddress;
	}

	/**
	 * helper method to stop the server held by the core.
	 */
	public void stopRESTServer() {
		try {
			//log.debug("stopping REST server...");
			restServer.stop();
			//log.debug("stopped REST server.");
		} catch (Exception e) {
			//log.error("could not stop REST server!");
			//log.debug(e.getMessage());
		}
		restServer = null;
	}
	
	/**
	 * helper method to start the server held by the core.
	 */
	public void startRESTServer() {
		restServer = new Component();
		restServer.getServers().add(Protocol.HTTP, getHTTPPort());

		restServer.getLogger().setUseParentHandlers(false);
		
		restApplication = new RESTApplication(restServer.getContext().createChildContext());
		
		// attach to the default host.
		restServer.getDefaultHost().attach(restApplication);
		
		
	   // Start the component.
        try {
        	System.err.println("starting REST server...");
			restServer.start();
			System.err.println("started REST server.");
		} catch (Exception e) {
			System.err.println("could not start REST server!");
			System.err.println(e.getMessage());
			Core.getInstance().shutdown();
		}
	}
	
	/**
	 * returns a handle to the REST server component.
	 * @return the server component responsible for the whole REST part.
	 */
	public Component getRestServer() {
		return restServer;
	}
	
	/**
	 * returns a handle to the application that is responsible for the restlets.
	 * @return a rest application.
	 */
	public RESTApplication getRestApplication() {
		return restApplication;
	}
	
	/**
	 * run in a loop until stopped by SIGINT/SIGHUP.
	 */
	public void run() {

		boolean doRun = true;
		while (doRun) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//log.info("good bye.");
	}
	
}
