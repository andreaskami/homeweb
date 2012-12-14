package simulation.multiClientsScenario;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.resource.StringRepresentation;

import controlLayer.libraryCode.Constants;

import deviceLayer.Device;
import deviceLayer.Driver;
import deviceLayer.MessageQueue;
import deviceLayer.Request;
import deviceLayer.Response;
import deviceLayer.info.DeviceDescription;
import deviceLayer.info.DeviceModel;
import deviceLayer.info.Resource;


// Represents every Device introduced to Smart Gateway as a fully operational Thread, independently from its specific Characteristics
public class DeviceAsProxy extends Device {

	
	// locking stuff...
	/** the next free token. DONT MODIFY DIRECTLY, USE getToken() */
	private static Long msgToken = new Long(1);
	
	/**
	 * @return a token for the message id.
	 */
	public static synchronized long getToken() {
		synchronized (msgToken) {
			msgToken++;
			return msgToken;
		}
	}
	
	/**
	 * dispatch a response to the synchronizer.
	 * @param r the low level response.
	 */
	public static synchronized void dispatchResponse(Response r) {
		AsyncToSync lock = synchronizer.get(r.getRequestID());
		if (null == lock) {
			return;
		}
		
		synchronized (lock) {
			lock.setResponse(r);
			lock.notifyAll();
		}
	}
	
	/**
	 * helper class to synchronize the async communication to contiki/tinyos.
	 * @author sawielan
	 *
	 */
	public class AsyncToSync {
		
		/** my token. */
		private final long token;
		
		/** the response onto my request. */
		private Response response = null;

		/** 
		 * constructor.
		 */
		public AsyncToSync() {
			token = DeviceAsProxy.getToken();
		}
		
		/**
		 * @return my token. 
		 */
		public long getToken() {
			return token;
		}
		
		/**
		 * @return the response
		 */
		public Response getResponse() {
			return response;
		}

		/**
		 * @param response the response to set
		 */
		public void setResponse(Response response) {
			this.response = response;
		}
	};
	
	/** a hash map containing the synchronizer objects. */
	private static Map<Long, AsyncToSync> synchronizer = 
		new ConcurrentHashMap<Long, AsyncToSync> ();
	
	// \\ end of synchronizing
	
	/** the identifier for the mote eventing. */
	public static final String MOTE_EVENTING = "EVENTING";
	
	public static final String MOTE_EVENTING_INTERVAL = "interval";
	public static final String MOTE_EVENTING_PERIOD = "iterations";
	
	// static definitions
	private static final int INITIAL_SLEEP_TIME     = 60000;  // sleep at the beginning 1 minute until all service description data arrive
	public  static final  int REQUEST_INTERVAL      = 0;   // send requests one after the other every x seconds
	public  static final int MAX_CACHE_DELAY_TIME   = 0; // service cached last value validity lasts 2 minutes
	public  static final int REQUEST_MAX_ATTEMPTS   = 0;      // maximum number of attempts to transmit a request (before Failure is detected)
	private static final int ACKNOWLEDGMENT         = 1;	  // Acknowledgment value returned by the Smart Device (for POST and EVENTING requests)

	// for Multiple Concurrent Clients Test Scenario
	public static int totalTransmissionAttempts  = 0;
	public static int failedTransmissionAttempts = 0;
	public static int cacheSuccess				 = 0;
	
	// Device General Characteristics
	private String       		 deviceName;		// name of Smart Device
	private String				 deviceID;			// Device Unique Identity
	private String				 deviceDescription;	// some Description Info concerning the Smart Device
	private List<String> 		 keywords;			// keywords that Smart Device is capable of handling (used in Eventing Mechanism)
	private String       		 location;			// Device current Location
	// TODO remove me
	//private Map<String, Service> services;			// List of Services offered by Smart Device 
	private MessageQueue 	     msgQueue;			// Request and Response Queue used for storing Messages (also a Failure Queue used for masking mechanism) 
	private Driver				 driver;			// low-level Driver that is responsible for communication with Smart Device underlined Protocol
	private boolean				 hasFailed;			// indicates that the Device doesn't operate normally anyomore
	private boolean				 isAlive;			// indicates that the Device is alive or it is not alive any more and can be safely removed from the System
	
	private DeviceAsProxy(String name) {
		super(name);
	}
	
	// Device Advanced Constructor
	DeviceAsProxy(String deviceID ,String devName, String devDesc, String location, List<String> keywords, Driver driver){
		
		// TODO: shall we use the device id in presentation or the device name
		super(deviceID);
		
		this.keywords   	   = Collections.synchronizedList(new LinkedList<String>());
		this.keywords.addAll(keywords);
		this.deviceID		   = deviceID;
		this.deviceName 	   = devName;
		this.deviceDescription = devDesc;
		this.location          = location;
		this.msgQueue  		   = new MessageQueue(devName);
		this.driver			   = driver;
		this.isAlive		   = true;
		this.hasFailed		   = false;
		
	}
	
	/* Handles Request made from Internet Clients by forwarding it to appropriate underlined Driver */
    private synchronized void handleRequest(Request req) throws IOException{
    	// it is a normal request, trade it normally
    	driver.sendMessage(req.getDeviceID(), 'R', req);
    }
    
    /*  Handles Responses from Smart Device by forwarding them to the appropriate Internet Client who made the Request */
    public synchronized void handleResponse(Response r){
    	System.out.println("Handling normal Response for service:"+r.getServiceName());
    	dispatchResponse(r);
    }
    
    /* Handles Responses for Requests which were forwarded from failed Smart Devices */
    public synchronized void handleFailedResponse(Response r){
    	System.out.println("Handling failed Response for service:"+r.getServiceName() + ". Device Failure Masking Mechanism was used.");
    	dispatchResponse(r);
    }
    
    /*  Handles Responses to Clients for Requests which could not be at all satisfied */
    public synchronized void handleNoResponse(Request req, String failureMessage){
    	System.out.println("Handling a Response Message for service:" + req.getServiceName() + " which could not be executed.");
    	System.out.println("Reason:" + failureMessage);
    	Response resp = new Response(req.getDeviceID(), req.getServiceName(), req.getCommand(), failureMessage);
    	resp.setRequestID(req.getRequestID());
    	dispatchResponse(resp);
    }
    
    /*  Handle Responses from Smart Device by checking for events caused by the measurements taken by Devices' sensors */
    public synchronized void handleEvent(Response r){
    	System.out.println("Checking Response for Events for service:"+r.getServiceName());
    	
    	// decide whether event or not
    	
    	setChanged();
    	//Event event = new Event(
			//	r.getServiceName(), 
			//	r.getResult().toString(),
			//	getDeviceName(),
			//	getContext().getSymbolicLocation().getLocation()
			//	);
		//notifyObservers(event);
    }
	
	/* add a new Service in Smart Device's Service List */
	public void addService(String key, Resource value){
		resources.addResource(value);
	}
	
	/*  checks if Smart Device contains Service with name serviceID */
	public boolean containsService(String resourceName){
		return resources.containsResource(resourceName);
	}
	
	/* retrieves next Request from Request Message Queue */
	public Request getNextRequest(){
		Request r = this.msgQueue.getNextRequestMessage();
		return r;
	}
	
	/* retrieves next Failed Request from Failed Requests Message Queue (used in Failure Masking mechanism) */
	public Request getNextFailedRequest(){
		Request r = this.msgQueue.getNextFailedRequestMessage();
		return r;
	}
	
	/* retrieves next Response from Response Message Queue */
	public Response getNextResponse(){
		Response r = this.msgQueue.getNextResponseMessage();
		return r;
	}
	
	/* checks if Request Message Queue has pending Requests */
	public boolean hasRequests(){
		boolean r = this.msgQueue.hasRequestMessage();
		return r;
	}
	
	/* checks if Failed Requests Message Queue has pending Failed Requests (used in Failure Masking mechanism) */
	public boolean hasFailedRequests(){
		boolean r = this.msgQueue.hasFailedRequestMessage();
		return r;
	}	
	
	/* checks if Response Message Queue has pending Responses */
	public boolean hasResponses(){
		boolean r = this.msgQueue.hasResponseMessage();
		return r;
	}
		
	/* adds Request r in Request Message Queue */
	public void addRequest(Request r){
		System.out.println("New request added to " + r.getDeviceID() + "'s Request Queue.");
		this.msgQueue.addRequestMessage(r);
	}
	
	/* adds Failed Request r in Failed Requests Message Queue */
	public void addFailedRequest(Request r){
		this.msgQueue.addFailedRequestMessage(r);
	}
	
	/* adds Response r in Response Message Queue */
	public void addResponse(Response r){
		long k=0;
		k=r.getRequestID();
		this.msgQueue.addResponseMessage(r,k);
	}	

	/* removes Request r from Request Message Queue */
	public void removeRequest(Request r){
		this.msgQueue.deleteRequestMessage(r);
	}
	
	/* removes Failed Request r from Failed Requests Message Queue (used in Failure Masking mechanism) */
	public void removeFailedRequest(Request r){
		this.msgQueue.deleteFailedRequestMessage(r);
	}
	
	/* removes Response r from Response Message Queue */
	public void removeResponse(Response r){
		this.msgQueue.deleteResponseMessage(r);
	}
	
	/* checks if Smart Device is still alive and it operates normally */
    public boolean isAlive(){	
    	return this.isAlive;
    }
	
    /* sets Smart Device as alive */
    public void setAlive(){
		this.isAlive = true;
    }
    
    private void notAlive(){
    	this.isAlive = false;
    }
    
    /* checks if Smart Device has failed (it is not alive and there no more Requests pending) */
    public boolean hasFailed(){
		return this.hasFailed;
    }
    
    /* marks Smart Device as failed */
    public void setFailed(){
    	this.hasFailed = true;
    }
	
    /* removes Failure Indication from Smart Device */
    public void unsetFailure(){

		this.hasFailed = false;
    } 

    /* Device Real-Time Operation, Thread live execution as long as Smart Device is alive */
	public void run(){
    	
    	
    	try {
    		// declarations
    		boolean requestMatched;
    		List<Request> sentRequests;
    		sentRequests = Collections.synchronizedList(new LinkedList<Request>());
    		
    		// sleep at the beginning of execution for a specified amount of time until device & service discovery phase is over
    		Thread.sleep(INITIAL_SLEEP_TIME);
    		
    		// iterate as long as the Smart Device is connected and operates normally
    		while(true){
    			
    			// rESPONSE MESSAGE ARRIVED FROM SMART DEVICE
    			if(this.hasResponses()){
    				
    				// check if it is a response to an eventing service being executed
    				if(this.getNextResponse().getCommand().equalsIgnoreCase("EVENTING")){
    					
    					requestMatched = false;
    					int i, deviceNum;
						for(i= sentRequests.size()-1; requestMatched != true && i >= 0; i--){	
							requestMatched = sentRequests.get(i).checkMatching(this.getNextResponse());							
						}
							
						// check if response received matches previous request -> done to avoid eventing from other services interfere
						Integer result = (Integer) this.getNextResponse().getResult();
						if(requestMatched == true  && result.intValue() == ACKNOWLEDGMENT){
							deviceNum = i+1;
							this.getNextResponse().setRequestID(sentRequests.get(deviceNum).getRequestID());	//Assign requestID to Response Message 
							sentRequests.remove(deviceNum);
							this.handleResponse(this.getNextResponse());	// forward response to Internet Client who requested it
						}
						else{ // it is a normal Eventing response (for a previously made Request)
							
							if(this.resources.containsResource((this.getNextResponse().getServiceName()))){

								// check Response for a possible Event
								this.handleEvent(this.getNextResponse());	
							}
						}

						// remove Response from Response Queue
						this.removeResponse(this.getNextResponse());
					}
    				else{ // check if it is response to a previously made Request
    					requestMatched = false;
    					int i, deviceNum;
						for(i= sentRequests.size()-1; requestMatched != true && i >= 0; i--){	
							requestMatched = sentRequests.get(i).checkMatching(this.getNextResponse());
						}
    				
    					// Request was made from an Internet Client
    					if(requestMatched == true){ // response received matches previous request
    						deviceNum = i+1;
    						this.getNextResponse().setRequestID(sentRequests.get(deviceNum).getRequestID());	//Assign requestID to Response Message 
    						sentRequests.remove(deviceNum);
    						this.handleResponse(this.getNextResponse());	// forward response to Internet Client who requested it

    						// remove Response from Response Queue
    						this.removeResponse(this.getNextResponse());
    					}
    					else{ // response received doesn't match previous request
    						System.err.println("Error! A response has received, for Service (" + this.getNextRequest().getServiceName() + "), which matches no previous Request. Response deleted.");
    						// remove Response from Response Queue without forwarding to Internet Clients and/or checking for Events
    						this.removeResponse(this.getNextResponse());
    					}
    				}
    			}
    			
    			// INCOMING REQUEST ARRIVED FROM PRESENTATION LAYER
    			if(this.hasRequests()){ 
    				// at first check if request is for an existing service (except if it is an Aliveness check)
    				if(!resources.containsResource(this.getNextRequest().getServiceName())){
    					System.err.println("Error! Request for unknown Service (" + this.getNextRequest().getServiceName() + ") can not be executed. Request deleted.");
    					this.handleNoResponse(this.getNextRequest(),"Request made for an unknown Service.");
    					// remove Request from Request Queue
						this.removeRequest(this.getNextRequest());
    				}
    				else{
    					
    						// send the Request to the Smart Device if it is still alive and operates normally
    						System.err.println("Smart Gateway sends a request to Smart Device:" + this.deviceID);
        			
    						//check if the request is for a capability, which is offered by the service
    						if(!resources.getResource(this.getNextRequest().getServiceName()).isCapable(this.getNextRequest().getCommand())){
    							System.err.println("Error! Service:" + this.getNextRequest().getServiceName() + " does not offer " 
    													+ this.getNextRequest().getCommand() + " capabilities. Request is removed.");
    							this.handleNoResponse(this.getNextRequest(),"Service does not offer the specified capability");
    							// remove Request from Request Queue
    							this.removeRequest(this.getNextRequest());
    						}
    						else{ // it is a permitted request
    							// send the next pending request to Smart Device
    							this.handleRequest(this.getNextRequest());

    							totalTransmissionAttempts ++;
    							
    							// add it to a temporary list
    							sentRequests.add(this.getNextRequest());
    							
    							// NO RETRANSMISSIONS: simply remove request after sending it
    							this.removeRequest(this.getNextRequest());
    						}
   					}
    			}
    		}
    	}catch(IOException e){
   			e.printStackTrace();
   		} 
   		catch (InterruptedException e) {
   			e.printStackTrace();
   		}
	}
    
    // Getter & Setter Functions
	
    public String getDeviceID(){
    	return deviceID;
    }
    
	public String getDeviceName(){
		// FIXME  make this again nice and sweet
		return deviceID;
		//return deviceName;
	}
	
	public void setDeviceName(String deviceName){
		//getDevice(deviceID)
		//this.deviceName = deviceName;
		this.deviceName = getDeviceID();
	}

	public String getDeviceDescription(){
		return this.deviceDescription;
	}
	
	public void setDeviceDescription(String deviceDescription){
		this.deviceDescription = deviceDescription;
	}
	
	public List<String> getKeywords(){
		return this.keywords;
	}
	
	public void setKeywords(List<String> keywords){
		this.keywords.clear(); 
		this.keywords.addAll(keywords);
	}
	
	public void setLocation(String location){
		this.location = location;
	}
	
	public String getLocation(){
		return location;
	}
	
	public Collection<Resource> getServiceValues(){
		return resources.getResources();
	}
	
	public int getMaxPendingRequests(){
		return msgQueue.getMaxPendingRequestNum();
	}

	@Override
	public String handle(org.restlet.data.Response response,
			org.restlet.data.Request request) {
		
		List<String> segments = request.getResourceRef().getSegments();
		// don't proceed if device name not matching...
		if (!super.getDeviceName().equals(segments.get(0))) {
			return asXML();
		}

		// if there are more than one segments, we possibly have a request to 
		// a service/resource
		if (segments.size() > 1) {
			if (getResources().containsResource(segments.get(1))) {
				// test if eventing is requested and that the resource 
				// is capable to handle eventing
				if ((segments.size() > 2) 
						&& MOTE_EVENTING.equals(segments.get(2))
						&& getResources().getResource(segments.get(1)).
							isCapable(MOTE_EVENTING)) {
					return requestEventing(response, request, segments);
				}
				
				String error = checkRequest(request);

				if (null != error) {
					// set the bad request...
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					response.setEntity(new StringRepresentation(error));
					return null;
				}
				
				// request is for a normal request
				String resourceName = segments.get(1);
				String method 		= request.getMethod().getName();
				List<String> params   	   = Collections.synchronizedList(new LinkedList<String>());
				List<Object> values   	   = Collections.synchronizedList(new LinkedList<Object>());
				Form eForm = request.getEntityAsForm();
				for(int i=0; i < eForm.size(); i++){
					Parameter currentParam = eForm.get(i);
					params.add(currentParam.getName());
					if(currentParam.getValue() == null)	// check if the parameter contains no value
						values.add("0");	// assume that the user intented to put the zero value
					else
						values.add(currentParam.getValue());
				}
				
				Request re = new Request(deviceID,resourceName,method,params, values, false,0);
				Response r = waitSynchronous(re);
				if (null == r) {
					response.setEntity(new StringRepresentation(Constants.NACK));
				} else {

					response.setEntity(
							new StringRepresentation(r.getResult().toString()));
				}
				return null;
			}
			// do nothing in order to send the default response
		}
		return asXML();
	}
	
	private String requestEventing(org.restlet.data.Response response,
			org.restlet.data.Request request, List<String> segments) {
		

		// enable eventing...
		Form eForm = request.getEntityAsForm();
		if ((null != eForm.getFirst(MOTE_EVENTING_INTERVAL)) 
				&& null != eForm.getFirst(MOTE_EVENTING_PERIOD)) {
			
			// we have the parameters set...
				
			List<String> eparams = Collections.synchronizedList(new LinkedList<String>());
			List<Object> evalues = Collections.synchronizedList(new LinkedList<Object>());
			eparams.add("delay");
			evalues.add(Integer.parseInt(
					eForm.getFirst(MOTE_EVENTING_INTERVAL).getValue()));
			
			eparams.add("times");
			evalues.add(Integer.parseInt(
					eForm.getFirst(MOTE_EVENTING_PERIOD).getValue()));
			
			System.out.println(String.format("process request: delay=%s,times=%s", evalues.get(0), evalues.get(1)));
			
			Request re = new Request(
					deviceID, segments.get(1), MOTE_EVENTING, 
					eparams, evalues, false, 0);
			
			Response r = waitSynchronous(re);
			if (null != r) {
				response.setStatus(Status.SUCCESS_OK);
				System.out.println(r.getResult());
				response.setEntity(
						// FIXME
						new StringRepresentation(r.getResult().toString()));

				return null;
			} 
		}
		
		return asXML();
	}
	
	public Response waitSynchronous(Request request) {
		// handle a request with lock wait...
		AsyncToSync lock = new AsyncToSync();
		synchronizer.put(lock.getToken(), lock);
		try {

			request.setRequestID(lock.getToken());
			addRequest(request);
			
			synchronized (lock) {
				lock.wait();
			}

		} catch (Exception e) {
			e.printStackTrace();
			synchronizer.remove(lock);
		}
		synchronizer.remove(lock.getToken());
		return lock.getResponse();		
	}

	@Override
	public void init(Map<String, Object> params) {
		// ignore
	}
}
