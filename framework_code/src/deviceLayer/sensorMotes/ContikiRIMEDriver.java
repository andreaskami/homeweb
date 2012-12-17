package deviceLayer.sensorMotes;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Time;

import deviceLayer.Device;
import deviceLayer.Devices;
import deviceLayer.Driver;
import deviceLayer.Request;
import deviceLayer.Response;


//class responsible for communication and interaction with Smart Devices equipped with Contiki Operating System 
public class ContikiRIMEDriver extends Driver {

	// Contiki Specific Characteristics
	private PrintWriter      sensorDataOutput;
	private ContikiSerialConn serialConnection;

	// Contiki Constructor
	public ContikiRIMEDriver(Devices devices, String comPort){
		super(devices);

		serialConnection = new ContikiSerialConn() {
			@Override
			protected void serialData(String line) throws IOException{
				// System.out.println("Sensor Data Received:" + line);
				messageReceived(line);
			}
		};  
    
		if (comPort != null) {
			serialConnection.setComPort(comPort);
		}
	}
  
    /* Main entry point: used for Contiki initializations, should be called only once, at the beginning! */
	public void startDevice(){
		connectToSerial();
	}

	/* responsible for connection with the sensor which operates as a Sink through serial */
	protected void connectToSerial() {
		if (!serialConnection.isOpen()) {
			String comPort = serialConnection.getComPort();
			if (comPort == null) {
				System.out.println("No Communication Port defined");
				//comPort = MoteFinder.selectComPort(window);
			}
			if(comPort != null) {
				serialConnection.open(comPort);
			}
		}
	}

	/* necessary clean-ups made at the end of operation, should be called at the end of the program */
	private void exit() {
		/* TODO Clean up resources */
		if (serialConnection != null) {
			serialConnection.close();
		}
		PrintWriter output = this.sensorDataOutput;
		if (output != null) {
			output.close();
		}
		System.exit(0);
	}

	// Serial communication

	/*  the module which is responsible for sending a message in DISCOVERY and DESCRIPTION mode */
	public void sendMessage(String nodeid, char message_type, String data) {
		if (serialConnection != null && serialConnection.isOpen()) {
			String message = nodeid + " " + (int)message_type + " " + data;
			serialConnection.writeSerialData(message);
		}
	}
  
	/* the module which is responsible for sending a Service Request message in OPERATION mode */
	public void sendMessage(String nodeid, char message_type, Request req) throws IOException{
  	 
		if (serialConnection != null && serialConnection.isOpen()) {
			switch(message_type){
		  	  	case 'R':{ 	// Service Request message
		  		  	ContikiParser parser = new ContikiParser();
		  		    String data = parser.parseRequestData(req);
		  		  	String message = nodeid +" " + (int)message_type + " " + data;
		  		  	serialConnection.writeSerialData(message);
		  		  	break;
		  	  	}
		  	  	default:{
		          	System.err.println("Unknown message subject. Sending failed."); 
		  	  	}
			}
		}
	}

	// the event-triggered procedure of receiving a new message from a Smart Device
	public synchronized void messageReceived(String line) throws IOException{
	
		ContikiParser parser = new ContikiParser();
		parser.parseSensorData(line);
		char subject   = parser.getMessageSubject();
		String nodeID  = parser.getDeviceID();
		String data    = parser.getMessageParsedData();
		
		if (nodeID != null) {	// message successfully parsed
			switch(subject){
				case 'H':{  // hello message
					sendMessage(nodeID, 'H', "");
					break; 
				}
				case 'D':{  // device description message
					parser.parseDeviceData(data);
					// check for duplicate device entry
					if(devices.containsDevice(nodeID) == false){ // new Device found
						Time currentTime = new Time(System.currentTimeMillis());
    					System.err.println("[" + currentTime.toString() + "]: Creating new Device in Gateways's list...");
						// add Smart Device in Smart Gateway's Device list
						devices.addDevice(nodeID, new Device(nodeID,parser.getDeviceName(), parser.getDeviceDescription(),"", parser.getKeywords(), this), parser.getWADLurl());
						// start Smart Device to operate as an independent Thread
						devices.startDevice(nodeID);
					}
					else{ // device already added in system
						System.err.println("Error: Device already exists! Not created...");	
					}
					sendMessage(nodeID, 'A', "");
					break;
				}
				case 'R':{ // Service Response message
					Response r  = parser.parseResponseData(data);
					r.setDeviceID(nodeID);
					// add Response to Smart Device's Response Message Queue
					devices.addResponseToDevice(nodeID, r);
					Time currentTime = new Time(System.currentTimeMillis());
					System.err.println("[" + currentTime.toString() + "]: Service:" + r.getServiceName() + " added to Response Queue with data:" + r.getResult());
					break;
				}   
				default:    // Unknown message received. Ignored
				}
		}
		else{
			//Invalid message received. No response to sensor.
		}
		return;
	}

}
