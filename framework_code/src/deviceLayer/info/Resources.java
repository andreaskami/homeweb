package deviceLayer.info;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * the resource description holds all the resources that are provided from a 
 * device. a temperature sensor might export its resource "getTemperature" 
 * through this resource description.
 * @author sawielan
 *
 */
public class Resources implements XMLRepresentable {
	
	/** a list holding the resource. */
	private Map<String, Resource> resources = new HashMap<String, Resource> (); 
	
	/** flags whether cache is ok or not. */
	protected boolean xmlCacheDirty = true;
	
	/** the cache string. */
	protected String xmlCacheString = null;
	
	/**
	 * standard constructor.
	 */
	public Resources() {
		setXMLCacheDirty(true);
	}
	
	/**
	 * constructor with a resource list.
	 * @param resources the resources provided through this interface.
	 */
	public Resources(LinkedList<Resource> resources) {
		for (Resource r : resources) {
			this.resources.put(r.getResourceName(), r);
		}
		setXMLCacheDirty(true);
	}
	
	public boolean isXmlCacheOk() {
		if (xmlCacheDirty) {
			return false;
		}
		for (Resource r : resources.values()) {
			if (!r.isXmlCacheOk()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @return a list holding all the resources provided through this interface.
	 */
	public Collection<Resource> getResources() {
		return resources.values();
	}
	
	/**
	 * perform a lookup for a specific resource. true if present, false 
	 * otherwise.
	 * @param resourceName the name of the resource.
	 * @return true if present, false otherwise.
	 */
	public boolean containsResource(String resourceName) {
		return resources.containsKey(resourceName);
	}
	
	/**
	 * performs a lookup for a specific resource. if the resource is not present 
	 * a null value is returned.
	 * @param resourceName the name of the resource to be returned.
	 * @return a specific resource.
	 */
	public Resource getResource(String resourceName) {
		return resources.get(resourceName);
	}
	
	/**
	 * sets the resources for this interface.
	 * @param resources the resources that are available on this interface.
	 */
	public void setResources(LinkedList<Resource> resources) {
		for (Resource r : resources) {
			this.resources.put(r.getResourceName(), r);
		}
	}
	
	/**
	 * removes a resource from the resource description. if the resource does not 
	 * exist this method just returns. this method is thread safe.
	 * @param resource the service to be removed.
	 */
	public synchronized void removeResource(Resource resource) {
		synchronized (resources) {
			resources.remove(resource);
		}
	}
	
	/**
	 * adds a new resource to the resource description. if the resource already 
	 * exists the old version will be replaced. this method is thread safe.
	 * @param resource the resource to be added. 
	 */
	public synchronized void addResource(Resource resource) {
		synchronized (resources) {
			resources.put(resource.getResourceName(), resource);
		}
		setXMLCacheDirty(true);
	}
	
	public String asXML() {
		String str = "";
		if (isXmlCacheOk()) {
			return xmlCacheString;
		}
		if (resources.size() == 0) {
			setXMLCacheDirty(false);
			str += "<resources/>";
			return str;
		}

		str += "<resources>";
		for (Resource resource : resources.values()) {
			str += resource.asXML();
		}
		str += "</resources>";
		
		xmlCacheString = str;
		setXMLCacheDirty(false);
		return str;
	}

	public void setXMLCacheDirty(boolean dirty) {
		this.xmlCacheDirty = dirty;
	}

}
