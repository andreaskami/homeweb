package deviceLayer.smartMeters;
import gnu.io.*;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import controlLayer.Core;
import controlLayer.libraryCode.Constants;
import controlLayer.libraryCode.DatabaseHandler;
import deviceLayer.Device;
import deviceLayer.Devices;
import deviceLayer.Driver;
import deviceLayer.Request;
import deviceLayer.Response;
	
//import com.ibm.mqtt.MqttException;

	

/**
 * This is the class that contains the main program of
 * Plogg Monitor application
 * @Author: Massimo Dore
 * March 2009
 * @version 3.0
 * @changelog Added support for external parameters file 26/03/2009.
 * 			  Using a simpler parser to get values
 * 
 */
 
	public class PloggMonitor extends Driver implements SerialPortEventListener, Runnable{
		
		 boolean 	   debug = true;
		 ParseBuffer   ps = new ParseBuffer();
		 String 	   comPort;
	     OutputStream  out;
	     MediaCalc 	   cal = null;
	     DecimalFormat df1;
		 
		public PloggMonitor(Devices devices, String commPort){
				super(devices);
				this.comPort = commPort;
	            this.df1 	 =   new DecimalFormat("####0.00");
		}
		 
		 public void startDevice (){			 
			    String osname = System.getProperty("os.name","").toLowerCase();
			    String serialPort;
			    if ( osname.startsWith("windows") ) {
			    	// windows
			    	serialPort = comPort;
			    } else if (osname.startsWith("linux")) {
			    	// linux
			    	serialPort = "/dev/tty" + comPort;
			    } else if ( osname.startsWith("mac") ) {
			    	// mac
			    	serialPort = "????";
			    } else {
			    	System.out.println("Invalid port specification");
			    	return;
			    }
	        	
	        	System.out.println("Energy Monitor by Massimo Dore. 2009 Release 3.0");
		        try
		        {
		            connect(serialPort); 
		        }
		        catch ( Exception e )
		        {
		            // TODO Auto-generated catch block
		            e.printStackTrace();
		        }
		    }
		 
		 /* Handle the serial port connection */
		void connect ( String portName ) throws Exception
	    {
	        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
	        if ( portIdentifier.isCurrentlyOwned() )
	        {
	            System.out.println("Error: Port is currently in use");
	        }
	        else
	        {
	            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
	            
	            if ( commPort instanceof SerialPort )
	            {
	                SerialPort serialPort = (SerialPort) commPort;
	                /*
	                 * Telegesis ETRX2 USB suggested serial port parameters 
	                 */
	                serialPort.setSerialPortParams(19200,
	                		SerialPort.DATABITS_8,
	                		SerialPort.STOPBITS_1,
	                		SerialPort.PARITY_NONE);
	                
	                InputStream in = serialPort.getInputStream();
	                out = serialPort.getOutputStream();
	                
	                /*
	                 * Start Serial Port write thread
	                 */               
                
	                serialPort.addEventListener(new SerialReader(in, ps));
	                serialPort.notifyOnDataAvailable(true);
	            }
	            else
	            {
	                System.out.println("Error: Only serial ports are handled by this example.");
	            }
	        }  
	       
	    }	
		
		@Override
		public void sendMessage(String nodeid, char messageType, Request request) throws IOException {
			
			String id = request.getDeviceID();	
			
	    	switch(messageType){
	    	
    			case 'R':{ 	// Service Request message
    				
    				if(request.getServiceName().equalsIgnoreCase(Constants.RESOURCE_ALIVE)){ // Aliveness check
        		        // prepare response
        		        String res = Constants.ACK;
        				Response r  = new Response(id, request.getServiceName(), request.getCommand(), res); 
        				// add Response to Smart Device's Response Message Queue
        				devices.addResponseToDevice(id, r);
        				long endTime = System.currentTimeMillis();
    					System.err.println("[" + endTime  + "]: Service:" + r.getServiceName() + " added to Response Queue with data:" + r.getResult());
        			}	
    				else{ // normal request
    					String cmd;
    					int retry = 0;
        		
    					cmd = "AT+UCAST:"+id+"=sv\r\n";
    					retry = 0;
    					do{
    						try {
    							writecmd(cmd);
    						} catch (InterruptedException e) {
    							// TODO Auto-generated catch block
    							e.printStackTrace();
    						}       			
    		        	
    						/* sometimes the cmd has no answer, I retry 3 times before failing and passing to next device*/
    						retry++;
    						if (retry > 2) 
    							break;
    					} while (!ps.enddata);
    		        	
    					// prepare response
    					String res = this.JSONprint(ps.getHash(), id);
    					Response r  = new Response(id, request.getServiceName(), request.getCommand(), res); 
    					// add Response to Smart Device's Response Message Queue
    					devices.addResponseToDevice(id, r);
    					long endTime = System.currentTimeMillis();
    					System.err.println("[" + endTime  + "]: Service:" + r.getServiceName() + " added to Response Queue with data:" + r.getResult());
    				}				
    					break;
    			}
	    	}
		}

		@Override
		public void sendMessage(String nodeid, char messageType, String content)
				throws IOException {
			// TODO Auto-generated method stub	
		}
		
	    /**
	      * discoverDevices() function scan for Plogg devices
	      * Because the answer to command "AT+SN" is asyncronous and undefined on total number of
	      * devices discovered, I wait for MAXWAITFORDEVICES milliseconds before submit other commands. 
	      */
	    public void discoverDevices() throws InterruptedException{
	        	
	        boolean debug = false;
	        ps.cmddata = false;
	        	
	        // inventory starts.....
	        String cmd = "AT+SN\r\n";
	        writecmd(cmd);
	        System.out.println("Device inventory...please wait");
	        	
	        Thread.sleep(Constants.MAXWAITFORMETERS);
	        /*
	         * Now get the Plogg name
	        */
	        	
	        int retry = 0;
	        ps.cmddata = true; // now preformat the string
	        System.out.println("Found  "+ps.NumDevices()+ " devices.");
	        	        	
	        Set<String> st = ps.Hdev.keySet();
	        Iterator<String> itr = st.iterator();
	            
	        while (itr.hasNext()){
	        	String id = itr.next();	   
	           
	           	// Add newly found Plogg Smart Meters in Gateway's lists
	   			if(devices.containsDevice(id) == false){ // new Device found
	   				Time currentTime = new Time(System.currentTimeMillis());
	   				String deviceName = "Plogg" + id.substring(9);
	   				List<String> 	 		keywords;	
	   				keywords       = Collections.synchronizedList(new LinkedList<String>());
	   				keywords.add("Electricity");
	   				System.err.println("[" + currentTime.toString() + "]: Creating new Plogg Smart Meter in Gateways's list...");
	   				// add Smart Device in Smart Gateway's Device list
	   				devices.addDevice(id, 
	   						new Device(id, deviceName, "Plogg Smart Meter", "", keywords, this), "http://localhost:8080/wadl/plogg/");
	   				// start Plogg Smart Meter to operate as an independent Thread
	   				devices.startDevice(id);
	   			}
	   			else{ // device already added in system
	   				System.err.println("Error: Plogg Smart Meter already exists! Not created...");	
	   			}
	   			if (debug) 
	        	   System.out.println("Ask device "+id);
	           
	   			cmd = "AT+UCAST:"+id+"=v\r\n";
	        	retry = 0;
	        	do{
	        		writecmd(cmd);    
	        		Thread.sleep(5000); // 5 secs
	        		/*
	        		 * sometimes the cmd has no answer, I retry 3 times before failing and passing
	        		 * to next device
	        		 */
	        		retry++;
	        		if (retry > 2) 
	        			break;
	        	} while (!ps.enddata);
	        }
	    }
	   
		   /**
	     *  Format all data to JSON string format
	     *  @param ht is the hash table that contains the Plogg devices information
	     */
	   public String JSONprint(Hashtable<String, PloggDevices> ht, String ploggID){
	       	PloggDevices pd;
	        String JsonStr = "[";
	
	        int i = 0;

	        float kwmean = 0f;
	        float h = 0f;
	        String numero;

	        pd = ht.get(ploggID);
	        if (i > 0) 
	        	JsonStr = JsonStr +",";
	        JsonStr = JsonStr + "{\"ID\":\""+pd.getId()+"\",";
	        JsonStr = JsonStr + "\"NAME\":\""+pd.GetName()+"\",";
	        JsonStr = JsonStr + "\"TIMESTAMP\":\""+pd.GetTime()+"\",";
	        JsonStr = JsonStr + "\"WATTS\": "+ pd.GetWatts()+",";
	        try {
	        	h = 0;
	        	if (!(pd.GetUpTime().length() == 0)){
	        		cal = new MediaCalc(pd.GetUpTime());
	        		h = cal.CalcHours();
	        	}
	        	if (h > 0){
	        		if (!(pd.GetCWatts().length() == 0))
	        			kwmean = Float.parseFloat(pd.GetCWatts()) /  cal.CalcHours();
	        		else kwmean = 0f;
	        		
	        		numero = df1.format(kwmean * 1000);
	        			
	        		/*  replace comma with point*/
	        		JsonStr = JsonStr + "\"KWHM\": "+ numero.replace(',', '.') +",";
	        	}
	        	else 
	        		JsonStr = JsonStr + "\"KWHM\": 0 "+",";
	        } catch (NumberFormatException e){
	        	e.printStackTrace();
	        };
	        
	        JsonStr = JsonStr + "\"KWH\": "+pd.GetCWatts()+"}";
	       
	        JsonStr = JsonStr + "]";

	        return JsonStr;
	   }
	       
	   /**
	     *  Format all data to JSON string format
	     *  @param ht is the hash table that contains the Plogg devices information
	     */
	   public String JSONprintAll(Hashtable<String, PloggDevices> ht){
	       	PloggDevices pd;
	        String JsonStr = "[";
	        	      
	        Set<String> st = ht.keySet();
	        Iterator<String> itr = st.iterator();
	        int i = 0;
	        while (itr.hasNext()){
	        	float kwmean = 0f;
	        	float h = 0f;
	        	String numero;
	        	String elem = itr.next();
	        	pd = ht.get(elem);
	        	if (i > 0) 
	        		JsonStr = JsonStr +",";
	        	JsonStr = JsonStr + "{\"ID\":\""+pd.getId()+"\",";
	        	JsonStr = JsonStr + "\"NAME\":\""+pd.GetName()+"\",";
	        	JsonStr = JsonStr + "\"TIMESTAMP\":\""+pd.GetTime()+"\",";
	        	JsonStr = JsonStr + "\"WATTS\": "+ pd.GetWatts()+",";
	        	try {
	        		h = 0;
	        		if (!(pd.GetUpTime().length() == 0)){
	        			cal = new MediaCalc(pd.GetUpTime());
	        			h = cal.CalcHours();
	        		}
	        		if (h > 0){
	        			if (!(pd.GetCWatts().length() == 0))
	        			     kwmean = Float.parseFloat(pd.GetCWatts()) /  cal.CalcHours();
	        			else kwmean = 0f;
	        			numero = df1.format(kwmean * 1000);
	        			/*
	        			*  replace comma with point
	        			*/
	        			JsonStr = JsonStr + "\"KWHM\": "+ numero.replace(',', '.') +",";
	        		}
	        		else JsonStr = JsonStr + "\"KWHM\": 0 "+",";
	        	} catch (NumberFormatException e){
	        		e.printStackTrace();
	        		};
	        		JsonStr = JsonStr + "\"KWH\": "+pd.GetCWatts()+"}";
	        	}
	        	JsonStr = JsonStr + "]";

	        	return JsonStr;
	   }
	    
	   /**
	     *  Read the current measures from devices
	     */
	   public void getValues() throws InterruptedException{
	        		        	
	        String cmd;
	        int retry = 0;
	        Set<String> st = ps.Hdev.keySet();
	        Iterator<String> itr = st.iterator();
	        while (itr.hasNext()){
	        	String id = itr.next();	        		
	        	cmd = "AT+UCAST:"+id+"=sv\r\n";
	        	retry = 0;
	        	do{
	        		writecmd(cmd);       			
	        		/**
	        		 * sometimes the cmd has no answer, I retry 3 times before failing and passing
	        		 * to next device
	        		 */
                     retry++;
                     if (retry > 2) 
                    	 break;
	        	} while (!ps.enddata);
	        }
	   }
	        	
	   /**
	     * Write a command to serial port
	     * @param cmd the String command to be send
	     */
	   public void writecmd(String cmd) throws InterruptedException{
	        try {
				this.out.write(cmd.getBytes());
					
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   }
	   
	   /**
	     * The main thread method.
	     * 1) discover the Plogg devices
	     * 2) get the measurement from devices 
	     * 3) sleep(some seconds)
	     * 4) repeat cycle 3-4
	     * @see java.lang.Runnable#run()
	     */
	   public void run (){	        	
	        		
			String cmd = null; // no specific command has been issued
	        try {
	            // initialize and discover Devices
	        	discoverDevices();

			} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
			
	        /* Loop forever*/					
	        while (true){
	             /* If an external command has been received, manage it else do default getValues() */
	             //TODO: set command for cmd
	             if (cmd == null)
	                try {
	                	/* a sample every minute */
						Thread.sleep(Constants.METER_STREAMING_DELAY);
						this.getValues();
							
						// prepare a JSON string and print it out
						String res = this.JSONprintAll(ps.getHash());
						System.out.println(res);
							
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
	              }	           
	        }

	  /**
	   * Serial thread reader
	   * Read the Serial port
	   */
		 public static class SerialReader implements SerialPortEventListener {
		        private InputStream in;
		        boolean debug = false; //debugging
		        
		        ParseBuffer ps ;
		        String bf;
		        
		        /**
		         * 
		         * @param in serial in
		         * @param ps1 token parser class pointer
		         */
		        public SerialReader ( InputStream in, ParseBuffer ps1 )
		        {
		            this.in = in;
		            this.ps = ps1;
		            this.bf = "";
		        }
		        
		        /**
		         * Event class handler
		         * @see gnu.io.SerialPortEventListener#serialEvent(gnu.io.SerialPortEvent)
		         */
		        public void serialEvent(SerialPortEvent arg0) {
		        	byte[] buffer = new byte[1024];
		        	PrepString pStr = new PrepString();
		            int len = -1;
		            try{
		                while ( ( len = this.in.read(buffer)) > -1 )
		                {
		                	bf = new String(buffer,0,len);
		                	if (ps.cmddata == false){
		                		// new Plogg Device found
		                		if (len > 0){
		                			ps.ParseDevs(this.bf);
		                			if (debug)
		                				System.out.print("----->"+ this.bf+"<--"+len);
		                		}
		                	} 
		                	else{
		                		// A new Energy consumption Measurement
		                		pStr.getText(bf.replaceAll("\r\n", ""));
		                		if (pStr.isEndString()){
		                			ps.exec(pStr.printStr(), pStr.getId());
		                			pStr.init();
		                			ps.enddata = true;
		                		}
		                		else{
		                			if (debug) System.out.println(pStr.printStr());
		                		}
		                	}
		                }
		            }
		            catch ( IOException e )
		            {
		                e.printStackTrace();
		            }            
		        
		        }
		       
		    
		        /**
		         * Parse the message coming from Plogg devices and 
		         * manage device structures
		         */
	 
	
		 }
	
    //@Override
	public void serialEvent(SerialPortEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}