package simulation.multiClientsScenario;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Protocol;

import controlLayer.Core;
import controlLayer.Server;
import controlLayer.libraryCode.Constants;
import deviceLayer.Device;
import deviceLayer.Devices;
import deviceLayer.info.Resource;

public class MultiClientsScenario_UniformDist implements  Runnable{

	static final int NUMBER_OF_CLIENTS       = 120;					// number of concurrent users on the system
	static final int NUMBER_OF_DEVICES		 = 2;					// number of Smart Devices connected to the Smart Gateway
	static final int NUM_REQUESTS            = NUMBER_OF_CLIENTS;
	static final int MAX_SIMULATION_TIME     = 180000;				// 1 minutes simulation time
	static final int MINUTE				     = 60000;
	
	static int  requestCounter    = 0;
	static long totalResponseTime = 0;
	static int  successfulRequest = 0;
	static int  failedRequest	  = 0;
	
	static FileWriter fstream;
	static BufferedWriter resultFile;
	
	/** the random number generated initialized with a seed. */
	public static final Random random = new Random(System.currentTimeMillis()); 
	
	/** the server instance. */
	private Server server = null;
	
	/** the thread pool. */
	private ThreadPoolExecutor pool = (ThreadPoolExecutor) java.util.concurrent.Executors.newCachedThreadPool();
	
	/**
	 * submit a runnable to the thread pool maintained by the core.
	 * @param runnable the runnable to execute.
	 * @return returns the future value returned by the thread pool.
	 */
	public Future<?> submitToThreadPool(Runnable runnable) {
		//log.debug("processing runnable in thread pool");
		return pool.submit(runnable);
	}
	
	public boolean isEmptyPool(){
		
		if(pool.getActiveCount() > 0){
			return false;
		}
		return true;
	}
	
	public MultiClientsScenario_UniformDist(Server server){
		this.server = server;
		
		try{
			String fileName = "tinyResponseTimesD" + NUMBER_OF_DEVICES + "C" + NUMBER_OF_CLIENTS + "R" + Constants.DEV_REQUEST_INTERVAL + ".txt";
			fstream = new FileWriter(fileName, true);
			resultFile = new BufferedWriter(fstream);
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	
	public Server getServer() {
		return server;
	}
	
		
	public void run() {
		
		String gatewayUrl = "http://localhost:8080";
		String deviceID   = null;
		String service    = null; 

		// wake an initial amount of time, until discovery process is over
		try {
			Thread.sleep(15000);	// awake after 15 seconds
			System.err.println("MultiClients Scenario: Initializing Testing Procedure...");
		} catch (InterruptedException e) {
			System.out.println("received interrupt.");
		}
		
		if (null == gatewayUrl) {
			System.err.println("MultiClientsScenario Error: Gateway url is null");
			return;
		} 
		
		Devices moteDevices = controlLayer.Core.getInstance().getDevices();
		
		long startTime 		  = System.currentTimeMillis();
		long currentTime	  = System.currentTimeMillis();
		long currentMinute,startMinute;
		int  clientRequestNum = 0;
		
		List<String> smartDevices = moteDevices.getDeviceIDs();
		
		if(smartDevices.size() == 0){
			System.err.println("MultiClients Scenario: No Devices found. Exiting...");
		}
		else if(smartDevices.size() != NUMBER_OF_DEVICES){
			System.err.println("MultiClients Scenario: Devices found not equal targeted number in simulation. Exiting...");
		}
		//String[] smartDevices = {"fec0:0:0:0:0:0:0:a", "fec0:0:0:0:0:0:0:9", "fec0:0:0:0:0:0:0:c", "fec0:0:0:0:0:0:0:d"};
		//String[] smartDevices = {"fec0:0:0:0:0:0:0:9"};
		
		while((currentTime - startTime) <= MAX_SIMULATION_TIME){	// for the duration of the simulation
				
			List<String> currentDevs = moteDevices.getDeviceIDs();
			if(currentDevs.size() == 0){
				System.err.println("MultiClients Scenario: All available devices failed during simulation. Exiting...");
				break;
			}
		
			ClientRequest restRequest[] = new ClientRequest[NUMBER_OF_CLIENTS];
			int    requestSendingTime[] = new int[NUMBER_OF_CLIENTS];

			for(int i=0; i < NUMBER_OF_CLIENTS; i++){
				
				// initialize Clients currently using the system as well as their dedicated REST Threads
				restRequest[i] = new ClientRequest(getServer(), resultFile);

				// initialize sending time of a client request during the current minute (random second in time)
				requestSendingTime[i] = Math.abs(random.nextInt()) % 60;
			}
			
			// for the current minute of the simulation, iterate until all clients send a random request to a random device
			clientRequestNum = 0;
			startMinute      = System.currentTimeMillis();
			currentMinute	 = System.currentTimeMillis(); 
			while(clientRequestNum < NUM_REQUESTS){
				for(int i=0; i < NUMBER_OF_CLIENTS; i++){
					if(requestSendingTime[i] != -1){
					if((currentMinute - startMinute) / 1000 >  requestSendingTime[i]){ //time expired to send the request
						deviceID = smartDevices.get(Math.abs(random.nextInt()) % smartDevices.size()); 
						//deviceID = smartDevices[Math.abs(random.nextInt()) % smartDevices.length]; 
						List<Resource> services = moteDevices.getServicesForDevice(deviceID);
						if(services != null){
							try{
								Resource resource =  services.get(Math.abs(random.nextInt()) % services.size());
								service = resource.getResourceName();
								List<String> RESTverbs = resource.getMethodVerbs();
								String RESTverb = RESTverbs.get(Math.abs(random.nextInt()) % RESTverbs.size());
								while(RESTverb == null)
									RESTverb = RESTverbs.get(Math.abs(random.nextInt()) % 2);

								System.err.println("MultiClients Scenario: Client:" + i +" is sendind a " + RESTverb + " request to device " + deviceID +
																	" for service:" + service +" in " + requestSendingTime[i] + " second...");
						
								// create the REST Threads and send the requests to the devices
								if(RESTverb.equalsIgnoreCase(Constants.VERB_GET)){
									// create the url
									String url = gatewayUrl + "/devices/" + deviceID + "/" + service  + "/";
									restRequest[i].setRequest (url);
								}
								else if(RESTverb.equalsIgnoreCase(Constants.VERB_POST)){
									// create the url
									String url = gatewayUrl + "/devices/" + deviceID + "/" + service + "/";
									Form form = new Form();
									int color = Math.abs(random.nextInt()) % 3;
									if(color == 0)
										form.add("color", String.format("%c",'R'));
									else if(color == 1)
										form.add("color", String.format("%c",'G'));
									else
										form.add("color", String.format("%c",'B'));
									restRequest[i].setRequest(url, form);
								}
								else{
									System.err.println("MultiClients Scenario: Error: An unknown verb is used to access a resource.");
									continue;
								}
								clientRequestNum++;
								submitToThreadPool(restRequest[i]);
								requestSendingTime[i] = -1;
							} catch (java.lang.ArithmeticException e){
								// do nothing - go the next request!
							}
						}
					}
					}
				}
				currentMinute	 = System.currentTimeMillis(); 
			}
			
			// all clients sent their requests in this minute
			if(clientRequestNum == NUM_REQUESTS){
				System.err.println("MultiClients Scenario: Waiting for minute time to complete");
				// wait until this minute has passed
				while(currentMinute - startMinute < MINUTE){
					currentMinute	 = System.currentTimeMillis();
				}
			}
			
			currentTime = System.currentTimeMillis();
		}
		
		System.err.println("MultiClients Scenario: Checking if there remain any pending requests in request queues...");
		// sleep a last, small amount of time, in case the last response takes longer than usual to come
		boolean msg = false;
		while(!isEmptyPool()){
			if(msg == false)
				System.err.println("MultiClients Scenario: Waiting some additional time for requests to finish execution...");
			msg = true;
		}
	
		System.err.println("MultiClients Scenario: Finished successfully!");
		System.err.println("MultiClients Scenario: Writing results to files...");
		
		try {
			int maxPendingReqNum = 0;
			int currentReqNum;
			for(int i=0; i < smartDevices.size(); i++){
				//currentReqNum = moteDevices.getMaxPendingRequestsForDevice(smartDevices.get(i));
				currentReqNum = moteDevices.getMaxPendingRequestsForDevice(smartDevices.get(i));

				if(currentReqNum > maxPendingReqNum)
					maxPendingReqNum = currentReqNum;
			}
			
			resultFile.newLine();
			resultFile.newLine();
			resultFile.append("Final Results:");	resultFile.newLine();
			resultFile.append("For " + NUMBER_OF_CLIENTS + " web clients, " + NUMBER_OF_DEVICES + " Smart Devices and " + MAX_SIMULATION_TIME/60000 + " minutes simulation time...");	
			resultFile.newLine();
			resultFile.append("Request Queue Time Interval:" + ((double)Constants.DEV_REQUEST_INTERVAL)/1000.0 + " seconds"); resultFile.newLine();
			resultFile.append("Request Retransmission Attempts:" + Constants.DEV_REQUEST_MAX_ATTEMPTS); resultFile.newLine();
			resultFile.append("Request Queues Maximum pending Request number during test:" + maxPendingReqNum);	resultFile.newLine();
			resultFile.append("Cache Time Validity:" + Constants.DEV_MAX_CACHE_DELAY_TIME / 1000 + " seconds");	resultFile.newLine();
			resultFile.append("Total Client Requests:" + requestCounter); resultFile.newLine();
			resultFile.append("Successful Responses:" + successfulRequest + "("+ (double)(successfulRequest / (double)requestCounter)*100.0 + "%)");
			resultFile.newLine();
			resultFile.append("Failed Responses:" + (requestCounter - successfulRequest) + "("+ (double)((requestCounter - successfulRequest) / (double)requestCounter)*100.0 + "%)");
			resultFile.newLine();
			resultFile.append("Total Transmission Attempts:" + Device.totalTransmissionAttempts); 
			resultFile.newLine();
			double failedPercentage = ((double) Device.failedTransmissionAttempts / (double) Device.totalTransmissionAttempts ) * 100;
			resultFile.append("Failed Transmission Attempts:" + Device.failedTransmissionAttempts + " (" + failedPercentage +"%) "); 
			resultFile.newLine();
			double cachePercentage = (double) Device.cacheSuccess / (double) requestCounter;
			resultFile.append("Cache Success:" + Device.cacheSuccess + " (" + cachePercentage +"%) "); resultFile.newLine();
			resultFile.append("Mean Response Time:" + ((double)totalResponseTime / (double) successfulRequest) + " (Only for successful Requests)");
			
			resultFile.close();
			Devices.reqQueueTimesFile.close();
			
			// close request queue sizes file
			List<String> allDevs = moteDevices.getDeviceIDs();
			for(int i=0; i< allDevs.size(); i++){
				moteDevices.getDevice(allDevs.get(i)).msgQueue.closeRequestQueueSizeFile();
			}
			
			System.err.println("MultiClients Scenario: Exiting...");
			Core.getInstance().shutdown();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}