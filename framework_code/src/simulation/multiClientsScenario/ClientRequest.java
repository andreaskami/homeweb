package simulation.multiClientsScenario;

import java.io.BufferedWriter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.data.Status;

import controlLayer.Server;
import controlLayer.libraryCode.Constants;

public class ClientRequest implements  Runnable{
	
	/** the client instance. */
	private Client client   = null;
	
	private String url      = null;
	
	private String RESTverb = null;
	
	private Form form     	= null;
	
	private BufferedWriter resultFile;
	
	/** REST request constructor */
	public ClientRequest(Server server, BufferedWriter outFile){
		this.client     = new Client(server.getRestApplication().getContext().createChildContext(), Protocol.HTTP);;
		this.resultFile = outFile;
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
		long startTime, endTime, reqResponseTime;
		startTime	 = System.currentTimeMillis(); 
		
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
		
		endTime	 	    = System.currentTimeMillis();
		reqResponseTime = endTime - startTime;
		
		MultiClientsScenario.requestCounter++;
    	
		// response has arrived from Device Layer
		if (response.getStatus().getCode() !=  Status.SUCCESS_OK.getCode()) {
		    Lock l = new ReentrantLock(); 
		    l.lock();
		        try {
					//write results in a file
				    try{  
				    	MultiClientsScenario.failedRequest++;
				    	//String resultInfo = "FAILURE! " + MultiClientsScenario.requestCounter + ") Request:" + url + " " + RESTverb + 
				    		//				" was not able to be sent.";
				    	//resultFile.append(resultInfo);
				    	//resultFile.newLine();
			    	
				    }catch (Exception e){
				    	System.err.println("Error: " + e.getMessage());
				    }
		        } finally {
		            l.unlock();
		        }
			System.err.println("MultiClientsScenario Error: could not send registration.");
		}
		else{ // successful request answer
		    Lock l = new ReentrantLock(); 
		    l.lock();
		        try {
					//write results in a file
				    try{  
				    	MultiClientsScenario.totalResponseTime += reqResponseTime;
				    	MultiClientsScenario.successfulRequest++;
				    	String resultInfo = "" +  reqResponseTime;
				    	resultFile.append(resultInfo);
				    	resultFile.newLine();
			    	
				    }catch (Exception e){
				    	System.err.println("Error: " + e.getMessage());
				    }
		        } finally {
		            l.unlock();
		        }
			System.err.println("MultiClientsScenario Success: received answer from device.");
		}
		response.getEntity().release();
	
	}

}
