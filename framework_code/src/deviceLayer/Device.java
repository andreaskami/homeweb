package deviceLayer;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.resource.StringRepresentation;


import controlLayer.Core;
import controlLayer.eventing.Event;
import controlLayer.libraryCode.Cache;
import controlLayer.libraryCode.Constants;
import controlLayer.libraryCode.AsyncToSync;

import deviceLayer.Driver;
import deviceLayer.Request;
import deviceLayer.Response;

import deviceLayer.info.Context;
import deviceLayer.info.Resource;
import deviceLayer.info.Resources;
import deviceLayer.info.XMLRepresentable;
//import deviceLayer.info.DeviceDescription;
//import deviceLayer.info.DeviceModel;

/**
 * interface for a device stored in the system.
 *
 */
public class Device extends Observable 
	implements XMLRepresentable, Runnable {
	
	// Device General Characteristics
	
	/** the device name. */
	private String deviceName = null;
	
	/** Device Unique Identity */
	private String deviceID;
	
	/** some description textual information concerning the Smart Device */
	private String deviceDescription;
	
	/** keywords that Smart Device is capable of handling (used in Streaming Mechanism) */
	private List<String> keywords;	
	
	/** Device current Location */
	private String location;	
	
	/** low-level Driver that is responsible for communication with Smart Device's underlined specific Protocol */
	private Driver driver;	
	
	/** indicates that the Device doesn't operate normally anyomore */
	private boolean	hasFailed;
	
	/** indicates that the Device is alive or it is not alive any more and can be safely removed from the System */
	private boolean	isAlive;
	
	/** the context of this device. */
	protected Context context = new Context();
	
	/** the resources description for this device. */
	protected Resources resources = new Resources();
	
	/** Request and Response Queue used for storing Messages (also a Failure Queue used for masking mechanism) */
	public MessageQueue msgQueue;
	
	/** A random number generator initialized with a seed (used in simulations including transmission failures) */
	public static final Random random = new Random(System.currentTimeMillis()); 
	
	/** the cache string. */
	protected String xmlCacheString = null;
	
	/** flags whether cache is ok or not. */
	protected boolean xmlCacheDirty = true;
	
	/** for Multiple Concurrent Clients Test Scenario */
	public static int totalTransmissionAttempts  = 0;
	public static int failedTransmissionAttempts = 0;
	public static int cacheSuccess				 = 0;
	
	// Constructors
	
	/** Device Simple Constructor */
	public Device(String deviceName){
		this.deviceName = deviceName;
		this.deviceID   = deviceName;
		this.msgQueue  	= new MessageQueue(deviceName);
	};
	
	/** Device Advanced Constructor */
	public Device(String deviceID ,String devName, String devDesc, String location, List<String> keywords, Driver driver){
		
		// TODO: shall we use the device id in presentation or the device name
		this.deviceID   	   = deviceID;
		this.deviceName 	   = devName;
		this.keywords   	   = Collections.synchronizedList(new LinkedList<String>());
		if(keywords != null)
			this.keywords.addAll(keywords);
		this.msgQueue  	       = new MessageQueue(deviceName);
		this.deviceName 	   = devName;
		this.deviceDescription = devDesc;
		this.location          = location;
		this.driver			   = driver;
		this.isAlive		   = true;
		this.hasFailed		   = false;
		
	}

	// Device General Functionality
	
	/**
	 * initialize the device.
	 * @param params the initialization parameters.
	 */
	public void init(Map<String, Object> params) {
		// ignore
	}

	/**
	 * tear down the device.
	 */
	public void shutdown() {
		
	}
	
	/**
	 * test flag to look if this device is alive and it operates normally.
	 * @return true if alive, false otherwise.
	 */
    public boolean isAlive(){	
    	return this.isAlive;
    }
	
	/**
	 * sets a device alive. the device itself periodically informs the driver
	 * if it is still alive.
	 */
    public void setAlive(){
		this.isAlive = true;
    }
    
    private void notAlive(){
    	this.isAlive = false;
    }
    
    /** checks if Smart Device has failed (it is not alive and there no more Requests pending) */
    public boolean hasFailed(){
		return this.hasFailed;
    }
    
    /** marks Smart Device as failed */
    public void setFailed(){
    	this.hasFailed = true;
    }
	
    /** removes Failure Indication from Smart Device */
    public void unsetFailure(){

		this.hasFailed = false;
    } 

    /** Device Real-Time Operation, Thread live execution as long as Smart Device is alive */
	public void run(){
    	
    	
    	try {
    		// declarations
    		long    startTime, currentTime, lastResponseTime, timeDifference;
    		boolean requestMatched;
    		
    		// initializations
    		startTime 		 = System.currentTimeMillis();
    		lastResponseTime = System.currentTimeMillis();
    		
    		// sleep at the beginning of execution for a specified amount of time until device & service discovery phase is over
    		Thread.sleep(Constants.DEV_INITIAL_SLEEP_TIME);
    		
    		// iterate as long as the Smart Device is connected and operates normally
    		while(true){
    			
    			// in case in Response Message Queue has been added a response to a previously sent Request 
    			if(this.hasResponses()){

    				lastResponseTime = System.currentTimeMillis();
    				// check if it is an EVENT, sent by a service that offers Eventing capabilities
    				if(this.getNextResponse().getServiceName().contains(Constants.EVENTING)){
						// check Response for a possible (significant) Event
						this.handleEvent(this.getNextResponse());
						
						// remove Response from Response Queue
						this.removeResponse(this.getNextResponse());
    				}
    				// check if it is a response to a STREAMING service being executed
    				else if(this.getNextResponse().getServiceName().contains(Constants.STREAMING)){
    					// check if Request was made to set the Streaming mechanism
						if(this.hasRequests() && this.getNextRequest().getServiceName().contains(Constants.STREAMING)){ 
							
	    					if(this.hasRequests())
	    						requestMatched = this.getNextRequest().checkMatching(this.getNextResponse());
	    					else
	    						requestMatched = false;

							// check if response received matches previous request -> done to avoid streaming from other services interfere
							Integer result = (Integer) this.getNextResponse().getResult();

							if(requestMatched == true  && result.intValue() == Constants.DEV_ACKNOWLEDGMENT){
								this.getNextResponse().setRequestID(this.getNextRequest().getRequestID());	//Assign requestID to Response Message 
								this.handleResponse(this.getNextResponse());	// forward response to Internet Client who requested it

								// mark Service that it is set for streaming
								Integer interval   = new Integer((String) this.getNextRequest().getValueForParameter(Constants.STREAMING_INTERVAL));
								Integer iterations = new Integer((String) this.getNextRequest().getValueForParameter(Constants.STREAMING_PERIOD));
				    			resources.getResource(this.getNextRequest().getServiceName()).setStreaming(interval.intValue(), iterations.intValue());
								
				    			// remove Request from Request Queue
								this.removeRequest(this.getNextRequest());
							}
							else{ // it is a normal streaming response (for a previously made Request)
								if(this.resources.containsResource((this.getNextResponse().getServiceName()))){
									// modificate the streaming parameters to keep streaming synchronized and adapted to many Client requests
									this.resources.getResource(this.getNextResponse().getServiceName()).checkStreaming();
									// save in Service's Cache Response's value
									this.resources.getResource(this.getNextResponse().getServiceName()).saveLastValue(this.getNextResponse().getResult(), lastResponseTime);
									// check Response for a possible Event
									this.handleEvent(this.getNextResponse());	
								}
							}
						}
						else{	// a normal Streaming Measurement response (for a previously made Request)
							if(this.resources.containsResource((this.getNextResponse().getServiceName()))){
								// modificate the streaming parameters to keep streaming synchronized and adapted to many Client requests
								this.resources.getResource(this.getNextResponse().getServiceName()).checkStreaming();
								// save in Service's Cache Response's value
								this.resources.getResource(this.getNextResponse().getServiceName()).saveLastValue(this.getNextResponse().getResult(), lastResponseTime);
								// check Response for a possible Event
								this.handleEvent(this.getNextResponse());	
							}
    				
						}
						// remove Response from Response Queue
						this.removeResponse(this.getNextResponse());
    				}
    				else{ // check if it is a NORMAL response to the previously made Request
    					if(this.hasRequests())
    						requestMatched = this.getNextRequest().checkMatching(this.getNextResponse());
    					else
    						requestMatched = false;
    				
    					// Request was made from an Internet Client
    					if(requestMatched == true){ // response received matches previous request
    						// check if the response is not a result from an Aliveness check
    						if(!this.getNextResponse().getServiceName().contentEquals(Constants.RESOURCE_ALIVE)){
    							this.getNextResponse().setRequestID(this.getNextRequest().getRequestID());	//Assign requestID to Response Message 
    							if(this.getNextRequest().hasFailed())	// it is a response for a Request which was forwarded from Failure Masking Mechanism
    								this.handleFailedResponse(this.getNextResponse());	// forward response to Internet Client who originally made the request
    							else
    								this.handleResponse(this.getNextResponse());	// forward response to Internet Client who requested it
     					
    							// save in Service's Cache Response's value
    							if(this.getNextResponse().getCommand().equalsIgnoreCase(Constants.VERB_GET)){ // in case of a Get command put Response in Cache
    								if(this.resources.containsResource((this.getNextResponse().getServiceName())))
    									this.resources.getResource(this.getNextResponse().getServiceName()).saveLastValue(this.getNextResponse().getResult(), lastResponseTime);
    								// since it is a Get Response, we should check for a possible Event triggered
    								this.handleEvent(this.getNextResponse());	// check Response for a possible Event
    							}
    						}
    						// remove Request from Request Queue
    						this.removeRequest(this.getNextRequest());
    						// remove Response from Response Queue
    						this.removeResponse(this.getNextResponse());
    					}
    					else{ // response received doesn't match previous request
    						System.err.println("Error! A response has received, for Service (" + this.getNextResponse().getServiceName() + "), which matches no previous Request. Response deleted.");
    						// remove Response from Response Queue without forwarding to Internet Clients and/or checking for Events
    						this.removeResponse(this.getNextResponse());
    					}
    				}
    			}
    			
				currentTime    = System.currentTimeMillis();
    			
    			// in case Request Message Queue has a pending request
    			if(this.hasRequests()){
    				
    				// at first check if request is for an existing service (except if it is an Aliveness check)
    				if(!resources.containsResource(this.getNextRequest().getServiceName()) && !this.getNextRequest().getServiceName().contentEquals(Constants.RESOURCE_ALIVE)){
    					System.err.println("Error! Request for unknown Service (" + this.getNextRequest().getServiceName() + ") can not be executed. Request deleted.");
    					this.handleNoResponse(this.getNextRequest(),"Request made for an unknown Service.");
    					// remove Request from Request Queue
						this.removeRequest(this.getNextRequest());
    				}
    				else{
        				// if service is valid, use at first CACHING MECHANISM to check if automatic answer of the request is possible
        				boolean cachedSuccess = false;
        				// only in case of a Get command (not for Aliveness check)
    					if(this.getNextRequest().getCommand().equalsIgnoreCase(Constants.VERB_GET) && !this.getNextRequest().getServiceName().contentEquals(Constants.RESOURCE_ALIVE)){
							if(this.resources.getResource(this.getNextRequest().getServiceName()).hasCachedValue()){
								long serviceTime = this.resources.getResource(this.getNextRequest().getServiceName()).getTimeOfSavedValue();
								timeDifference = currentTime - serviceTime;
								// check if the cached value is still "valid" according to measurement exact time
								if(timeDifference < Constants.DEV_MAX_CACHE_DELAY_TIME){
									Object result = resources.getResource(this.getNextRequest().getServiceName()).getSavedValue();
									System.err.println("Caching Mechanism used for Service:" + this.getNextRequest().getServiceName() + " with data:" + result);	
				
									// forward response to Internet Client who requested it
									Response resp = new Response(this.getNextRequest().getDeviceID(), this.getNextRequest().getServiceName(),
																  this.getNextRequest().getCommand(), result);
									resp.setRequestID(this.getNextRequest().getRequestID());
									this.handleResponse(resp);	
									
									cachedSuccess = true;	// indicate that Cache Mechanism was used
									cacheSuccess++;
									
									// remove Request from Request Queue
									this.removeRequest(this.getNextRequest());
								}
							}
    					}
    					if(!cachedSuccess){ // if no cached value available, transmit the request to Smart Device
    						timeDifference = currentTime - startTime;
    				
    						// SYNCHRONIZATION MECHANISM: wait some amount of time to send next request to avoid request congestion in Smart Device
    						if(timeDifference > Constants.DEV_REQUEST_INTERVAL || this.hasFailed()){
    							// DEVICE FAILURE IDENTIFICATION detected
    							if(this.getNextRequest().getAttempts() >= Constants.DEV_REQUEST_MAX_ATTEMPTS || this.hasFailed()){
    								System.err.println("DBG: DEVICE HAS FAILED AND MAX ATTEMPTS REACHED");
    								// indicates that this Device should stop being presented in Internet since it is unavailable
    								this.setFailed();
								
    								// FAILURE MASKING MECHANISM
    								boolean failureCachedSuccess = false;
								
    								// mechanism works ONLY for real Client Requests (it could be an Aliveness check Message or a Streaming message)
    								if(this.resources.containsResource((this.getNextRequest().getServiceName()))){
    									if(this.getNextRequest().getCommand().equalsIgnoreCase("GET")){ // only Get commands are allowed to be forwarded for Masking Mechanism
    										// at first use Caching Mechanism for Failure to check if automatic answer of the request is possible
    										if(this.resources.getResource(this.getNextRequest().getServiceName()).hasCachedValue()){
    											long serviceTime = this.resources.getResource(this.getNextRequest().getServiceName()).getTimeOfSavedValue();
    											timeDifference = currentTime - serviceTime;

    											// Device has failed so use a bigger time frame in order to try to answer the request from Cache
    											if(timeDifference < Constants.DEV_MAX_FAILURE_DELAY_TIME){
    												Object result = this.resources.getResource(this.getNextRequest().getServiceName()).getSavedValue();
    												System.err.println("Failure Masking (Cache) Mechanism used for Service:" + this.getNextRequest().getServiceName() + " with data:" + result);	
    												
    												// forward response to Internet Client who requested it
    												Response resp = new Response(this.getNextRequest().getDeviceID(), this.getNextRequest().getServiceName(),
    																			  this.getNextRequest().getCommand(), result);
    												resp.setRequestID(this.getNextRequest().getRequestID());
    												this.handleResponse(resp);
	    							
    												failureCachedSuccess = true;
    												// remove Request from Request Queue
    												this.removeRequest(this.getNextRequest());
    											}
    										}
    										// second attempt to mask Failure: seek for a Device with a similar Service
    										if(!failureCachedSuccess){
			    						
    											System.err.println("Request for Service:" + this.getNextRequest().getServiceName() + " forwarded for Failure Masking (Device) Mechanism.");
    											// mark the Request as failed in order to be identified by the alternative Smart Device (if such one is found) 
    											this.getNextRequest().setFailed();
    											// forward request to Failure Queue
    											this.addFailedRequest(this.getNextRequest());
    										}
    									}
    									else{ // current Request not a 'Get', Failure Masking Mechanism can not be used
    										String failureMessage = "Failure Masking (Device) Mechanism can not be used for Service:" + this.getNextRequest().getServiceName();
    										System.err.println(failureMessage + ".Request deleted");	
    										// inform Client that his Request can not be satisfied
    										this.handleNoResponse(this.getNextRequest(),failureMessage);
    									}
    								}
    								// remove Request from Request Queue
    								this.removeRequest(this.getNextRequest());
    							}
    							else{	// send the Request to the Smart Device if it is still alive and operates normally
    								System.err.println("DBG: Smart Gateway sends a REQUEST to Smart Device:" + this.deviceName);
        			
    								//check if the request is for a capability, which is offered by the service
    								if(!this.getNextRequest().getServiceName().contentEquals(Constants.RESOURCE_ALIVE) && !resources.getResource(this.getNextRequest().getServiceName()).isCapable(this.getNextRequest().getCommand())){
    									System.err.println("Error! Service:" + this.getNextRequest().getServiceName() + " does not offer " 
    															+ this.getNextRequest().getCommand() + " capabilities. Request is removed.");
    									this.handleNoResponse(this.getNextRequest(),"Service does not offer the specified capability");
    									// remove Request from Request Queue
    									this.removeRequest(this.getNextRequest());
    								}
    								else{ // it is a permitted request
    									
    									// report the exact time the request dequeues from the Request Queue
    									this.getNextRequest().dequeueTime = System.currentTimeMillis();
    									
    									// this command is to simulate multiple failure attempts
    									// if(this.getNextRequest().getAttempts() < 1)
    									
    									// send the next pending request to Smart Device
    									this.handleRequest(this.getNextRequest());
    									
    									// measure exact time when the request was sent
    									startTime = System.currentTimeMillis();
    									// increment Request Attempt in Request Queue to achieve fault tolerance
    									this.getNextRequest().incAttempts();
    									
    									totalTransmissionAttempts ++;
    									if(this.getNextRequest().getAttempts() > 1)
    										failedTransmissionAttempts++;
    								}
    							}
    						}
    					}
    				}
    			}
    			else if(this.hasFailed() && this.isAlive()){ // no more Requests are pending and Device has failed
    				System.err.println("Device has failed after all Requests have been satisfied/forwarded. Thread stops...");
    				// indicate that the Device is not any more alive, now Devices can safely remove Smart Device from the system
    				this.notAlive();
    				// invoce (Device) FAILURE MASKING MECHANISM + delete Device after all Requests are examined
    				controlLayer.Core.getInstance().getDevices().handleFailedRequests(this);
    				// after all the requests have been examined, the current Device is safely removed from the system
    				controlLayer.Core.getInstance().getDevices().removeDevice(this.getDeviceID()); 
    				break;
    			}
    					
    			// Smart Device Failure Identification Check
    			timeDifference = currentTime - lastResponseTime;
    			if(!this.hasFailed() && timeDifference > Constants.DEV_ALIVENESS_CHECK_TIME){
    				// send an Aliveness request to Smart Device to examine aliveness
    	    		List<String> params   	   = Collections.synchronizedList(new LinkedList<String>());
    	    		List<Object> values   	   = Collections.synchronizedList(new LinkedList<Object>());
    	    		this.addRequest(new Request(this.deviceID,Constants.RESOURCE_ALIVE,Constants.VERB_GET,params, values, false,0));
    	    		lastResponseTime           = System.currentTimeMillis();
    			}
    		}
    	} catch(IOException e){
    		e.printStackTrace();
    	} 
    	catch (InterruptedException e) {
			e.printStackTrace();
		}
    	catch (Exception e){
    		e.printStackTrace();
    	}
    	
    }
    
    // Getter & Setter Functions
	
    public String getDeviceID(){
    	return deviceID;
    }
	
	/**
	 * returns the device name. within the gateway the device name has to be 
	 * unique.
	 * @return the device name.
	 */
	public String getDeviceName(){
		return deviceName;
	}
	
	/**
	 * sets the name of the device. be careful with this method. especially do 
	 * not call it when you have already installed the device in the gateway. 
	 * otherwise this could lead to inconsistency to the device mapping within 
	 * the gateway.
	 * @param deviceName the new name for this device.
	 */
	public void setDeviceName(String deviceName){
		//getDevice(deviceID)
		//this.deviceName = deviceName;
		this.deviceName = getDeviceID();
	}

	/**
	 * gives a handle to the description of this device.
	 * @return the description of this device (model, vendor, ...).
	 */
	public String getDeviceDescription() {
		return deviceDescription;
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
	
	public int getMaxPendingRequests(){
		return msgQueue.getMaxPendingRequestNum();
	}
	
	/**
	 * returns a handle to the context of this device.
	 * @return the context of this device.
	 */
	public Context getContext() {
		return context;
	}
	
	/** 
	 * gives a handle to the resources description this device is providing.
	 * @return the resources description provided by this device.
	 */
	public Resources getResources() {
		return resources;
	}
	
	// Manipulate Services offered by the Device
	
	/** add a new Service in Smart Device's Service List */
	public void addResource(Resource value){
		resources.addResource(value);
	}
	
	/**  checks if Smart Device contains Service with name serviceID */
	public boolean containsService(String resourceName){
		return resources.containsResource(resourceName);
	}
	
	public Collection<Resource> getServiceValues(){
		return resources.getResources();
	}
	
	// methods for interaction with Message Queue
	
	/** retrieves next Request from Request Message Queue */
	protected deviceLayer.Request getNextRequest(){
		deviceLayer.Request r = this.msgQueue.getNextRequestMessage();
		return r;
	}
	
	/** retrieves next Failed Request from Failed Requests Message Queue (used in Failure Masking mechanism) */
	protected deviceLayer.Request getNextFailedRequest(){
		deviceLayer.Request r = this.msgQueue.getNextFailedRequestMessage();
		return r;
	}
	
	/** retrieves next Response from Response Message Queue */
	protected deviceLayer.Response getNextResponse(){
		deviceLayer.Response r = this.msgQueue.getNextResponseMessage();
		return r;
	}
	
	/** checks if Request Message Queue has pending Requests */
	protected boolean hasRequests(){
		boolean r = this.msgQueue.hasRequestMessage();
		return r;
	}
	
	/** checks if Failed Requests Message Queue has pending Failed Requests (used in Failure Masking mechanism) */
	protected boolean hasFailedRequests(){
		boolean r = this.msgQueue.hasFailedRequestMessage();
		return r;
	}	
	
	/** checks if Response Message Queue has pending Responses */
	protected boolean hasResponses(){
		boolean r = this.msgQueue.hasResponseMessage();
		return r;
	}
		
	/** adds Request r in Request Message Queue */
	public void addRequest(deviceLayer.Request r){
		System.out.println("New request added to " + r.getDeviceID() + "'s Request Queue.");
		this.msgQueue.addRequestMessage(r);
	}
	
	/** adds Failed Request r in Failed Requests Message Queue */
	protected void addFailedRequest(deviceLayer.Request r){
		this.msgQueue.addFailedRequestMessage(r);
	}
	
	/** adds Response r in Response Message Queue */
	public void addResponse(deviceLayer.Response r){
		long k=0;
		k= r.getRequestID();
		this.msgQueue.addResponseMessage(r,k);
	}	

	/** removes Request r from Request Message Queue */
	protected void removeRequest(deviceLayer.Request r){
		this.msgQueue.deleteRequestMessage(r);
	}
	
	/** removes Failed Request r from Failed Requests Message Queue (used in Failure Masking mechanism) */
	public void removeFailedRequest(deviceLayer.Request r){
		this.msgQueue.deleteFailedRequestMessage(r);
	}
	
	/** removes Response r from Response Message Queue */
	protected void removeResponse(deviceLayer.Response r){
		this.msgQueue.deleteResponseMessage(r);
	}

	// Methods that deal with Requests & Responses Handling
	
	/** Handles Request made from Internet Clients by forwarding it to appropriate underlined Driver */
    private synchronized void handleRequest(deviceLayer.Request req) throws IOException{

    	// check at first if it is a request for streaming mechanism and if Resource produces already an streaming stream from Smart Device
    	if(req.getServiceName().contains(Constants.STREAMING) &&
    		this.resources.getResource(req.getServiceName()).isStreamingEnabled()){
    			Request ereq = this.resources.getResource(req.getServiceName()).synchronizeStreamingRequest(req);
    			driver.sendMessage(ereq.getDeviceID(), 'R', ereq);
    	}	
    	else{	// it is a normal request, trade it normally
    		int probabilityOfTransmission = Math.abs(random.nextInt()) % 100;
    		if(probabilityOfTransmission >= Constants.TRANSMISSION_FAILURE){
    			driver.sendMessage(req.getDeviceID(), 'R', req);
    		}
    		else // transmission failure
    			System.out.println("TRANSMISSION_FAILURE (Reason: Simulation)");
    	}
    }
    
    /**  Handles Responses from Smart Device by forwarding them to the appropriate Internet Client who made the Request */
    public synchronized void handleResponse(deviceLayer.Response r){
    	System.out.println("Handling normal Response for service:"+r.getServiceName());
    	
    	// if Database Support is enabled, upload the new Measurement in Database
    	if(Core.getInstance().hasDatabaseSupport()){
    		String query = "INSERT INTO Measurement (Time, DeviceID, ServiceName, Value) VALUES (CURRENT_TIMESTAMP,'" + r.getDeviceID() +"','" + r.getServiceName() + "','" + (Integer)r.getResult() + "')";
    		Core.getInstance().getDatabaseHandler().executeUpdate(query);
		}
    	
    	dispatchResponse(r);
    }
    
    /** Handles Responses for Requests which were forwarded from failed Smart Devices */
    public synchronized void handleFailedResponse(deviceLayer.Response r){
    	System.out.println("Handling failed Response for service:"+r.getServiceName() + ". Device Failure Masking Mechanism was used.");
    	dispatchResponse(r);
    }
    
    /**  Handles Responses to Clients for Requests which could not be at all satisfied */
    public synchronized void handleNoResponse(deviceLayer.Request req, String failureMessage){
    	System.out.println("Handling a Response Message for service:" + req.getServiceName() + " which could not be executed.");
    	System.out.println("Reason:" + failureMessage);
    	Response resp = new Response(req.getDeviceID(), req.getServiceName(), req.getCommand(), failureMessage);
    	resp.setRequestID(req.getRequestID());
    	dispatchResponse(resp);
    }
    
    /**  Handle Responses from Smart Device by checking for events caused by the measurements taken by Devices' sensors */
    public synchronized void handleEvent(deviceLayer.Response r){
    	System.out.println("Checking Response for Events for service:"+r.getServiceName());
    	
    	// decide whether event or not
    	setChanged();
    	Event event = new Event(
			r.getServiceName(), 
				r.getResult().toString(),
				getDeviceName(),
				getContext().getSymbolicLocation().getLocation()
				);
		//notifyObservers(event);
		Core.getInstance().getEventManagement().notifyEvent(event);
    }
	

	/**
	 * handle a request.
	 * @param response the response to the request.
	 * @param request the request that was performed on the gateway.
	 * @return the representation as String or if set in response null.
	 */
	public String handle(org.restlet.data.Response response, org.restlet.data.Request request) {
		
		List<String> segments = request.getResourceRef().getSegments();
		String error;

		// if there are more than one segments, we possibly have a request to a resource
		if (segments.size() > 2) {
			if (getResources().containsResource(segments.get(2))) {
		
				return normalRequest(response, request, segments);
			}
			else{	// Resource has not a valid Identifier
				error = String.format("requested resource not existant: %s", segments.get(2));
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				response.setEntity(new StringRepresentation(error));
				return null;
			}
		}
		
		// do nothing in order to send the default response
		return asXML();	
	}

	/**  request for a normal Service offered by the Smart Device */
	private String normalRequest(org.restlet.data.Response response,
			org.restlet.data.Request request, List<String> segments) {
		
		String error = checkRequest(request);
		//log.debug("error == " + error);
		if (null != error) {
			// set the bad request...
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			response.setEntity(new StringRepresentation(error));
			return null;
		}
		
		// request is for a "valid" request
		String resourceName = segments.get(2);

		// check if the request is for Streaming
		if(segments.size() > 2 && segments.get(3).equalsIgnoreCase(Constants.STREAMING))
			resourceName = resourceName + "/" + segments.get(3);
		
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
		
		deviceLayer.Request re = new Request(deviceID, resourceName, method, params, values, false, 0);
		Response r = waitSynchronous(re);
		if (null == r) {
			response.setEntity(new StringRepresentation(Constants.NACK));
		} else {
			//log.debug(r.getResult());
			response.setEntity(new StringRepresentation(r.getResult().toString()));
		}
		return null;

	}
	
	/**
	 * checks whether a request is valid for the requested resource. tests if 
	 * the required parameters fit MIME-types etc...
	 * @param request the request to be tested.
	 * @return null if everything is OK, error string otherwise.
	 */
	public String checkRequest(org.restlet.data.Request request) {
		String error    = null;
		List<String> s = request.getResourceRef().getSegments();
		
		// request just the base URL of the device.
		if (s.size() == 1) {
			//log.debug("process base url of the device (device description)");
			return null;
		}
		String resource = s.get(2);
		// check if the request is for Streaming
		if(s.size() > 2 && s.get(2).equalsIgnoreCase(Constants.STREAMING))
			resource = resource + "/" + s.get(3);
			
		// test the request method (PUT, POST, DELETE, ...)
		Resource res       = resources.getResource(resource);
		
		// special case for Streaming, since encapsulated in POST request
		String capability;
		if(Constants.STREAMING.equalsIgnoreCase(s.get(3))){
			if(!request.getMethod().getName().equalsIgnoreCase(Constants.VERB_POST)){
				error = "Streaming Requests must be encapsulated in POST Requests";
				return error;
			}	
		}
		
		capability  = request.getMethod().getName();
		Form form 	  = request.getEntityAsForm();

		if(!res.isCapable(capability)){
			error = "request method does not fit the resource: " + request.getMethod().getName();
			return error;
		}
		
		// now test the parameters and MIME-types
		Method m = request.getMethod();

		// parameter check for POST requests
		if( m.getName().equalsIgnoreCase(Constants.VERB_POST)) {
			//log.debug(String.format("found matching request method: %s", m.getName()));
			for (String p : res.getMethod(Constants.VERB_POST).getParameterNames()){
				if (null == form.getFirst(p)) {
					error = String.format("missing parameter: %s", p);
					//log.debug(error);
					return error;
				}
			}
			return null;
		}

		//log.debug(error);
		return null;
	}
	
	// Methods for changing from synchronous to asynchronous operation and vice-versa
	
	public Response waitSynchronous(deviceLayer.Request request) {
		// handle a request with lock wait...
		AsyncToSync lock = new AsyncToSync();
		synchronizer.put(lock.getToken(), lock);
		try {
			//log.debug("wait on lock.");
			request.setRequestID(lock.getToken());
			addRequest(request);
			
			synchronized (lock) {
				lock.wait();
			}
			//log.debug("leaving lock");
		} catch (Exception e) {
			e.printStackTrace();
			synchronizer.remove(lock);
		}
		//log.debug("left lock");
		synchronizer.remove(lock.getToken());
		return lock.getResponse();		
	}
	
	// locking stuff...
	
	/** the next free token. DONT MODIFY DIRECTLY, USE getToken() */
	private static Long msgToken = new Long(1);
	
	/** a hash map containing the synchronizer objects. */
	private static Map<Long, AsyncToSync> synchronizer = new ConcurrentHashMap<Long, AsyncToSync> ();
	
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
			try{
				boolean found = false;
				Request tempr = null;
				Devices devs = Core.getInstance().getDevices();
				List<String> dev = devs.getDeviceIDs();
				for(int i=0; i< dev.size(); i++){
					if(devs.getDevice(dev.get(i)).msgQueue.hasRequestMessage() ){
						tempr = devs.getDevice(dev.get(i)).msgQueue.getNextRequestMessage();
						
						if(tempr.getRequestID() == r.getRequestID()){
							found = true;
							tempr = devs.getDevice(dev.get(i)).msgQueue.getNextRequestMessage();
							System.out.println("Response Message matches request message with RequestID=" + tempr.getRequestID());
							break;
						}
					}
				}
				if(found ==  true){
					lock.setResponse(r);
					lock.notifyAll();
					System.out.println("Response Message arrived after " + tempr.getAttempts() + " attempts");
				}
				else
					System.out.println("LOCK ERROR: Nobody to notify about the new response.");
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
		
	// Methods for XML representation of the Device
	
	public void setXMLCacheDirty(boolean dirty) {
		controlLayer.Core.getInstance().getCache().invalidate(this);
		this.xmlCacheDirty = dirty;
	}
	
	public boolean isXmlCacheOk() {
		if (xmlCacheDirty) {
			return false;
		}
		if (!context.isXmlCacheOk()) {
			return false;
		}
		if (!resources.isXmlCacheOk()) {
			return false;
		}
		return true;
	}
	
	public String asXML() {
		if (isXmlCacheOk()) {
			return xmlCacheString;
		}
		
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		buf.append("<device>");

		buf.append("<name>" + getDeviceName() + "</name>");
		
		// append the context
		buf.append(getContext().asXML());
		
		// append the service description
		buf.append(getResources().asXML());
		
		// append device description
		//str.append(getDeviceDescription().asXML(false));
				
		buf.append("</device>");
		xmlCacheString = buf.toString();
		setXMLCacheDirty(false);
		controlLayer.Core.getInstance().getCache().cache(getDeviceName() + Cache.SUFFIX[Cache.XML], xmlCacheString);
		return xmlCacheString;
	}

}
