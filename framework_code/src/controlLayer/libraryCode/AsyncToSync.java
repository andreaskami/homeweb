package controlLayer.libraryCode;

import deviceLayer.Response;
import deviceLayer.Device;

/**
 * helper class to synchronize the async communication to contiki/tinyos.
 * @author sawielan
 *
 */
public class AsyncToSync {
	
	/** my token. */
	private final long token;
	
	/** the response onto my request. */
	private Response response = null;

	/** 
	 * constructor.
	 */
	public AsyncToSync() {
		token = Device.getToken();
	}
	
	/**
	 * @return my token. 
	 */
	public long getToken() {
		return token;
	}
	
	/**
	 * @return the response
	 */
	public Response getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(Response response) {
		this.response = response;
	}
};