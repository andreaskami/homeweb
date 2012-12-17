/**
 * 
 */
package deviceLayer.info;

import java.util.HashMap;
import java.util.Map;

/**
 * encapsulates dynamic information about eg a context, device info, etc...
 * @author sawielan
 */
public abstract class DynamicDescription implements XMLRepresentable {
	
	/** hash map to store different dynamic properties as key-value pairs. */
	private Map<String, Property> dynamicProperties = 
			new HashMap<String, Property> ();
	
	/** flags whether cache is ok or not. */
	protected boolean xmlCacheDirty = true;
	
	/** the cache string. */
	protected String xmlCacheString = null;
	
	/**
	 * default constructor.
	 */
	public DynamicDescription() {
		
	}
	
	/**
	 * constructor that creates a fully equipped dynamic description object.
	 * @param dynamicProperties the dynamic properties.
	 */
	public DynamicDescription(Map<String, Property> dynamicProperties) {
		
		// only set the dynamic properties if not null...
		if (dynamicProperties != null) {
			this.dynamicProperties = dynamicProperties;
		}
		setXMLCacheDirty(true);
	}
	
	/**
	 * @param dynamicProperties the dynamic properties to set
	 */
	public void setDynamicProperties(Map<String, Property> dynamicProperties) {
		this.dynamicProperties = dynamicProperties;
		setXMLCacheDirty(true);
	}

	/**
	 * @return the dynamics
	 */
	public Map<String, Property> getDynamicProperties() {
		return dynamicProperties;
	}
	
	/**
	 * adds a dynamic information to the dynamic properties.
	 * @param key the key of the property.
	 * @param property the property itself.
	 */
	public void addDynamicProperty(String key, Property property) {
		dynamicProperties.put(key, property);
		setXMLCacheDirty(true);
	}
	
	/**
	 * removes a dynamic information from the dynamic properties.
	 * @param key the key of the property.
	 * @return the property that was removed, null if not existant.
	 */
	public Property removeDynamicProperty(String key) {
		return dynamicProperties.remove(key);
	}
	
	public String asXML() {
		String str = "";
		if (isXmlCacheOk()) {
			return xmlCacheString;
		}
		
		str += "<dynamic>";
		for (Property property : dynamicProperties.values()) {
			str += property.asXML();
		}
		str += "</dynamic>";
		
		xmlCacheString = str;
		setXMLCacheDirty(false);
		return str;
	}
}
