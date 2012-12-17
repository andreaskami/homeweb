package deviceLayer;
import java.util.List;

// it represents the Request type and format, made from Internet Clients to Smart Devices
public class Request {
	
	private String 	     deviceID;		// Identity of the Device responsible for answering the request
	private String 	     serviceName;	// Service requested for execution
	private String 	     command;		// Command to be operated
	private List<String> parameters;	// Parameters concerning proper service invocation
	private List<Object> values;		// Values of the parameters
	private int	 		 attempt;		// number of attempts to send a request to the Smart Device (used for Robustness and Reliability issues)
	private boolean		 failed;		// used to identify that the request was made from a failed Device and forwarded to one with same capabilities
	private long		 requestID;		// used to identify the Thread which caused this request to be sent to the Smart Device, 									// in order to send the Response back to the appropriate Internet Client who requested it
	
	public long enqueueTime;
	public long dequeueTime;
	public long predictedTime;
	public int  requestQueueSize;
	public int  priority;
	public int  dequeuePriority;
	public String priorityCategory;

	// Request Constructor
	public Request(String devID, String servName, String command, List<String> parameters,	List<Object> values, boolean failed, long requestID){
		this.deviceID    = devID;
		this.serviceName = servName;
		this.command     = command;
		this.parameters  = parameters;
		this.values      = values;
		this.attempt     = 0;	// initial attempts to send the request to Smart Device
		this.failed		 = failed;
		this.requestID   = requestID;
		this.enqueueTime = 0;
		this.dequeueTime = 0;
		this.requestQueueSize = 0;
		this.priority = 0;
		this.dequeuePriority = 0;
		this.priorityCategory = "Normal";
	}
	
	// check if this Request matches to the Response received from Smart Device
	public boolean checkMatching(Response r){
		
		if(r.getDeviceID() == null || r.getServiceName() == null || r.getCommand() == null){
			System.out.println("REQUEST CHECKING FAILED");
			return false;
		}
			
		if(this.deviceID.contentEquals(r.getDeviceID())  && this.serviceName.contentEquals(r.getServiceName() )&& this.command.contentEquals(r.getCommand()))
			return true;
		else
			return false;
	}
	
	/* increment number of attempts to transmit the Request to Smart Device */
	public void incAttempts(){
		attempt++;
	}
	
	/* return number of previous attempts to send the Request to Smart Device */
	public int getAttempts(){
		return this.attempt;
	}
	
	/* check if it a request made from the (Device) Failure Masking Mechanism */
	public boolean hasFailed(){
		return this.failed;
	}
	
	/* indicate that the Request is forwarded to (Device) Failure Masking Mechanism */
	public void setFailed(){
		this.failed = true;
	}

	// Getter & Setter Functions
	
	public long getRequestID(){
		return this.requestID;
	}
	
	public void setRequestID(long reqID){
		this.requestID = reqID;
	}
	
	public String getDeviceID(){
		return this.deviceID;
	}
	
	public String getServiceName(){
		return this.serviceName;
	}
	
	public String getCommand(){
		return this.command;
	}
	
	public List<String> getParameters(){
		return this.parameters;
	}
	
	public List<Object> getValues(){
		return this.values;
	}
	
	public Object getValueForParameter(String param){
		for(int p=0; p < parameters.size(); p++)
			if(parameters.get(p).equalsIgnoreCase(param))
				return values.get(p);
		return null;
	}
}