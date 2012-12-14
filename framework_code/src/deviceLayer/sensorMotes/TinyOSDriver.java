package deviceLayer.sensorMotes;

import java.io.IOException;
import java.sql.Time;

import net.tinyos.message.Message;
import net.tinyos.message.MessageListener;
import net.tinyos.message.MoteIF;
import net.tinyos.util.PrintStreamMessenger;

import deviceLayer.Device;
import deviceLayer.Devices;
import deviceLayer.Driver;
import deviceLayer.Request;
import deviceLayer.Response;
import deviceLayer.info.Resource;


// class responsible for communication and interaction with Smart Devices equipped with tinyOS 
public class TinyOSDriver extends Driver implements MessageListener{

	// static definitions
	private static final int TINYOS_GATEWAY_ID = 1;
	
	// tinyOS Specific Characteristics
	private MoteIF   mote;				 // connects to Base Station sensor
	private short    gateway_tinyOS_id;  // identity of the Base Station for tinyOS communication
	
	// tinyOS constructor
	public TinyOSDriver(Devices devices){
		super(devices);
		gateway_tinyOS_id = TINYOS_GATEWAY_ID;
	}
	
    /* Main entry point: used for tinyOS initializations, should be called only once, at the beginning! */
	public void startDevice() {
    	mote = new MoteIF(PrintStreamMessenger.err);
    	mote.registerListener(new SmartDeviceMsg(), this);
    }
	
    /* receive a message from a sensor mote */
    public synchronized void messageReceived(int dest_addr, Message msg){
    	if (msg instanceof SmartDeviceMsg) {
    		SmartDeviceMsg smsg = (SmartDeviceMsg)msg;

    		/* extract message's received data */
    		int nodeid    = (int) smsg.get_nodeid();
    		char subject  = (char)smsg.get_subject();
    		short[] sdata = smsg.get_data(); 
    		
    		switch(subject){
    			case 'H':{  // hello message
    				Time currentTime = new Time(System.currentTimeMillis());
					System.err.println("[" + currentTime.toString() + "]: Hello Msg successfully received from mote:" + nodeid + ".");    			
    				try{ // reply with a Hello message indicating Smart Gateway's existence
    					this.sendMessage(String.valueOf(nodeid), 'H', new String());	
    				} catch (IOException e) {
    					System.err.println("Cannot send message to mote");
    					e.printStackTrace();
    				}
    				break;
    			}
    			case 'D':{  // device description message
    				System.err.println("Device Description Msg successfully received from mote:" + nodeid + ".");
    				char[] cdata = new char[sdata.length];
    				for(int i=0; i< sdata.length; i++)
    					cdata[i] = (char)sdata[i];
    				String data = new String(cdata);
    				TinyOSParser parser = new TinyOSParser();
    				parser.parseDeviceData(new Integer(nodeid).toString(), data);
    				// check for duplicate device entry
    				if(devices.containsDevice(new Integer(nodeid).toString()) == false){ // new Device found
    					Time currentTime = new Time(System.currentTimeMillis());
    					System.err.println("[" + currentTime.toString() + "]: Creating new Device in Gateways's list...");
    					// add Smart Device in Smart Gateway's Device list
    					devices.addDevice(new Integer(nodeid).toString(), 
    							new Device(new Integer(nodeid).toString(),parser.getDevName(), parser.getDeviceDescription(), "", parser.getKeywords(), this), parser.getWADLurl());
    					// start Smart Device to operate as an independent Thread
    					devices.startDevice(new Integer(nodeid).toString());
    				}
    				else{ // device already added in system
    					System.err.println("Error: Device already exists! Not created...");	
    				}
    				try{ // reply with an Acknowledgment message indicating successful binding with Smart Gateway
    					this.sendMessage(String.valueOf(nodeid), 'A', new String());	
    					Time currentTime = new Time(System.currentTimeMillis());
    					System.err.println("[" + currentTime.toString() + "]: Acknlowledgment Message sent to mote:" + nodeid + ".");    			
    				} catch (IOException e) {
    					System.err.println("Cannot send message to mote");
    					e.printStackTrace();
    				}
    				break;
    			}
    			case 'R':{ // Service Response message
    				TinyOSParser parser = new TinyOSParser();
    				Response r  = parser.parseResponseData(sdata); 
    				int result  = (int)smsg.get_result();
    				r.setDeviceID(new Integer(nodeid).toString());
    				r.setResult(new Integer(result));
    				// add Response to Smart Device's Response Message Queue
    				devices.addResponseToDevice(new Integer(nodeid).toString(), r);
    				Time currentTime = new Time(System.currentTimeMillis());
					System.err.println("[" + currentTime.toString() + "]: Service:" + r.getServiceName() + " added to Response Queue with data:" + r.getResult());
    				break;
    			}    		
    			default:
    				System.err.println("Unknown message received from mote:" + nodeid + ".");
    			}
    		}
    }

    /* the module which is responsible for sending a message in DISCOVERY and DESCRIPTION mode */
    public void sendMessage(String nodeid, char message_type, String content) throws IOException{
    	SmartDeviceMsg smsg = new SmartDeviceMsg();
    	smsg.set_nodeid(gateway_tinyOS_id);
    	smsg.set_subject((short) message_type);

    	switch(message_type){
    		case 'H':{ 	// Hello reply to sensor mote indicating Smart Gateway's existence
		
    			try {
    				System.err.println("Replying to mote...");  
    				synchronized(mote){
    					mote.send(Integer.parseInt(nodeid), smsg);
    				}
    				System.err.println("Hello Msg successfully sent from PC with id:" + gateway_tinyOS_id);	
    			} catch (IOException e) {
    				System.err.println("Cannot send Hello message to mote");
    				e.printStackTrace();
    			}
    			break;
    		}
    		case 'A':{ 	// Acknowledgement message to sensor mote indicating successful binding with Smart Gateway
		
    			try {
    				System.err.println("Replying to mote...");  
    				synchronized(mote){
    					mote.send(Integer.parseInt(nodeid), smsg);
    				}
    				System.err.println("Ack Msg successfully sent from PC with id:" + gateway_tinyOS_id);	
    			} catch (IOException e) {
    				System.err.println("Cannot send Ack message to mote");
    				e.printStackTrace();
    			}
    			break;
    		}	  		
    		default:{
    			System.err.println("Unknown message subject. Sending failed."); 
    		}
    	}
    }
    
    /* the module which is responsible for sending a Service Request message in OPERATION mode */
    public void sendMessage(String nodeid, char message_type, Request req) throws IOException{
    	String content;
    	TinyOSParser parser = new TinyOSParser();
    	SmartDeviceMsg smsg = new SmartDeviceMsg();
    	smsg.set_nodeid(gateway_tinyOS_id);
    	smsg.set_subject((short) message_type);
    	content = parser.parseRequestData(req);
    	
    	switch(message_type){
    	  
    		case 'R':{ 	// Service Request message
    			try {
    				char[] cdata = content.toCharArray();
    				short[] sdata = new short[cdata.length];
    				for(int i=0; i< cdata.length; i++)
    					sdata[i] = (short) cdata[i];
    				smsg.set_data(sdata);
    				synchronized(mote){
    					mote.send(Integer.parseInt(nodeid), smsg);
    				}
    			} catch (IOException e) {
    				System.err.println("Cannot send Service Request Message to mote");
    				e.printStackTrace();
    			}
    			break;
    		}
    		default:{
    			System.err.println("Unknown message subject. Sending failed."); 
    		}
    	}
    }
   
}
