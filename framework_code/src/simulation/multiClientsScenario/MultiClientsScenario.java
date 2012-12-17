package simulation.multiClientsScenario;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
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
import deviceLayer.MessageQueue;
import deviceLayer.info.Resource;

public class MultiClientsScenario implements  Runnable{

	static final int 	NUMBER_OF_CLIENTS       = 90;					// number of concurrent users on the system
	static final int 	NUMBER_OF_DEVICES		= 4;					// number of Smart Devices connected to the Smart Gateway
	static final int 	NUM_REQUESTS            = NUMBER_OF_CLIENTS;
	static final int	MAX_SIMULATION_MINUTES  = 4;					// simulation time in minutes
	static final int	MINUTE				    = 60000;
	static final int	MAX_SIMULATION_SECONDS  = (MAX_SIMULATION_MINUTES * MINUTE) / 1000;	//  simulation time in seconds
	static final int 	TOTAL_CLIENTS           = NUMBER_OF_CLIENTS * MAX_SIMULATION_MINUTES;	// number of total users during the simulation
	static final double LAMBDA 					= (double)MAX_SIMULATION_SECONDS / (double)TOTAL_CLIENTS;
	
	static int  requestCounter    = 0;
	static long totalResponseTime = 0;
	static int  successfulRequest = 0;
	static int  failedRequest	  = 0;
	
	public static int  totalWaitingTime    = 0;
	public static int  counterWaitingTimes = 0;
	
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
	
	public MultiClientsScenario(Server server){
		this.server = server;
		
		try{
			String fileName = "tinyResponseTimesD" + NUMBER_OF_DEVICES + "C" + NUMBER_OF_CLIENTS + "R" + Constants.DEV_REQUEST_INTERVAL + ".txt";
			fstream = new FileWriter(fileName, true);
			resultFile = new BufferedWriter(fstream);
		}catch (Exception e){//Catch exception if any
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	
	public Server getServer() {
		return server;
	}
	
    /**
     * Return real number uniformly in [0, 1).
     */
    public static double uniform() {
        return random.nextDouble();
    }
	
    /**
     * Return an integer with a Poisson distribution with mean lambda.
     */
    public static double poisson(double lambda) {
        // using algorithm given by Knuth
        // see http://en.wikipedia.org/wiki/Poisson_distribution
        int k = 0;
        double p = 1.0;
        double L = Math.exp(-lambda);
        do {
            k++;
            p *= uniform();
        } while (p >= L);
        return k-1;
    }
    
    /**
     * Return a real number from an exponential distribution with rate lambda.
     */
    public static double exp(double lambda) {
        return -Math.log(uniform()) * lambda;  // Matlab exprnd uses log(uniform) while the other version log(1-uniform)
    	//return lambda * Math.exp(-lambda * uniform());
    }
	
		
	public void run() {
		
		String gatewayUrl = "http://localhost:8080";
		String deviceID   = null;
		String service    = null; 

		// wake an initial amount of time, until discovery process is over
		try {
			Thread.sleep(10000);	// awake after 30 seconds
			System.err.println("MultiClients Scenario: Initializing Testing Procedure...");
		} catch (InterruptedException e) {
			System.out.println("received interrupt.");
		}
		
		if (null == gatewayUrl) {
			System.err.println("MultiClientsScenario Error: Gateway url is null");
			return;
		} 
		
		Devices moteDevices = controlLayer.Core.getInstance().getDevices();
		
		long currentSecond,startSecond;
		
		List<String> smartDevices = moteDevices.getDeviceIDs();
		
		if(smartDevices.size() == 0){
			System.err.println("MultiClients Scenario: No Devices found. Exiting...");
			//System.exit(0);
		}
		else if(smartDevices.size() != NUMBER_OF_DEVICES){
			System.err.println("MultiClients Scenario: Devices found not equal targeted number in simulation. Exiting...");
			// System.exit(0);
		}
		//String[] smartDevices = {"fec0:0:0:0:0:0:0:a", "fec0:0:0:0:0:0:0:9", "fec0:0:0:0:0:0:0:c", "fec0:0:0:0:0:0:0:d"};
		//String[] smartDevices = {"fec0:0:0:0:0:0:0:9"};
		
		ClientRequest restRequest[] = new ClientRequest[TOTAL_CLIENTS];
		double requestSendingTime[] = new double[TOTAL_CLIENTS];
		double slot = 0;
		double CUMULATIVE_LAMBDA = 0;

		for(int i=0; i < TOTAL_CLIENTS; i++){
			
			// initialize Clients currently using the system as well as their dedicated REST Threads
			restRequest[i] = new ClientRequest(getServer(), resultFile);

			// initialize sending time of a client request during the current minute (random second in time)
			slot = exp(LAMBDA);
			CUMULATIVE_LAMBDA += slot;
			requestSendingTime[i] = CUMULATIVE_LAMBDA;
			// System.err.println(i + ") " + CUMULATIVE_LAMBDA);
		}

		int clientsCounter = 0;
		startSecond      = System.currentTimeMillis();
		
		while(clientsCounter < TOTAL_CLIENTS){	// for the duration of the simulation - until all clients are satisfied
				
			List<String> currentDevs = moteDevices.getDeviceIDs();
			if(currentDevs.size() == 0){
				System.err.println("MultiClients Scenario: All available devices failed during simulation. Exiting...");
				break;
			}

			currentSecond	 = System.currentTimeMillis();
			//long timePassed  = currentSecond - startSecond;
			//System.out.println("TIME PASSED:" + timePassed + " SECONDS:" + timePassed / 1000.0);
			if((currentSecond - startSecond) / 1000.0 >  requestSendingTime[clientsCounter]){ //time expired to send the request
				
				// use a LOAD BALANCER to select device to satisfy the request
				List<String> allDevs       = moteDevices.getDeviceIDs();
				List<String> zeroQueueDevs = new ArrayList<String>();
				int currentSize    = 0;
				int minSize        = 1000;
				for(int i=0; i< allDevs.size(); i++){
					currentSize = moteDevices.getDevice(allDevs.get(i)).msgQueue.getCurrentRequestQueueSize();
					if(currentSize == 0)
						zeroQueueDevs.add(allDevs.get(i));
					if(currentSize < minSize){
						minSize  = currentSize;
						deviceID = allDevs.get(i);
					}	
				}
				// if more than 1 devices have zero queue, then select randomly a device with zero queue
				if(zeroQueueDevs.size() > 1){
					deviceID = zeroQueueDevs.get(Math.abs(random.nextInt()) % zeroQueueDevs.size()); 
				}
				
				// select RANDOMLY a device to satisfy the request
				// deviceID = smartDevices.get(Math.abs(random.nextInt()) % smartDevices.size()); 
				
				
				List<Resource> services = moteDevices.getServicesForDevice(deviceID);
				if(services != null){
					try{
						Resource resource =  services.get(Math.abs(random.nextInt()) % services.size());
						service = resource.getResourceName();
						List<String> RESTverbs = resource.getMethodVerbs();
						String RESTverb = RESTverbs.get(Math.abs(random.nextInt()) % RESTverbs.size());
						while(RESTverb == null)
							RESTverb = RESTverbs.get(Math.abs(random.nextInt()) % 2);

						System.err.println("MultiClients Scenario: Client:" + clientsCounter +" is sendind a " + RESTverb + " request to device " + deviceID +
																		" for service:" + service +" in " + requestSendingTime[clientsCounter] + " second...");
						
						// create the REST Threads and send the requests to the devices
						if(RESTverb.equalsIgnoreCase(Constants.VERB_GET)){
							// create the url
							String url = gatewayUrl + "/devices/" + deviceID + "/" + service  + "/";
							restRequest[clientsCounter].setRequest (url);
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
							restRequest[clientsCounter].setRequest(url, form);
						}
						else{
							System.err.println("MultiClients Scenario: Error: An unknown verb is used to access a resource.");
							continue;
						}

						submitToThreadPool(restRequest[clientsCounter]);
						clientsCounter++;

					} catch (java.lang.ArithmeticException e){
						// do nothing - go the next request!
					}
				}
			}
		}
		
		// System.err.println("MultiClients Scenario: Checking if there remain any pending requests in request queues...");
		// sleep a last, small amount of time, in case the last response takes longer than usual to come
		// boolean msg = false;
		// while(!isEmptyPool()){
		//	if(msg == false)
		//		System.err.println("MultiClients Scenario: Waiting some additional time for requests to finish execution...");
		//	msg = true;
		// }
	
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
			resultFile.append("For " + NUMBER_OF_CLIENTS + " web clients, " + NUMBER_OF_DEVICES + " Smart Devices and " + MAX_SIMULATION_MINUTES + " minutes simulation time...");	
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
			double cachePercentage = ( (double) Device.cacheSuccess / (double) requestCounter ) * 100;
			resultFile.append("Cache Success:" + Device.cacheSuccess + " (" + cachePercentage +"%) "); resultFile.newLine();
		
			double avgWaitingTime = (double) totalWaitingTime / (double) counterWaitingTimes;
			resultFile.append("Average Waiting Time:" + avgWaitingTime); resultFile.newLine();
			
			if(Constants.PRIORITIES == true){
				double highPriorityPercentage = ( (double) MessageQueue.totalHighPriorityRequests / (double) requestCounter ) * 100;
				resultFile.append("High Priority Requests:" + MessageQueue.totalHighPriorityRequests + " (" + highPriorityPercentage +"%) "); resultFile.newLine();
				double normalPriorityPercentage = ( (double) MessageQueue.totalNormalPriorityRequests / (double) requestCounter) * 100;
				resultFile.append("Normal Priority Requests:" + MessageQueue.totalNormalPriorityRequests + " (" + normalPriorityPercentage +"%) "); resultFile.newLine();
				double lowPriorityPercentage = ( (double) MessageQueue.totalLowPriorityRequests / (double) requestCounter) * 100;
				resultFile.append("Low Priority Requests:" + MessageQueue.totalLowPriorityRequests + " (" + lowPriorityPercentage +"%) "); resultFile.newLine();
			}
				
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