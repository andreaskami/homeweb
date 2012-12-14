package deviceLayer;

import java.net.URL;

/**
 * an abstract driver for a gateway class. in the tree structure only 
 * devices of type gateway driver are allowed.
 * @author sawielan
 *
 */
public abstract class GatewayDevice extends Device {

	/** the name of the gateway. */
	protected String name;
	
	/** the host uri of the other gateway. */
	protected URL host;
	
	/**
	 * constructor of the device.
	 * @param name the name of the gateway.
	 */
	public GatewayDevice(String name) {
		super(name);
		this.name = name;
	}

	/**
	 * returns the host uri of the other gateway.
	 * @return the host uri of the other gateway.
	 */
	public URL getHost() {
		return host;
	}
	
	/**
	 * returns the device name.
	 * @return the name of the device.
	 */
	public String getDeviceName() {
		return name;
	}
	
	/**
	 * sets the name of the device. be careful with this method. especially do 
	 * not call it when you have already installed the device in the gateway. 
	 * otherwise this could lead to inconsistency to the device mapping within 
	 * the gateway.
	 * @param deviceName the new name for this device.
	 */
	public void setDeviceName(String deviceName) {
		this.name = deviceName;
	}

}
