package deviceLayer.sensorMotes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import deviceLayer.Request;
import deviceLayer.Response;



//a parser for tinyOS Driver, used to encode and decode Messages, in appropriate formats, from and to the Smart Device
public class TinyOSParser{

	private String 					deviceID   = "";
	private String					deviceName = "";
	private String					deviceDescription;
	private String					location;
	private String					WADLurl;
	private String					mimeType;
	private List<String> 	 		keywords;	

	// tinyOS Parser Constructor
	TinyOSParser(){
		this.keywords       = Collections.synchronizedList(new LinkedList<String>());
	}
	
	/*  parse incoming data concerning Device Description Information */
	public void parseDeviceData(String nodeid, String data){
		data		    = data.trim();
		int index 	    = data.indexOf(" ");
		this.deviceID  += nodeid;
		this.deviceName = data.substring(0, index) + this.deviceID.charAt(nodeid.length()-1);
		int index2 	    = data.indexOf(" ", index+1);
		this.deviceDescription = data.substring(index+1, index2);
		index 		    = data.indexOf(" ", index2+1); 
		this.location   = data.substring(index2+1, index);
		index2 		    = data.indexOf(" ", index+1); 

		if(index2 > index)
			this.WADLurl =  data.substring(index+1, index2);
		else
			this.WADLurl = data.substring(index+1);
	}
	
	/*  parse outgoing data concerning Request Message, transform it in a form understood by Smart Device's underlined tinyOS Driver */
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
	public Response parseResponseData(short[] data){
		Response r    = new Response();
		char[] cdata = new char[data.length];
		for(int i=0; i< data.length; i++)
			cdata[i] = (char)data[i];
		String content = new String(cdata);
		
		content		  = content.trim();
		int index 	  = content.indexOf(" ");

		String serviceName = content.substring(0, index);
		int index2 	= content.indexOf(" ", index+1);
		String command = content.substring(index+1, index2);
		r.setServiceName(serviceName);
		r.setCommand(command);

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
	
	public String getDevName(){
		return deviceName;
	}
	
	public String getDeviceDescription(){	
		return deviceDescription;
	}
	
	public String getDeviceLocation(){	
		return location;
	}
	
	public String getWADLurl(){
		return WADLurl;
	}
	
	public List<String> getKeywords(){	
		return this.keywords;
	}
	
	public String getMimeType(){
		return mimeType;
	}
    
}