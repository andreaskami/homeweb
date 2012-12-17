package deviceLayer.sensorMotes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import deviceLayer.Device;
import deviceLayer.Devices;
import deviceLayer.Driver;
import deviceLayer.Request;
import deviceLayer.Response;

import controlLayer.Core;
import controlLayer.libraryCode.Constants;


public class TinyosIPv6Driver extends Driver implements Runnable{

	String subnetAddress;
	String multicastAddress;
	int    multicastPort;
	
	public TinyosIPv6Driver(Devices devices) {
		super(devices);
		
	}

    /* Main entry point: used for IP-enabled tinyos initializations, should be called only once, at the beginning! */
	public void startDevice(){
		
		// set the subnet address
		subnetAddress = "fec0";
		
		// set the multicasting parameters
		multicastAddress = "ff02::1";
		multicastPort    = 10000;

	}
	
	@Override
	public void sendMessage(String nodeid, char message_type, String content)
			throws IOException {
		
		// THROUGH THE TERMINAL
		//String command = "curl -g 'http://[fec0::6]/Illumination'";
		//String[]   cmd = command.split(" ");
		//Process pr = Runtime.getRuntime().exec(cmd);
		//BufferedReader buf = new BufferedReader(new InputStreamReader (pr.getInputStream()));
		//String line;
		//String response = "";
		//while( ( line=buf.readLine()) != null){
			//response += line;
		//}
		//System.out.println(response);
		
		// THROUGH A SOCKET
		/*InetAddress ia = InetAddress.getByName("fec0::6");
		 Socket socket     = null;
		 PrintWriter out   = null;
		 BufferedReader in = null;
		 String resp 	   = null;
		 
		 try {

			 socket = new Socket(ia, 80);
		     out = new PrintWriter(socket.getOutputStream(), true);

		     out.println("/Illumination");
		     in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		     if(in.ready())
		     	resp = in.readLine();
		     else
		    	 resp = "0";
		     //while( (line= in.readLine()) != null){
		    	// System.out.println("line");
				 //resp += line;
		     //} 
		  } catch (UnknownHostException e) {
		     System.err.println("Don't know about host " + deviceID  + ".");
		     System.exit(1);
		  } catch (IOException e) {
		     System.err.println("Couldn't get I/O for " + "the connection to: " + deviceID  + ".");
		     System.exit(1);
		  } 
		  out.close();
		  in.close();
		  socket.close();
		  */
	}

	@Override
	public void sendMessage(String deviceID, char message_type, Request request)
			throws IOException {

		String service  = request.getServiceName();
		String output = "";
		try {	
			// case of POST request
			if(request.getCommand().equalsIgnoreCase(Constants.VERB_POST)){
				String payload ="";
				// Construct payload data
				for(int i=0; i < request.getParameters().size(); i++){
					if(i == 0)
						payload = URLEncoder.encode(request.getParameters().get(i), "UTF-8") + "=" + URLEncoder.encode((String)request.getValues().get(i), "UTF-8"); 
					else
						payload += "&" + URLEncoder.encode(request.getParameters().get(i), "UTF-8") + "=" + URLEncoder.encode((String)request.getValues().get(i), "UTF-8"); 
				}
				
				URL url = new URL("http://[" + deviceID + "]/" + service); 
				URLConnection conn = url.openConnection(); 
				conn.setDoOutput(true); 
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); 
				wr.write(payload); 
				wr.flush(); 
				
				// Get the response 
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
				String line; 
				
				while ((line = rd.readLine()) != null) { 
					// Process line...
					output += line;
				} 
				
				wr.close(); 
				rd.close(); 
			}
			else if(request.getCommand().equalsIgnoreCase(Constants.VERB_GET)){
				
				Calendar calendar = Calendar.getInstance();
				Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());

				System.err.println("[" + currentTimestamp.toString() + "]: Service request sent to " + deviceID);
				
				// Create a URL for the desired page 
				URL url = new URL("http://[" + deviceID + "]/" + service); 
				// Read all the text returned by the server 
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream())); 
				String str; 
		
				while ((str = in.readLine()) != null) { 
					output += str;
				}
				in.close(); 
			}
				
		} catch (Exception e) { 
		
		}

		char[] checkVal =output.toCharArray();
		String correctOutput = "";
		for(int i=0; i < checkVal.length; i++){
			if(Character.isDigit(checkVal[i]))
					correctOutput += checkVal[i];
		}
		if(correctOutput == "")
			correctOutput = "0";
		
		Response r  = new Response(deviceID, service, request.getCommand(), new Integer(correctOutput));
		
		// add Response to Smart Device's Response Message Queue
		devices.addResponseToDevice(deviceID, r);
		Calendar calendar = Calendar.getInstance();
		Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());

		System.err.println("[" + currentTimestamp.toString() + "]: Service:" + r.getServiceName() + " added to Response Queue with data:" + r.getResult());

	}
	
	/** listen for broadcasting messages from IP-enabled sensor devices */
	public void run(){

        InetAddress ia = null;
        byte[] buffer = new byte[65535];
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
        int port = 0;

        // read the address from the command line
        try{
            try {
                ia = InetAddress.getByName(multicastAddress);
            }
            catch (UnknownHostException e) {
                System.err.println(e);
            }
            port = multicastPort;
        }// end try
        catch (Exception e) {
            System.err.println(e);
        }

        try{
            MulticastSocket ms = new MulticastSocket(port);
            ms.joinGroup(ia);

    		// add virtual Devices for Simulation reasons
    		List<String> keywords = new LinkedList<String>();
    		
            devices.addDevice("fec0:0:0:0:0:0:0:7", new Device("fec0:0:0:0:0:0:0:7", "fec0:0:0:0:0:0:0:7", "Telosb Sensor Mote", "Kitchen", keywords, this), "/wadl/tinyosIP6/");
            devices.startDevice("fec0:0:0:0:0:0:0:7");
            devices.addDevice("fec0:0:0:0:0:0:0:8", new Device("fec0:0:0:0:0:0:0:8", "fec0:0:0:0:0:0:0:8", "Telosb Sensor Mote", "Bedroom", keywords, this), "/wadl/tinyosIP6/");
            devices.startDevice("fec0:0:0:0:0:0:0:8");
            devices.addDevice("fec0:0:0:0:0:0:0:9", new Device("fec0:0:0:0:0:0:0:9", "fec0:0:0:0:0:0:0:9", "Telosb Sensor Mote", "Living Room", keywords, this), "/wadl/tinyosIP6/");
            devices.startDevice("fec0:0:0:0:0:0:0:9");
            devices.addDevice("fec0:0:0:0:0:0:0:a", new Device("fec0:0:0:0:0:0:0:a", "fec0:0:0:0:0:0:0:a", "Telosb Sensor Mote", "Living Room", keywords, this), "/wadl/tinyosIP6/");
            devices.startDevice("fec0:0:0:0:0:0:0:a");
            
            while (true) {
                ms.receive(dp);
                String payload = new String(dp.getData(),0,dp.getLength());
                System.out.println("IPv6 Multicast Message:" + payload);
                
                // get IPv6 address
                String deviceID = dp.getAddress().getHostAddress();
                int subnetIndex = deviceID.indexOf(":");
                deviceID = subnetAddress + deviceID.substring(subnetIndex);
                
                // parse the device description data
				TinyOSParser parser = new TinyOSParser();
				parser.parseDeviceData(deviceID, payload);
				
				// check for duplicate device entry
				if(devices.containsDevice(deviceID) == false){ // new Device found
					Time currentTime = new Time(System.currentTimeMillis());
					System.err.println("[" + currentTime.toString() + "]: Creating new Device in Gateways's list...");
					// add Smart Device in Smart Gateway's Device list
					devices.addDevice(deviceID, 
							new Device(deviceID,parser.getDevName(), parser.getDeviceDescription(), parser.getDeviceLocation(), parser.getKeywords(), this), parser.getWADLurl());
					// start Smart Device to operate as an independent Thread
					devices.startDevice(deviceID);
				}
				else{
					System.out.println("Device with IP address:" + deviceID + " already added to the system.");
				}
       
            }
        }
        catch (SocketException se) {
            System.err.println(se);
        }
        catch (IOException ie) {
            System.err.println(ie);
        }
    }

}
