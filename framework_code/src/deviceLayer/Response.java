package deviceLayer;

// it represents the Response type and format, made from Smart Devices as answers to Internet Clients Requests
public class Response {
	
	private String deviceID;	// Identity of the Smart Device which answered the request
	private String serviceName;	// Service which was previously requested for execution
	private String command;		// Command operated
	private Object result;		// Result of Service invocation, return value
	private long   requestID;	// used to identify the Thread which caused this request to be sent to the Smart Device, 
								// in order to send the Response back to the appropriate Internet Client who requested it
	
	// default constructor
	public Response(){}
	
	public Response(String deviceID, String serviceName, String command, Object result){
		this.deviceID    = deviceID;
		this.serviceName = serviceName;
		this.command     = command;
		this.result		 = result;
	}
	
	// Getter & Setter Functions
	
	public void setRequestID(long requestID){
		this.requestID = requestID;
	}

	public long getRequestID(){
		return this.requestID;
	}
	
	// mark the Response with the corresponding Smart Device, from where it was produced
	public void setDeviceID(String deviceID){
		this.deviceID = deviceID;
	}
	
	public String getDeviceID(){
		return deviceID;
	}
	
	public void setServiceName(String serviceName){
		this.serviceName = serviceName;
	}
	
	public String getServiceName(){
		return serviceName;
	}
	
	public void setCommand(String command){
		this.command = command;
	}
	
	public String getCommand(){
		return command;
	}
	
	public void setResult(Object result){
		this.result = result;
	}
	
	public Object getResult(){
		return result;
	}
}