package simulation.TestingMultipleClients;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Response;

import controlLayer.Server;
import deviceLayer.Devices;
import deviceLayer.Request;

public class TestingMultipleClients implements  Runnable{

	static final int NUMBER_OF_CLIENTS       = 100;					// number of concurrent users on the system
	static final int NUMBER_OF_DEVICES		 = 1;					// number of Smart Devices connected to the Smart Gateway
	
	private Server server = null;
	
	public TestingMultipleClients(Server server){
		this.server = server;
	}
	
	public Server getServer() {
		return server;
	}
	
	public void run() {
		
		String gatewayUrl = "http://localhost:8080";
		
		// wake an initial amount of time, until discovery process is over
		try {
			Thread.sleep(30000);	// awake after 10 seconds
			System.err.println("Testing Scenario: Initializing Testing Procedure...");
		} catch (InterruptedException e) {
			System.out.println("received interrupt.");
		}
		
		Client client[]     = new Client[NUMBER_OF_CLIENTS];
		ClientRequest restRequest[] = new ClientRequest[NUMBER_OF_CLIENTS]; 
		
		String deviceID = "fec0:0:0:0:0:0:0:5";
		String service  = "Temperature";
		String url = gatewayUrl + "/devices/" + deviceID + "/" + service  + "/";
		
		Devices moteDevices = controlLayer.Core.getInstance().getDevices();
		List<String> currentDevs = moteDevices.getDeviceIDs();
		while(currentDevs.size() == 0){
			// waiting for devices to be discovered
		}
		
		for(int i=0; i < NUMBER_OF_CLIENTS; i++){
			//client[i] 	   = new Client(getServer().getRestApplication().getContext().createChildContext(), Protocol.HTTP);
			//restRequest[i] = new ClientRequest(client[i]);
			//restRequest[i].setRequest (url);
			//restRequest[i].run();
			
			List<String> parameters       = Collections.synchronizedList(new LinkedList<String>());
			List<Object> values           = Collections.synchronizedList(new LinkedList<Object>());
			Request r = new Request(deviceID, service, "GET", parameters, values, false, 0  );
			moteDevices.getDevice(deviceID).addRequest(r);
		}
		
	}
	
}
