package deviceLayer.info;

import java.util.Map;

/**
 * encapsulates information about the underlying device (stuff like firmware, 
 * vendor, ...).
 *
 */

public class DeviceDescription extends DynamicDescription {
	
	/** the vendor. */
	private String vendor = null;
	
	/** the model. */
	private DeviceModel model = null;
	
	/**
	 * default constructor.
	 */
	public DeviceDescription() {
		super();
		setXMLCacheDirty(true);
	}

	/**
	 * constructor that create a fully equipped device description.
	 * @param vendor the vendor.
	 * @param model the device model.
	 * @param dynamicProperties the dynamic properties to set. if set to null 
	 * the parameter is ignored.
	 */
	public DeviceDescription(
			String vendor, 
			DeviceModel model,
			Map<String, Property> dynamicProperties
			) {
		
		super(dynamicProperties);
		
		this.vendor = vendor;
		this.model = model;		
		setXMLCacheDirty(true);
	}
	
	public boolean isXmlCacheOk() {
		if ((null != model) && (!model.isXmlCacheOk()) ) {
			return false;
		}
		if (xmlCacheDirty) {
			return false;
		}
		return true;
	}
	
	/**
	 * @param model the model to set
	 */
	public void setModel(DeviceModel model) {
		this.model = model;
		setXMLCacheDirty(true);
	}

	/**
	 * @return the model
	 */
	public DeviceModel getModel() {
		return model;
	}

	/**
	 * @param vendor the vendor to set
	 */
	public void setVendor(String vendor) {
		this.vendor = vendor;
		setXMLCacheDirty(true);
	}

	/**
	 * @return the vendor
	 */
	public String getVendor() {
		return vendor;
	}
	
	@Override
	public String asXML() {
		String str = "";
		if (isXmlCacheOk()) {
			return xmlCacheString;
		}
		str += "<information>";
		// add the parent stuff
		str += super.asXML();
		
		str += "<properties>";
		str += "<vendor>" + vendor + "</vendor>";
		str += model.asXML();
		str += "</properties>";
		str += "</information>";		
		xmlCacheString = str;
		setXMLCacheDirty(false);
		return str;
	}
	
	public void setXMLCacheDirty(boolean dirty) {
		this.xmlCacheDirty = dirty;
	}
}
