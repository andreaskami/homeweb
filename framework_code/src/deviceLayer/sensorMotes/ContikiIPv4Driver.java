package deviceLayer.sensorMotes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Time;

import deviceLayer.Devices;
import deviceLayer.Driver;
import deviceLayer.Request;
import deviceLayer.Response;

public class ContikiIPv4Driver extends Driver{

	public ContikiIPv4Driver(Devices devices) {
		super(devices);
		// TODO Auto-generated constructor stub
	}

    /* Main entry point: used for IP-enabled Contiki initializations, should be called only once, at the beginning! */
	public void startDevice(){
		// listen for broadcasting messages from IP-enabled sensor devices
	}
	
	@Override
	public void sendMessage(String nodeid, char message_type, String content)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessage(String nodeid, char message_type, Request request)
			throws IOException {
		String command = "curl -s -X GET 172.16.20.253:8080/helloworld -i";
		String[]   cmd = command.split(" ");
		Process pr = Runtime.getRuntime().exec(cmd);
		BufferedReader buf = new BufferedReader(new InputStreamReader (pr.getInputStream()));
		String line;
		String response = "";
		while( ( line=buf.readLine()) != null){
			response += line;
		}
		Response r  = new Response(request.getDeviceID(), request.getServiceName(), request.getCommand(), response);
		r.setDeviceID(request.getDeviceID());
		// add Response to Smart Device's Response Message Queue
		devices.addResponseToDevice(request.getDeviceID(), r);
		Time currentTime = new Time(System.currentTimeMillis());
		System.err.println("[" + currentTime.toString() + "]: Service:" + r.getServiceName() + " added to Response Queue with data:" + r.getResult());
		
	}

}
