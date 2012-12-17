package deviceLayer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.xml.sax.SAXParseException;

import controlLayer.Core;
import controlLayer.libraryCode.Constants;
import controlLayer.libraryCode.WADLparser;
import deviceLayer.Device;
import deviceLayer.info.Method;
import deviceLayer.info.Resource;

// it represents all the Smart Devices that are available in the Smart Gateway's neighborhood
// it is actually the class which communicates with upper layers (Control and Presentation Layer)
public class Devices extends java.util.Observable implements java.util.Observer {

    private Map<String, Device> smartDevices; 	// the list of Smart Devices found in the nearby environment
	FileWriter fstream;
	static public BufferedWriter reqQueueTimesFile;
	
	// default constructor
	public Devices(){
		smartDevices = new ConcurrentHashMap<String, Device>();
		
		try{
			String fileName = "tinyRequestQueueTimeDelay.txt";
			fstream = new FileWriter(fileName, true);
			reqQueueTimesFile = new BufferedWriter(fstream);
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	/**
	 * creates a new device and adds it to the list of devices. if something 
	 * goes wrong false will be returned, true otherwise.
	 * It is used at Presentation Layer, by the REST mechanism to create a new Device
	 * @param cfg the configuration for the device.
	 * @return if something goes wrong false will be returned, true otherwise.
	 */
	public boolean invokeAdd(Map<String, Object> cfg) {
		String name = (String) cfg.get("name");
		
		if (containsDevice(name)) {
			return false;
		}
		
		String clzzName = (String) cfg.get("class");
		Object obj = null;
		Class clz;
		try {
			clz = Class.forName(clzzName);
	    	Constructor ctor = clz.getConstructor(String.class);
	    	obj = ctor.newInstance(name);

		} catch (Exception e) {
			//log.debug("could not invoke: " + e.getMessage());
			return false;
		}
    	
    	if (obj instanceof Device) {
    		// everything is ok...
    		Device dev = (Device) obj;
    		//log.debug("initializing device");
    		// start the device worker thread.
    		dev.init(cfg);
    		addDevice(name, dev,null);
    		Core.getInstance().getGateway().setXMLCacheDirty(true);
    		//log.debug("device successfully created.");
    		return true;
    	} 
    	
    	//log.debug("created object is not device...");
		return false;
	}
	
	/*  check if a Smart Device with ID deviceID is contained in the system */
	public boolean containsDevice(String deviceID){
		
		if(smartDevices.containsKey(deviceID))
			return true;
		else
			return false;
	}

	/* add a new Device in the list of Smart Devices */
	public void addDevice(String key, Device device, String descFileURL){
		
		// add the Device in the list of Devices
		smartDevices.put(key, device);
		
		//get the WADL Service Description File
		if(descFileURL != null){
			URL serverURL = Core.getInstance().getServer().getHostURI();
			descFileURL = serverURL.toString() + descFileURL;
			
			Client client = new Client(
					Core.getInstance().getServer().getRestApplication()
						.getContext().createChildContext(), 
						Protocol.HTTP
					);
			
			org.restlet.data.Response response = client.get(descFileURL);
			System.out.println(descFileURL);
			
			try{
				String xml = (String) response.getEntity().getText();
				WADLparser wadlParser = new WADLparser();

				// Service Discovery and Description from WADL file
				wadlParser.parseWADL(xml);
				List<String> resources    = wadlParser.getResourceNames();
				List<String> resourceDesc = wadlParser.getResourceDescriptions();
				// iterate through Resources
				for(int r=0; r < resources.size(); r++){
					String resourceName = resources.get(r);
					
					// Check if Resource is already in Device's List
					if(deviceContainsResource(device.getDeviceID(), resourceName)){
						continue;
					}

					String resourceDescription = resourceDesc.get(r);
					List<String> meths      = wadlParser.getResourceMethods(resourceName);
					Method[] methods = new Method[meths.size()];
					List<String> methodDesc = null;
					List<String> paramNames = null;
					List<String> paramDesc  = null;
					Map<String, List<String>>  paramOptions = null;
					List<String> paramTypes = null;
					List<String> returnMTypes = null;
					MediaType[]  mediaTypes = null;
					
					// iterate through Resource's Methods
					for(int m=0; m < meths.size(); m++){
						String methodName = meths.get(m);
						methodDesc   = wadlParser.getMethodDescriptions(resourceName, methodName);
						paramNames   = wadlParser.getParameterNames(resourceName, methodName);
						//set Parameter Option Values
						if(paramNames != null){
							for(int p=0; p < paramNames.size(); p++){
								if(wadlParser.getParameterValues(resourceName, methodName, paramNames.get(p)) != null){
									if(paramOptions == null)
										paramOptions = new ConcurrentHashMap<String, List<String>>();
									paramOptions.put(paramNames.get(p), wadlParser.getParameterValues(resourceName, methodName, paramNames.get(p)));
								}		
							}
						}
						paramDesc    = wadlParser.getParameterDescriptions(resourceName, methodName);
						paramTypes   = wadlParser.getParameterTypes(resourceName, methodName);
						returnMTypes = wadlParser.getReturnMediaTypes(resourceName, methodName);
						mediaTypes   = new MediaType[returnMTypes.size()];
						for(int t=0; t < returnMTypes.size(); t++){
							mediaTypes[t] = new MediaType(returnMTypes.get(t));
						}
						methods[m] = new Method(methodName, methodDesc, paramNames, paramDesc, paramTypes, paramOptions, mediaTypes);
					}
					Resource resource = new Resource(resourceName, resourceDescription, methods);
					addResourceToDevice(device.getDeviceID(), resource);
				}
				
				//add the Resources to Device's keywords
				List<String> keywords = Collections.synchronizedList(new LinkedList<String>());
				for(int res=0; res < resources.size(); res++){
					if(!resources.get(res).contains(Constants.STREAMING))
						keywords.add(resources.get(res));
				}			
				if(keywords.size() > 0){
					Core.getInstance().getGateway().setXMLCacheDirty(true);
				}
				getDevice(device.getDeviceID()).getContext().setKeywords(keywords);
				
	    	} catch (SAXParseException e) {
	    		System.err.println("Couldn't parse WADL file for Device:" + device.getDeviceName() + ".");
	    	} catch (IOException ex) {
	    		System.err.println("URL not valid for Device:" + device.getDeviceName() + ".");	
			} catch (Exception e){
				System.err.println("Error while reading Service Description Data for Device:" + device.getDeviceName() + ".");
			    //e.printStackTrace();
			}
		}
	}
	
	/* get a particular Device from the list of Devices based on deviceID */
	public Device getDevice(String deviceID){
		return smartDevices.get(deviceID);
	}

	/* get a list with all the available Smart Devices */
	public Map<String, Device> getAll() {
		return smartDevices;
	}
	
	/* get a list with the names of all the Smart Devices */
	public List<String> getDeviceIDs(){
		Set<String>  devIDs;
		List<String> deviceIDs = Collections.synchronizedList(new LinkedList<String>());
		devIDs = smartDevices.keySet();
		Iterator<String> device = devIDs.iterator();
		while(device.hasNext())
			deviceIDs.add(device.next());
		return deviceIDs;
	}
	
	/* get a list with the names of all the Smart Devices */
	public List<String> getDeviceNames(){

		List<String> deviceNames = Collections.synchronizedList(new LinkedList<String>());
		Collection<Device> dev = smartDevices.values();
		Iterator<Device> curDev = dev.iterator();
		while(curDev.hasNext())
			deviceNames.add(curDev.next().getDeviceName());
		
		return deviceNames;
	}
	
	/* get a list with the names of all the Smart Devices */
	public List<String> getDeviceDescriptions(){
		
		List<String> deviceDesc = Collections.synchronizedList(new LinkedList<String>());
		Collection<Device> dev = smartDevices.values();
		Iterator<Device> curDev = dev.iterator();
		while(curDev.hasNext())
			deviceDesc.add(curDev.next().getDeviceDescription());
		
		return deviceDesc;
	}
	
	/* check in specified time intervals if some Smart Device has failed and delete it from the system if it has no pending requests */
	/* NOTE: This function works only for Sensor Devices' Failures */
	public void removeDevice(String deviceID){
		if(containsDevice(deviceID)){
			System.out.println("Device:" + deviceID + " has been successfully removed from the system.");
			this.smartDevices.remove(deviceID);
		}
	}
	
	/*  add a new Service with name serviceName to device with ID deviceID */
	public void addResourceToDevice(String deviceID, Resource resource){
		if(containsDevice(deviceID)){
			getDevice(deviceID).addResource(resource);
		}
	}
	
	/* check if Smart Device deviceID contains service serviceName in its services list */
	public boolean deviceContainsResource(String deviceID, String serviceName){
		if(!containsDevice(deviceID)){
			return false;
		}
		if(getDevice(deviceID).containsService(serviceName))
			return true;
		else 
			return false;
	}
	
	/*  returns the Services, Smart Device deviceID currently has */
	public List<Resource> getServicesForDevice(String deviceID){
		if(!containsDevice(deviceID)){
			return null;
		}
		Collection<Resource>  servNames;
		List<Resource> serviceNames = Collections.synchronizedList(new LinkedList<Resource>());
		servNames = 	getDevice(deviceID).getServiceValues();
		Iterator<Resource> service = servNames.iterator();
		while(service.hasNext()){
			serviceNames.add(service.next());
		}
		return serviceNames;
	}	
	
	/*  returns the Services, Smart Device deviceID currently has */
	public List<String> getServiceNamesForDevice(String deviceID){
		if(!containsDevice(deviceID)){
			return null;
		}
		Collection<Resource>  servNames;
		List<String> serviceNames = Collections.synchronizedList(new LinkedList<String>());
		servNames = 	getDevice(deviceID).getServiceValues();
		Iterator<Resource> service = servNames.iterator();
		while(service.hasNext())
			serviceNames.add(service.next().getResourceName());
		return serviceNames;
	}
	
	/*  returns the Services, Smart Device deviceID currently has */
	public List<String> getServiceDescriptionsForDevice(String deviceID){
		if(!containsDevice(deviceID)){
			return null;
		}
		Collection<Resource>  servNames;
		List<String> serviceNames = Collections.synchronizedList(new LinkedList<String>());
		servNames = 	getDevice(deviceID).getServiceValues();
		Iterator<Resource> service = servNames.iterator();
		while(service.hasNext())
			serviceNames.add(service.next().getResourceDescription());
		return serviceNames;
	}
	
	/*  returns the number of Services, Smart Device deviceID currently has */
	public int getServicesNumForDevice(String deviceID){
		if(!containsDevice(deviceID)){
			return 0;
		}
		return getDevice(deviceID).getServiceValues().size();
	}
	
	/* returns a specific Service named serviceName that is offered by deviceID */
	public Resource getServiceForDevice(String deviceID, String serviceName){
		if(!containsDevice(deviceID)){
			return null;
		}
		Collection<Resource>  servNames;
		servNames = 	getDevice(deviceID).getServiceValues();
		Iterator<Resource> service = servNames.iterator();
		
		while(service.hasNext()){
			Resource res = service.next();
			if(res.getResourceName().equalsIgnoreCase(serviceName))
				return res;
		}
		
		return null;
	}
	
	/*  returns the Methods (REST verbs) that are offered by serviceName, which is offered by deviceID */
	public List<Method> getMethodsForService(String deviceID, String serviceName){
		if(!containsDevice(deviceID)){
			return null;
		}
		Resource res = getServiceForDevice(deviceID, serviceName);
		if(res != null){
			Method methods[] = res.getMethods();
			List<Method> meth = Collections.synchronizedList(new LinkedList<Method>());
			for(int i=0; i < methods.length; i++)
				meth.add(methods[i]);
			return meth;
		}
		
		return null;
	}
	
	/*  returns the Method Names (REST verbs) that are offered by serviceName, which is offered by deviceID */
	public List<String> getMethodNamesForService(String deviceID, String serviceName){
		if(!containsDevice(deviceID)){
			return null;
		}
		Resource res = getServiceForDevice(deviceID, serviceName);
		if(res != null)
			return res.getMethodVerbs();
		
		return null;
	}
	
	/*  returns a specific Method that is offered by serviceName, which is offered by deviceID */
	public Method getMethodForService(String deviceID, String serviceName, String methodName){
		if(!containsDevice(deviceID)){
			return null;
		}
		Resource res = getServiceForDevice(deviceID, serviceName);
		if(res != null){
			Method methods[] = res.getMethods();
			for(int i=0; i < methods.length; i++)
				if(methods[i].getMethodName().equalsIgnoreCase(methodName))
					return methods[i];
		}
		return null;
	}
	
	/*  returns the Parameter Names, necessary to invoke a specific Method that is offered by serviceName, which is offered by deviceID */
	public List<String> getParameterNamesForMethod(String deviceID, String serviceName, String methodName){

		Method meth = getMethodForService(deviceID, serviceName, methodName);
		if(meth != null)
			return meth.getParameterNames();
			
		return null;
	}
	
	/*  returns the Parameter Types, necessary to invoke a specific Method that is offered by serviceName, which is offered by deviceID */
	public List<String> getParameterTypesForMethod(String deviceID, String serviceName, String methodName){

		Method meth = getMethodForService(deviceID, serviceName, methodName);
		if(meth != null)
			return meth.getParameterTypes();
			
		return null;
	}
	
	/*  returns the Return Types, returned by a specific Method that is offered by serviceName, which is offered by deviceID */
	public List<String> getReturnTypesForMethod(String deviceID, String serviceName, String methodName){

		Method meth = getMethodForService(deviceID, serviceName, methodName);
		if(meth != null){
			MediaType[] mType = meth.getMediaType();
			if(mType != null){
				List<String> mTypes = Collections.synchronizedList(new LinkedList<String>());
				for(int i=0; i < mType.length; i++)
					mTypes.add(mType[i].getName());
				
				return mTypes;
			}
		}
		return null;
	}
	
	public int getMaxPendingRequestsForDevice(String deviceID){
		return getDevice(deviceID).getMaxPendingRequests();
	}
	
	/*  adds a new Request r to Smart Device deviceID in order to be executed by the corresponding Device's Thread */
	public void addRequestToDevice(String deviceID, Request r){
		getDevice(deviceID).addRequest(r);
	}
	
	/*  A Handler to an external third-party Application that wants to make requests to the application framework */
	public Object handleRequestsForDevice(String deviceID, Request r){
		if(r.getCommand().equalsIgnoreCase(Constants.VERB_GET)){
			if(getServiceForDevice(deviceID, r.getServiceName()).hasCachedValue())
				return getServiceForDevice(deviceID, r.getServiceName()).getSavedValue();
			else
				return null;
		}
		else if(r.getCommand().equalsIgnoreCase(Constants.VERB_POST)){
			addRequestToDevice(deviceID,r);
			return new Integer(1);
		}
		else
			return null;
	}
	
	/*  route a response from a Smart Device to the corresponding Thread which controls this particular Device */
	public void addResponseToDevice(String deviceID, Response r){
		if(this.containsDevice(deviceID))
			getDevice(deviceID).addResponse(r);
	}
	
	/* force Smart Device to start being executed as an independent Thread */
	public void startDevice(String deviceID){

		Device dev = getDevice(deviceID);
		Core.getInstance().submitToThreadPool(dev);
	}
	
	/* Failure Masking Mechanism - try to mask requests to Smart Devices which already failed and delete the current Device afterwards */ 
	public void handleFailedRequests(Device currentDevice){
		System.out.println("Handling Requests from Device:" + currentDevice.getDeviceID() + " which failed...");
		Collection<Device> devices = smartDevices.values();
		
		// check current Device for Failure
		if(!currentDevice.isAlive()){
			// if Device has pending requests after its failure, try to mask them by finding similar Services from other Smart Devices
			while(currentDevice.hasFailedRequests()){
				Request r = currentDevice.getNextFailedRequest();
				Iterator<Device>   otherDevice  = devices.iterator();
				Device currentOtherDevice;
				boolean failed = true;
				//seek for other Smart Devices which offer similar Services to the original requests
				while(otherDevice.hasNext()){
					boolean found = false;
					currentOtherDevice = otherDevice.next();
					if(currentOtherDevice.getDeviceID() != currentDevice.getDeviceID()){
						// iterate through each Device's Services to find a similar one
						Collection<Resource> otherServices = currentOtherDevice.getResources().getResources();
						Iterator<Resource>   otherService  = otherServices.iterator();
						Resource otherCurrentService;
						while(otherService.hasNext()){
							otherCurrentService = otherService.next();
							// services match according to their URI and they both have a 'Get' capability
							if(otherCurrentService.getResourceName().contentEquals(r.getServiceName()) && otherCurrentService.isCapable("GET")){
								System.err.println("Device found for masking Failure. Sending request for Service:" + r.getServiceName() + " from Device with ID:"+currentOtherDevice.getDeviceID());
								// change Request and adapt it to be able to be sent to an other Smart Device
								 Request q = new Request(currentOtherDevice.getDeviceID(),r.getServiceName(),r.getCommand(),r.getParameters(),r.getValues(), r.hasFailed(), r.getRequestID());
								// send the Request through the alternative Smart Device
								smartDevices.get(currentOtherDevice.getDeviceID()).addRequest(q);
								found  = true;
								failed = false;
								break;
							}
						}
						if(found == true)
							break;
					}
				}
				if(failed == true){ // failure masking can not be used for specified request	
					String failureMessage = "Requested Device has failed. Additionally Failure Masking (Device) Mechanism can not be used for Service:" + r.getServiceName();
					currentDevice.handleNoResponse(r, failureMessage);
				}
				currentDevice.removeFailedRequest(r);
			}
		}
	}

	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}
	
}
