package simulation.TestingMultipleClients;

import java.io.BufferedWriter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Response;
import org.restlet.data.Status;

import controlLayer.libraryCode.Constants;

public class ClientRequest implements  Runnable{
	
	/** the client instance. */
	private Client client   = null;
	
	private String url      = null;
	
	private String RESTverb = null;
	
	private Form form     	= null;
	
	/** REST request constructor */
	public ClientRequest(Client client){
		this.client     = client;
	}
	
	public void setRequest(String url){
		this.url = url;
		this.RESTverb = Constants.VERB_GET;
	}
	
	public void setRequest(String url, Form form){
		this.url  = url;
		this.form = form;
		this.RESTverb = Constants.VERB_POST;
	}
	
	public void run() {
		
		Response response;
		
		// request is created in Presentation Layer
		if(RESTverb.equalsIgnoreCase(Constants.VERB_GET)){			// GET request
			response   = new Response(null);
			response   = client.get(url);
		}
		else if (RESTverb.equalsIgnoreCase(Constants.VERB_POST)){	// POST request
			response   = new Response(null);
			response    = client.post(url, form.getWebRepresentation());
		}
		else{
			System.err.println("MultiClientsScenario: Error: No REST verb specified for client's request. Thread is exiting...");
			return;
		}
    	
		// response has arrived from Device Layer
		if (response.getStatus().getCode() !=  Status.SUCCESS_OK.getCode()) {

			System.err.println("MultiClientsScenario Success: received answer from device.");
		}
	
	}

}
