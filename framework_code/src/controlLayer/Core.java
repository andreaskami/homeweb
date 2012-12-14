package controlLayer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import simulation.TestingMultipleClients.TestingMultipleClients;
import simulation.multiClientsScenario.MultiClientsScenario;
import simulation.multiClientsScenario.MultiClientsScenario_UniformDist;

import controlLayer.eventing.AsynchronousEventReceiver;
import controlLayer.eventing.Eventing;
import controlLayer.libraryCode.Cache;
import controlLayer.libraryCode.Constants;
import controlLayer.libraryCode.DatabaseHandler;

import deviceLayer.Device;
import deviceLayer.Devices;
import deviceLayer.Driver;
import deviceLayer.Sensaris.SimpleRead;
import deviceLayer.Sensaris.senspodDevice;
import deviceLayer.info.Context;
import deviceLayer.info.SymbolicLocation;
import deviceLayer.sensorMotes.ContikiRIMEDriver;
import deviceLayer.sensorMotes.ContikiIPv4Driver;
import deviceLayer.sensorMotes.TinyOSDriver;
import deviceLayer.sensorMotes.TinyosIPv6Driver;
import deviceLayer.smartMeters.PloggMonitor;

//import ch.ethz.inf.vs.gateway.translator.TranslatorManager;
//import ch.ethz.inf.vs.gateway.commons.exception.translator.TranslatorException;

/**
 * the core acts the main dispatcher of the whole gateway.
 *
 */
public class Core {

	/** the thread pool. */
	private ExecutorService pool = java.util.concurrent.
			Executors.newCachedThreadPool();
	
	/** the standard devices registry plugin. */
	private static Devices devices = null;
	
	/** the server instance. */
	private Server server = null;
	
	/** the name of this gateway. */
	private String gatewayName 	  = "";
	
	/** the location of this gateway. */
	private String gatewayLocation = "";
	
	/** a cache service for xml, html, plain-text representations of devices */
	private Cache cache;
	
	/** the standard eventing plugin. */
	private Eventing eventManagement = null;
	
	/** the event sink for all the devices on the gateway. */
	private AsynchronousEventReceiver eventSink = null;
	
	/** access to the random number generator initialized with a seed. */
	private static Random random = new Random(System.currentTimeMillis());
	
	/** a handler for a mySQL Database */
	private DatabaseHandler db;
	
	/** determines whether DB support is enabled on the gateway */
	private boolean databaseSupport = false;
	
	/**
	 * @return the server instance belonging to this core item.
	 */
	public Server getServer() {
		return server;
	}
	
	/** 
	 * returns the name of this gateway.
	 * @return the name of this gateway.
	 */
	public String getDeviceName() {
		return gatewayName;
	}
		
	/**
	 * @return the cache module.
	 */
	public Cache getCache() {
		return cache;
	}
	
	/**
	 * provides a short cut to the eventing plugin
	 * @return a handle to the standard eventing plugin.
	 */
	public Eventing getEventManagement() {
		return eventManagement;
	}
	
	/**
	 * @return the random number generator cleanly initialized.
	 */
	public static Random getRandomGenerator() {
		return random;
	}
	
	/**
	 * @return the handler to the Database
	 */
	public DatabaseHandler getDatabaseHandler() {
		return db;
	}
	
	/**
	 * @return whether the system has enabled DB Support
	 */
	public boolean hasDatabaseSupport() {
		return databaseSupport;
	}
	
	/**
	 * enables DB Support at the gateway
	 */
	public void enableDatabaseSupport() {
		databaseSupport = true;
	}
	
	/**
	 * sets the gateway name
	 * @param gatewayName the name for the gateway.
	 */
	void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}
	
	/**
	 * sets the Gateway Location
	 * @param gatewayLocation the name for the gateway.
	 */
	void setGatewayLocation(String gatewayLocation) {
		this.gatewayLocation = gatewayLocation;
	}
	
	/**
	 * start the server that provides HTTP etc.
	 * @param address the address where to attach the server.
	 * @param port the port where to bind.
	 */
	void startServer(String address, int port) {
		//log.info(String.format("using address: %s:%d", address, port));
		server = new Server(address, port);
		
		server.startRESTServer();
	}
	
	/**
	 * startup the core
	 * @throws Exception
	 */
	void startup(String address, int port) throws Exception {
		
		devices = new Devices();
		
		cache = new Cache();
		
		eventManagement = new Eventing();
		
		getGateway().getContext().
				getSymbolicLocation().setLocation("/" + gatewayName + "/");
	
		startServer(address, port);
		
		// start DataBase Support
		// db = new DatabaseHandler();
		// db.startDB();
		// db.checkForEventing("INsert into Measurement ..");
		// enableDatabaseSupport();
		
		// start the event sink
		// startEventSink();
		
    	// start Multi-clients test scenario
		// MultiClientsScenario test = new MultiClientsScenario(Core.getInstance().getServer());
		// Core.getInstance().submitToThreadPool(test);
		
		// start Debugging test scenario
		// TestingMultipleClients test = new TestingMultipleClients(Core.getInstance().getServer());
		// Core.getInstance().submitToThreadPool(test);
		

		//keywords.add("Humidity"); keywords.add("Temperature"); keywords.add("Illumination"); keywords.add("Electrical_Consumption");
		//keywords.add("Switch_Electrical_Appliance");
        
        //devices.addDevice("QBXE434FE", new Device("QBXE434FE", "Television", "Plogg Smart Meter", "Living Room", keywords, null), "/wadl/plogg/");
        //devices.addDevice("QBXE435BD", new Device("QBXE435BD", "Washing Machine", "Plogg Smart Meter", "Kitchen", keywords, null), "/wadl/plogg/");
        //devices.addDevice("QBXE43DKE", new Device("QBXE43DKE", "Radio", "Plogg Smart Meter", "Kitchen", keywords, null), "/wadl/plogg/");
        //devices.addDevice("QBXE43VOA", new Device("QBXE43VOA", "DVD Player", "Plogg Smart Meter", "Living Room", keywords, null), "/wadl/plogg/");
        //devices.addDevice("QBXE43SJB", new Device("QBXE43SJB", "Alarm", "Plogg Smart Meter", "Bedroom", keywords, null), "/wadl/plogg/");
		
	}
	
	/**
	 * tears down the whole server and kills the application.
	 */
	public void shutdown() {
		System.err.println("Shutting down the server...");

		// turn off.
		System.exit(0);
	}
	
	/**
	 * submit a runnable to the thread pool maintained by the core.
	 * @param runnable the runnable to execute.
	 * @return returns the future value returned by the thread pool.
	 */
	public Future<?> submitToThreadPool(Runnable runnable) {
		//log.debug("processing runnable in thread pool");
		return pool.submit(runnable);
	}
	
	/**
	 * default constructor.
	 */
	private Core() {
	}
	
	/** the instance of the singleton. */
	private static final Core instance = new Core();
	
	/**
	 * returns the singleton of the core.
	 * @return the singleton of the core.
	 */
	public static Core getInstance() {
		return instance;
	}

	/**
	 * provides a short cut to the devices plugin.
	 * @return a handle to the standard devices plugin.
	 */
	public Devices getDevices() {
		return devices;
	}

	//////////////////////////////////////////////////////////////////////
	// EVENTSINK THAT ALLOWS USERS TO RECEIVE EXTERNAL EVENTS CENTRALLY //
	//////////////////////////////////////////////////////////////////////	
	/**
	 * startup the event sink.
	 */
	private void startEventSink() {
		int port = 1;
		while (port < 2048) {
			port = getRandomGenerator().nextInt(57000);
		}
		System.err.println("start up event sink...");
		eventSink = new AsynchronousEventReceiver(port);
		System.err.println("starting receiver thread.");
		submitToThreadPool(eventSink);
		Observer o = new Observer() {
			public void update(Observable o, Object arg) {
				System.err.println("received event.");
			}
			
		};
		int nport = eventSink.waitAndGetPort(o);
		if (nport == AsynchronousEventReceiver.ERROR_PORT) {
			System.err.println("no free port found!");
			shutdown();
		}
		System.err.println("receiver thread started on port: " + port);
	}
	
	/**
	 * @return the event sink.
	 */
	public AsynchronousEventReceiver getEventSink() {
		return eventSink;
	}
	
	/**
	 * @return the URL where the event sink can be accessed.
	 */
	public URL getEventSinkURL() {
		URL url = getServer().getHostURI();
		try {
			return new URL("http://" + url.getHost() + ":" 
					+ eventSink.getPort() + Constants.EVENTING_SUBMIT_EVENT);
		} catch (MalformedURLException e) {
			System.err.println("could not encode event sink url: " + e.getStackTrace());
		}
		return null;
	}
	
	/**
	 * subscribe for events at a given URL.
	 * @param url the URL where to subscribe.
	 * @param leaseTime the lease-time the registration is valid.
	 * @param callback the call-back to use. if set to null then the call-back 
	 * of the core event-sink will be used.   
	 * @param keyword the keyword shall be used for the registration.
	 */
	public void subscribeForExternalEvents(
			String url, long leaseTime, String callback, String keyword) {
		
		if (null == url) {
			System.err.println("url is null");
			return;
		}
		
		String urlStr = url + Constants.EVENTING_REGISTRATION;	
		System.err.println(String.format("Using registration url: %s", urlStr));
		
		Form form = new Form();
		form.add("leasetime", 
				String.format("%d",leaseTime));
		if (null == callback) {
			System.err.println("using the core callback.");
			form.add("callback", getEventSinkURL().toString());
		} else {
			form.add("callback", callback);
		}
		form.add("keyword", keyword);
		
		Client client = new Client(
				getServer().getRestApplication()
					.getContext().createChildContext(), Protocol.HTTP);
		
		Response response = client.post(
				urlStr, 
				form.getWebRepresentation());
		if (response.getStatus().getCode() != 
			Status.SUCCESS_OK.getCode()) {
			System.err.println("could not send registration.");
		}
		response.getEntity().release();
	}

	///////////////////////////////////////////////////////////////////////
	// GATEWAY REPRESENTATION OF THE CORE (BASICALLY THE CORE GW DEVICE) //
	///////////////////////////////////////////////////////////////////////
	/**
	 * helper class that gives access to the gateway as a device.
	 */
	private Device gateway = new Device (gatewayName) {
		
		public String getDeviceName() {
			return gatewayName;
		}

		public String handle(Response response, Request request) {
			return asXML();
		}
		
		@Override
		public Context getContext() {
			context.setSymbolicLocation(new SymbolicLocation(gatewayLocation, null));
			context.setKeywords(getKeywords());
			return context;
		}
		
		/**
		 * return all the keywords accessible on this gateway.
		 * @return a list of keywords.
		 */
		public LinkedList<String> getKeywords() {
			LinkedList<String> keywords = new LinkedList<String> ();
			for (String deviceName : getDevices().getDeviceIDs()) {
				for (String keyword : getDevices().
						getDevice(deviceName).getContext().getKeywords()) {
					
					if (!keywords.contains(keyword)) {
						keywords.add(keyword);
					}
				}
			}
			
			return keywords;
		}
		
		@Override
		public String asXML() {
			if (isXmlCacheOk()) {
				return xmlCacheString;
			}
			StringBuffer buf = new StringBuffer();
			buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			buf.append("<Gateway>");

			buf.append("<name>" + getDeviceName() + "</name>");
			
			// append the context
			buf.append(getContext().asXML());

			buf.append("<Devices>");
			Collection<Device> devs  = getDevices().getAll().values();
			buf.append("<local>");
			for (Device dw : devs) {
				buf.append("<Device><ID>" + dw.getDeviceID() + "</ID><Name>" + dw.getDeviceName() + "</Name><Location>" + dw.getLocation() + "</Location></Device>");
			}
			buf.append("</local>");
			buf.append("</Devices>");
			
			// append device description
			//buf.append(getDeviceDescription().asXML());
			
			// append the Resource description
			//buf.append(getResources().asXML());
					
			buf.append("</Gateway>");
			setXMLCacheDirty(false);
			xmlCacheString = buf.toString();
			Core.getInstance().getCache().cache(getDeviceName() + Cache.SUFFIX[Cache.XML], xmlCacheString);
			return xmlCacheString;
		}
		
		public void setXMLCacheDirty(boolean dirty) {
			//Core.getInstance().getCache().invalidate(this);
			this.xmlCacheDirty = dirty;
		}
		
		public void run(){}
		
	};
	
	public Device getGateway() {
		return gateway;
	}
	///////////////////////////////////////////////////////////////////

	/**
	 * display the help to the command line.
	 * @param options the options available on the command line.
	 */
	public static void help() {	}
	
	/**
	 * @param args
	 */
    public static void main(String[] args) throws Exception {
   	
    	try {
    		String gatewayName     = args[0];
    		String gatewayLocation = args[1];
    		String address 	   	   = args[2];
    		int     port       	   = new Integer(args[3]).intValue();
    		Core.getInstance().setGatewayName(gatewayName);
    		Core.getInstance().setGatewayLocation(gatewayLocation);		
    		Core.getInstance().startup(address, port);
    	} catch (Exception e) {
    		e.printStackTrace();
    		System.out.println("");
    		help();
    	}
		for (int i=4; i< args.length; i=i+2){
			if(args[i].equalsIgnoreCase(Constants.TINYOS)){ // tinyOS support provided
				System.err.println("Loading drivers for tinyOS sensors...");
				//System.err.println("Starting tinyOS...");
				//String port    = args[i+1];
				//String moteCom = "export MOTECOM=serial@/dev/tty" + port + ":tmote";
				//String[] cmd = moteCom.split(" ");
				//Process serialDumpProcess = Runtime.getRuntime().exec(cmd);
				Driver tinyDriver = new TinyOSDriver(devices);
				System.err.println("Initializing Active Message-based TinyOS Support...");
				tinyDriver.startDevice();
			}
    		if(args[i].equalsIgnoreCase(Constants.TINYOS_IP)){ // IP-enabled tinyOS sensors support provided
				System.err.println("Loading drivers for tinyOS-enabled sensors with TCP/IPv6 connectivity...");
				
				// TODO: set automatically the command to listen from the ip-driver
				//String ipBaseStationPort = args[i+1];
				//String comPort = "/dev/tty" + ipBaseStationPort;
				//String command = "sudo /opt/tinyos-2.x/support/sdk/c/blip/driver/ip-driver " + comPort + " micaz";
				//String[] cmd = command.split(" ");
				//Process pr = Runtime.getRuntime().exec(cmd);
				
				System.err.println("Initializing IPv6-based TinyOS Support...");
				Driver tinyv6Driver = new TinyosIPv6Driver(Core.getInstance().getDevices());
				tinyv6Driver.startDevice();
				
				// listen for broadcasting messages from IP-enabled sensor devices
				Core.getInstance().submitToThreadPool((TinyosIPv6Driver) tinyv6Driver);	
			}
			if(args[i].equalsIgnoreCase(Constants.CONTIKI)){ // contiki support provided
				System.err.println("Loading drivers for Contiki sensors...");
				String contikiPort = args[i+1];
				String comPort = "/dev/tty" + contikiPort;
				Driver contikiDriver = new ContikiRIMEDriver(devices, comPort);
				System.err.println("Initializing Rime-based Contiki Support...");
				contikiDriver.startDevice();
			}
			if(args[i].equalsIgnoreCase(Constants.CONTIKI_IP)){
				try{
					System.err.println("Loading drivers for Contiki IP-enabled sensors...");
					String command = "SmartDevices/contikiRestIP/RestExample/setup_linslip";
					String[] cmd = command.split(" ");
					Process pr = Runtime.getRuntime().exec(cmd);
					Driver contikiIPv4Driver = new ContikiIPv4Driver(devices);
					System.err.println("Initializing IPv4 Contiki Support...");
					contikiIPv4Driver.startDevice();
					//Core.getInstance().getDevices().addDevice("15", new Device("15", "kokos", "Sensoropoulos","daxame", null, contikiIPv4Driver), "http://localhost:8080/wadl/cntiki/");
					//Core.getInstance().getDevices().startDevice("15");
				} catch (IOException e){
					System.err.println("You must use linslip in order to communicate with Contiki IP-enabled Sensors.");
				}
			}
			if(args[i].equalsIgnoreCase(Constants.SMART_METER)){ 
				System.err.println("Loading drivers for Plogg Smart Meters...");
				String comPort = args[i+1];
				PloggMonitor ploggMonitor = new PloggMonitor(devices, comPort);
				ploggMonitor.startDevice();		
				Core.getInstance().submitToThreadPool(ploggMonitor);	
			}
			if(args[i].equalsIgnoreCase(Constants.SENSARIS)){ 
				System.err.println("Loading drivers for Sensaris Sensor Devices...");
				String comPort = args[i+1];
				senspodDevice sensarisReader = new senspodDevice("ttyUSB0");	
				//Core.getInstance().submitToThreadPool(sensarisReader);	
			}
		}
    }
}
