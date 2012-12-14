package deviceLayer.info;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import deviceLayer.Request;

/**
 * wrapper for a resource description. 
 * @author sawielan
 * @author andreaka
 *
 */
public class Resource implements XMLRepresentable {
	
	/** the name of the resource (eg. getTemperature). */
	private String resourceName;
	
	/** a textual description of the resource. */
	private String resourceDescription;
	
	/** the methods that can be used to access the resource (eg. POST, GET). */
	private Method[] methods;
	
	// XML Cache
	/** flags whether cache is ok or not. */
	protected boolean xmlCacheDirty = true;
	
	/** the cache string. */
	protected String xmlCacheString = null;
	
	// Sensor Data Cache
	/** if Service has a cached value. */
	private boolean isCached = false;
	
	/** last cached Value. */
	private Object lastValue;
	
	/** time when last Value was cached. */
	private long timeOfLastValue;
	
	// for statistic reasons. 
	/** number of previous service return values (Statistics Mechanism). **/
	public static final int PREVIOUS_VALUES_MAX_NUM = 5;
	
	/** Queue with previous values returned by the Service. */
	private Queue<Object> previousValues= new ConcurrentLinkedQueue<Object>();
	
	/** Queue with timestamps for corresponding previousValues. */
	private Queue<Long> previousTimestamps = new ConcurrentLinkedQueue<Long>();
		
	// for low level Streaming reasons.
	
	/** indicates Streaming Capability of Service. */
	private boolean streaming = false;	
	
	/** indicates number of left iterations for the resource's stream from the Smart Device */
	private int iterations = 0;
	
	/** indicates interval for every Streaming response, produced by the Smart Device */
	private int interval   = 0;
	
	/**
	 * constructor of a resource representation.
	 * @param resourceName the name of the resource (eg. getTemperature).
	 * @param methods the methods that can be used to access the resource 
	 * (eg. POST, GET).
	 * @param mediaTypes the media type (mime) that is used for this resource.
	 * @param description a textual description of the resource.
	 */
	public Resource(
			String 		 resourceName,
			String 		 description,
			Method[]     methods
			) {
		
		this.resourceName 		 = resourceName;
		this.resourceDescription = description; 
		this.methods = new Method[methods.length]; 			 
		for(int m=0; m < methods.length; m++)
			this.methods[m] = methods[m];
		
		setXMLCacheDirty(true);
	}
	
	/**
	 * @return the methods that can be used to access the resource 
	 * (eg. POST, GET).
	 */
	public Method[] getMethods() {
		return methods;
	}
	
	/**
	 * @return the method specified by the parameter methodName
	 * (eg. POST, GET).
	 */
	public Method getMethod(String methodName){
		for(int m=0; m < methods.length; m++)
			if(methods[m].getMethodName().equalsIgnoreCase(methodName))
				return methods[m];
		return null;
	}
	
	/**
	 * @return the capabilities that can be used to access the resource 
	 * (eg. POST, GET).
	 */
	public List<String> getMethodVerbs() {
		List<String>  verbs = null;
		
		if(getMethods().length > 0){
			verbs = Collections.synchronizedList(new LinkedList<String>());
		
			for(int v=0; v < getMethods().length; v++){
				verbs.add(getMethods()[v].getMethodName());
			}
		}
		return verbs;
	}
	
	
	/**
	 * @return the name of the resource (eg. getTemperature). 
	 */
	public String getResourceName() {
		return resourceName;
	}
	
	/**
	 * @return a textual description of the resource.
	 */
	public String getResourceDescription(){
		return resourceDescription;
	}

	public String asXML() {
		String str = "";
		if (isXmlCacheOk()) {
			return xmlCacheString;
		}
		
		str += "<resource>";
		str += "<name>" + resourceName + "</name>";
		str += "<description>" + resourceDescription + "</description>";
		str += "<methods>";
		for (Method method : methods) {
			str += method.asXML();
		}
		str += "</methods>";

		str += "</resource>";
	
		xmlCacheString = str;
		setXMLCacheDirty(false);
		return str;
	}

	public void setXMLCacheDirty(boolean dirty) {
		this.xmlCacheDirty = dirty;
	}
	
	public boolean isXmlCacheOk() {
		return (false == xmlCacheDirty);
	}
	
	/**
	 * get last cached value returned by the resource (used in Caching Mechanism).
	 * @return the cached object.
	 */
	public Object getSavedValue(){
		return this.lastValue;
	}
	
	/**
	 * get time of last cached value returned by the Service (used in Caching 
	 * Mechanism) .
	 * @return the timestamp of the last cached value.
	 */
	public long getTimeOfSavedValue(){
		return this.timeOfLastValue;
	}
	
	/**
	 * save last value returned by the Service in Cache (used in Caching 
	 * Mechanism).
	 * @param value the value that was sent as last by the service.
	 * @param time the time, when the value was sent.
	 */
	public void saveLastValue(Object value, long time){
		this.isCached		 = true;
		this.lastValue 		 = value;
		this.timeOfLastValue = time;
		
		// save also last value for Statistic reasons
		this.saveForStatistics(value, time);
	}
	
	/**
	 * check if Service already has a cached value 
	 * @return true if there is a cached value, false otherwise.
	 */
	public boolean hasCachedValue(){
		return this.isCached;
	}
	
	/**
	 *  save return value and the time it was produced by the Smart Device
	 *  (used in Statistics Mechanism).
	 * @param value the object to store.
	 * @param time the timestamp of the generated response.
	 */
	private void saveForStatistics(Object value, long time){
		// if Queue is already full with values, remove oldest value
		if(this.previousValues.size() == PREVIOUS_VALUES_MAX_NUM){
			this.previousValues.remove();
			this.previousTimestamps.remove();
		}
		
		// insert next value in tail of the Queue
		this.previousValues.add(value);
		this.previousTimestamps.add(new Long(time));
	}
	
	/** get all the stored previous return values produced by the Smart 
	 * Device (used in Statistics Mechanism).
	 * @return a array of stored objects.
	 */
	public Object[] getAllPreviousValues(){
		return this.previousValues.toArray();	
	}
	
	/** 
	 * get all the stored timestamps of the previous return values produced 
	 * by the Smart Device (used in Statistics Mechanism).
	 * @return a array of timestamps.
	 */
	public Long[] getAllPreviousTimeStamps(){
		 Object[] times    = this.previousTimestamps.toArray();
		 Long[] timestamps = new Long[times.length];
		 
		 for(int i=0; i< times.length; i++)
			 timestamps[i] = (Long) times[i];
		 
		 return timestamps;
	}

	/** 
	 * check if current resource has a specific capability.
	 * @param capability the capability (eg. REST verb) to check.
	 * @return true if capable, false otherwise.
	 */
	public boolean isCapable(String capability){
		for (Method m : methods) {
			if (m.getMethodName().equalsIgnoreCase(capability)) {
				return true;
			}
		}
		return false;
	}
	
	/** when multiple Streaming Request Messages arrive, Streaming mechanism should be adapted to satisfy all the requests 
	 *  the protocol followed is that the lowest delay is selected until all the required iterations are finished
	 */
	public Request synchronizeStreamingRequest(Request r){
		Integer newInt = (Integer) r.getValues().get(0);
		Integer newIte = (Integer) r.getValues().get(1);
		int newInterval   = newInt.intValue();
		int newIterations = newIte.intValue();
		
		// perform some Mathematical equations to adapt the current Request to satisfy the previous Streaming Request
		if(newInterval > this.interval){
			float intervalDiff = newInterval / this.interval;
			this.iterations = Math.max((int) Math.abs(intervalDiff * newIterations),this.iterations);
		}
		else if(newInterval == this.interval){
			this.iterations = Math.max(newIterations,this.iterations);
		}
		else{
			float intervalDiff = this.interval / newInterval;
			this.iterations    = Math.max((int) Math.abs(intervalDiff * this.iterations), newIterations);
			this.interval      = newInterval; 
		}
		// create the new adapted Request to be sent to Smart Device
		List<Object> adaptedValues = Collections.synchronizedList(new LinkedList<Object>());
		adaptedValues.add(this.interval);
		adaptedValues.add(this.iterations);
		
		Request newReq = new Request(r.getDeviceID(), r.getServiceName(), r.getCommand(),r.getParameters(), adaptedValues, false, r.getRequestID());
		return newReq;
	}
	
	/** 
	 * when a new Streaming Message arrives from Stream produced by the Smart Device, some changes must be applied in order to stay 
	 * synchronized with many Streaming requests from Internet Clients 
	 */
	public void checkStreaming(){
		if(isStreamingEnabled()){ 
			this.iterations--;	// reduce number of left iterations
			if(this.iterations == 0){	// not any more Streaming messages expected from the Sensor Device
				unsetStreaming();	// mark Resource as not used for Streaming anymore
				this.interval = 0;
			}
		}
		else{
			// probably a not synchronized message produced by Smart Device, it is simply ignored
		}
	}
	
	/** 
	 * mark current resource that it is being used for Streaming purposes.
	 */
	public void setStreaming(int interval, int iterations){
		this.streaming   = true;
		this.interval   = interval;
		this.iterations = iterations;
	}
	
	/** 
	 * unmark current resource not being any more used for Streaming purposes. 
	 */
	private void unsetStreaming(){
		this.streaming = false;
	}
	
	/** 
	 * check if resource is being currently used for Streaming.
	 * @return true if used, false otherwise.
	 */
	public boolean isStreamingEnabled(){
		if(this.streaming == true)
			return true;
		else
			return false;
	}
	
}
