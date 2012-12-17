package deviceLayer.info;

/**
 * encapsulates a key-value pair. you can specify a textual description to 
 * clarify the content of this property. the value of the property can be any 
 * kind of object (just make sure that this object supports the method 
 * toString() ).
 * @author sawielan
 *
 */
public class Property implements XMLRepresentable {

	/** the key of the property. */
	private String key = null;
	
	/** the value of the property. can be any type of object. */
	private Object value = null;
	
	/** a textual description of the key-value pair. */
	private String description = null;
	
	/** flags whether cache is ok or not. */
	protected boolean xmlCacheDirty = true;
	
	/** the cache string. */
	protected String xmlCacheString = null;

	/**
	 * @param key the key of the property.
	 * @param value the value of the property. can be any type of object. 
	 * @param description a textual description of the key-value pair.
	 */
	public Property(String key, Object value, String description) {
		this.key = key;
		this.value = value;
		this.description = description;
		setXMLCacheDirty(true);
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
		setXMLCacheDirty(true);
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
		setXMLCacheDirty(true);
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
		setXMLCacheDirty(true);
	}

	public String asXML() {
		String str = "";
		if (isXmlCacheOk()) {
			return xmlCacheString;
		}
		str += "<property>";
		str += "<key>" + key + "</key>";
		str += "<value>" + value.toString() + "</value>";
		str += "<description>" + description + "</description>";
		str += "</property>";		
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
