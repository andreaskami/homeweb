package controlLayer.libraryCode;

public class Constants {

	 /** ack body text. */
	public static final String ACK = "ack";
	
	/** nack body text. */
	public static final String NACK = "nack";
	
	// REST basic verbs
	/** POST verb. */
	public static final String VERB_POST = "POST";
	
	/** GET verb. */
	public static final String VERB_GET = "GET";
	
	/** DELETE verb. */
	public static final String VERB_DELETE = "DELETE";
	
	/** PUT verb. */
	public static final String VERB_PUT = "PUT";
	
	/** EVENTING verb */
	public static final String EVENTING = "Eventing";
	
	/** STREAMING verb. */
	public static final String STREAMING = "Streaming";
	
	/** the identifiers for Device's Thread Behavior */
	
	/** the identifiers for streaming. */
	public static final String STREAMING_INTERVAL = "interval";
	public static final String STREAMING_PERIOD   = "iterations";
	
	/** Aliveness Resource: check if Device is still alive*/
	public static final String RESOURCE_ALIVE ="Aliveness";

	/** command line parameter for Sensaris sensors support */
	public static final String SENSARIS = "sensaris";
	
	/** command line parameter for TinyOS support */
	public static final String TINYOS = "tinyos";
	
	/** command line parameter for TinyOS support */
	public static final String TINYOS_IP = "TinyosIP";
	
	/** command line parameter for Contiki support */
	public static final String CONTIKI = "contikiRIME";
	
	/** command line parameter for IP-enabled Contiki support */
	public static final String CONTIKI_IP = "contikiIP";
	
	/** command line parameter for Plogg Smart Meters support */
	public static final String SMART_METER = "Ploggs";
	
	/** Smart Energy Meters Parameters */
	
	/** Maximum amount of time waiting until all the Plogg Smart Meters are discovered, before issuing other commands (12 seconds)*/
	public static final int MAXWAITFORMETERS = 12000;
	
	/** Streaming delay for Energy Measurements (1 minute) */
	public static final int METER_STREAMING_DELAY = 60000;
	
	/** System Parameters */
	
	/** amount of time for sleeping at the beginning until all service description data arrive */
	public static final int DEV_INITIAL_SLEEP_TIME     = 0;
	
	/** time delay for sending requests one after the other at the Request Queue (300 millisecond for one-hop IPv6-enabled Telosb sensors)*/
	public  static final int DEV_REQUEST_INTERVAL      = 600;					
	
	/** percentage of transmission failures during the operation of the gateway (mostly for simulation reasons)*/
	public  static final int TRANSMISSION_FAILURE = 0;
	
	/** failure identification check time interval (15 min)*/
	public static final int DEV_ALIVENESS_CHECK_TIME   = 900000; 
	
	/** service cached last value validity time duration */
	public static final int DEV_MAX_CACHE_DELAY_TIME   = 60000;
	
	/** in case of Device failure, cached value validity time duration  */
	public static final int DEV_MAX_FAILURE_DELAY_TIME = 0;
	
	/** maximum number of attempts to transmit a request (before Failure is detected) */
	public  static final int DEV_REQUEST_MAX_ATTEMPTS   = 5;
	
	/** Request Priority Parameters */
	
	/** value of priority for requests with high priority (mostly for simulation reasons)*/
	public  static final boolean PRIORITIES = false;
	
	/** value of priority for requests with high priority (mostly for simulation reasons)*/
	public  static final int HIGH_PRIORITY = 10;
	
	/** value of priority for requests with high priority (mostly for simulation reasons)*/
	public  static final int NORMAL_PRIORITY = 5;
	
	/** value of priority for requests with high priority (mostly for simulation reasons)*/
	public  static final int LOW_PRIORITY = 1;
	
	/** percentage of requests with high priority (mostly for simulation reasons)*/
	public  static final int PROBABILITY_HIGH_PRIORITY = 15;
	
	/** percentage of requests with high priority (mostly for simulation reasons)*/
	public  static final int PROBABILITY_LOW_PRIORITY = 10;
	
	/** value of increasing priority at every addition in the priority queue (mostly for simulation reasons)*/
	public  static final int INC_PRIORITY_VALUE = 1;
	
	/** URI Values */
	
	/** Acknowledgment value returned by the Smart Device (for POST and STREAMING requests) */
	public static final int DEV_ACKNOWLEDGMENT         = 1;
	
	/** the identifiers for REST architectural Style in presentation Layer */
	
	public static final String MODIFY_DEVICE = "/_device";
	
	/** the name of the parameter in the request to create a new device. */
	public static final String CREATE_DEVICE_PARAM_NAME = "name";
	
	/** the name of the parameter in the request to create a new device. */
	public static final String CREATE_DEVICE_PARAM_CLASS = "class";

	/** register for eventing */
	public static final String EVENTING_REGISTRATION = "/_eventing/registration";
	  
	/** submit an event */
	public static final String EVENTING_SUBMIT_EVENT = "/_eventing/event";
	
	/** the identifiers for WADL Service Description Data URLs */
	
	/** WADL general Identifier */
	public static final String WADL = "/wadl/";
	
	/** WADL for tinyOS */
	public static final String WADL_ROUTE_TINYOS = WADL + "tinyos/";
	
	/** WADL for tinyOS IPv6*/
	public static final String WADL_ROUTE_TINYOS_IPv6 = WADL + "tinyosIP6/";
	
	/** WADL for tinyOS */
	public static final String WADL_ROUTE_CONTIKI = WADL + "contiki/";
	
	/** WADL for Ploggs */
	public static final String WADL_ROUTE_PLOGG = WADL + "plogg/";
	
	/**  the identifiers for WADL file parsing */
	
	public static final String WADL_RESOURCE         = "resource";
	public static final String WADL_METHOD 	   	     = "method";
	public static final String WADL_DESCRIPTION      = "doc";
	public static final String WADL_REQUEST 	     = "request";
	public static final String WADL_PARAMETER        = "param";
	public static final String WADL_PARAMETER_OPTION = "option";
	public static final String WADL_RESPONSE	     = "response";
	public static final String WADL_REPRESENTATION   = "representation";
}
