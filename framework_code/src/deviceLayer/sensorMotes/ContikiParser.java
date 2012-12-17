package deviceLayer.sensorMotes;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import deviceLayer.Request;
import deviceLayer.Response;

// a parser for Contiki Driver, used to encode and decode Messages, in appropriate formats, from and to the Smart Device
public class ContikiParser{

	private String 					deviceID   = "";
	private String					deviceName = "";
	private String					deviceDescription;
	private String					WADLurl;
	private String					mimeType;
	private String 					data;
	private char					subject;
	private List<String> 	 		keywords;	

	// Contiki Parser Constructor
	ContikiParser(){
		this.keywords       = Collections.synchronizedList(new LinkedList<String>());
		
	}
	
	/*  split an incoming message from a Sensor Device into DeviceID, subject and data */
	public void parseSensorData(String line) throws IOException{
		try{
			if(line != null && line.length() > 8 && Character.isDigit(line.charAt(0)) && !line.contentEquals(" ") && !line.contentEquals("")){
				int nodeIdPos = line.indexOf(" ");
				String nodeID = line.substring(0, nodeIdPos);
				// this command is executed in case Contiki is version 2.2.2 or older
				//this.deviceName  = mapNodeID(new Integer(nodeID).intValue());
				this.deviceID  = nodeID;
				
				int subjectPos = line.indexOf(" ", nodeIdPos + 1); 
				String subject = line.substring(nodeIdPos + 1, subjectPos);
				subject.trim();
				this.subject   = (char) new Integer(subject).intValue();
				this.data      = line.substring(subjectPos + 1);
			}
		} catch(Exception e){
		
		}
	}
	
	/* translate DeviceID into its decimal value */
	/* Note: Only for Contiki versions 2.2.2 or older */
	private String mapNodeID(int nodeID) {
		return "" + (nodeID & 0xff) + '.' + ((nodeID >> 8) & 0xff);
	}
	
	/*  parse incoming data concerning Device Description Information */
	public void parseDeviceData(String data) throws IOException{
		try{
			data		= data.trim();
			int index 	= data.indexOf(" ");
			this.deviceName = data.substring(0, index) + this.deviceID;
			int index2 	= data.indexOf(" ", index+1);
			this.deviceDescription = data.substring(index+1, index2);
			index 		= data.indexOf(" ", index2+1); 

			if(index > index2)
				this.WADLurl = "http://" + data.substring(index2+1, index);
			else
				this.WADLurl = "http://" + data.substring(index2+1);
			
			
		} catch(Exception e){
			
		}
	}
	
	/*  parse outgoing data concerning Request Message, transform it in a form understood by Smart Device's underlined Contiki Driver */
	public String parseRequestData(Request req){
		String resource = req.getServiceName();
		String verb = req.getCommand();
		
		String data = resource + " " + verb + " ";
		for(int i=0; i < req.getParameters().size(); i++){
			data += req.getParameters().get(i).toString() + " " +req.getValues().get(i).toString();
			if(i != req.getParameters().size()-1)
				data += " ";
		}

		return data;
	}

	/*  parse incoming data concerning Response Message, transform it in a universal form understood by Smart Gateway */
	public Response parseResponseData(String data) throws IOException{
		
		Response r    = new Response();
		
		data		  = data.trim();
		int index 	  = data.indexOf(" ");

		String serviceName = data.substring(0, index);
		int index2 	= data.indexOf(" ", index+1);
		String command = data.substring(index+1, index2);
		r.setServiceName(serviceName);
		r.setCommand(command);
		
		try{
			index = serviceName.length() + command.length() + 2;
			
			String result = data.substring(index, data.length());
			result.trim();
			char[] res = result.toCharArray();
			boolean valid = true;
			
			// check if all the value characters are numbers
			for(int i=0; i < res.length; i++)
				if(!Character.isDigit(res[i]))
					valid = false;
			
			if(valid == true)
				r.setResult(Integer.parseInt(result));
			else
				r.setResult(new Integer(0));
		} catch(Exception e){
			
		}
		
		return r;
	}
	
	/*  map the Resource's return type to its appropriate MIME type*/
	private void mapMimeType(String returnType){
		
		if(returnType.equalsIgnoreCase("int"))
			this.mimeType = "text/plain";
		else if(returnType.equalsIgnoreCase("float"))
			this.mimeType = "text/plain";
		else if(returnType.equalsIgnoreCase("double"))
			this.mimeType = "text/plain";
		else if(returnType.equalsIgnoreCase("bool"))
			this.mimeType = "text/plain";
		else
			this.mimeType = returnType;
	}
	
	// Getter Functions
	public String getDeviceID(){
		return deviceID;
	}
	
	public String getDeviceName(){
		return deviceName;
	}
	
	public String getDeviceDescription(){
		return deviceDescription;
	}
	
	public String getWADLurl(){
		return WADLurl;
	}
	
	public List<String> getKeywords(){	
		return this.keywords;
	}

	public char getMessageSubject(){
		return this.subject;
	}
	
	public String getMessageParsedData(){
		return this.data;
	}

}