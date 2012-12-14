package deviceLayer.info;

/**
 * a device can have different models.
 * @author sawielan
 *
 */
public class DeviceModel implements XMLRepresentable {

	/** the name of the model. */
	private String modelName = null;
	
	/** the number of the model. */
	private String modelNumber = null;
		
	/** flags whether cache is ok or not. */
	protected boolean xmlCacheDirty = true;
	
	/** the cache string. */
	protected String xmlCacheString = null;
	
	/**
	 * default constructor.
	 */
	public DeviceModel() {
		setXMLCacheDirty(true);
	}
	
	/**
	 * constructor creating a fully equipped device model.
	 * @param modelName the name of the model.
	 * @param modelNumber the version number of the model.
	 */
	public DeviceModel(String modelName, String modelNumber) {
		this.modelName = modelName;
		this.modelNumber = modelNumber;
		setXMLCacheDirty(true);
	}

	/**
	 * @param modelNumber the modelNumber to set
	 */
	public void setModelNumber(String modelNumber) {
		this.modelNumber = modelNumber;
		setXMLCacheDirty(true);
	}

	/**
	 * @return the modelNumber
	 */
	public String getModelNumber() {
		return modelNumber;
	}

	/**
	 * @param modelName the modelName to set
	 */
	public void setModelName(String modelName) {
		this.modelName = modelName;
		setXMLCacheDirty(true);
	}

	/**
	 * @return the modelName
	 */
	public String getModelName() {
		return modelName;
	}
	
	public String asXML() {
		String str = "";
		if (isXmlCacheOk()) {
			return xmlCacheString;
		}
		str += "<model>";
			str += "<name>" + modelName + "</name>";
			str += "<number>" + modelNumber + "</number>";
		str += "</model>";		
		xmlCacheString = str;
		setXMLCacheDirty(false);
		return str;
	}
	
	public void setXMLCacheDirty(boolean dirty) {
		this.xmlCacheDirty = dirty;
	}
	
	public boolean isXmlCacheOk() {
		return (false == xmlCacheDirty);
	}
}
