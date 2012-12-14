package deviceLayer.info;

import java.util.LinkedList;
import java.util.List;

/**
 * context encapsulates information of the device like the location of the 
 * device, supported keywords, ...
 * @author sawielan
 *
 */
public class Context implements XMLRepresentable {
	
	/** the location in the overlay. */
	private SymbolicLocation symbolicLocation = new SymbolicLocation();
	
	/** the physical location of the device (eg geo location). */
	private GISLocation gisLocation = null;
	
	/** the keywords supported by this device. */
	private LinkedList<String> keywords = new LinkedList<String>();
	
	/** flags whether cache is ok or not. */
	protected boolean xmlCacheDirty = true;
	
	/** the cache string. */
	protected String xmlCacheString = null;
	
	/**
	 * default constructor.
	 */
	public Context() {
		setXMLCacheDirty(true);
	}
	
	/**
	 * constructor that creates a fully equipped Context object.
	 * @param symbolicLocation the location in the overlay.
	 * @param gisLocation the physical location of the device. (geo).
	 * @param keywords the keywords supported by the device.
	 */
	public Context(
			SymbolicLocation symbolicLocation, 
			GISLocation gisLocation,
			LinkedList<String> keywords) {
		
		setKeywords(keywords);
		setGISLocation(gisLocation);
		setSymbolicLocation(symbolicLocation);
		setXMLCacheDirty(true);
	}
	
	
	public boolean isXmlCacheOk() {
		if (xmlCacheDirty) {
			return false;
		}
		
		if (null != symbolicLocation && !symbolicLocation.isXmlCacheOk()) {
			return false;
		}
		
		if (null != gisLocation && !gisLocation.isXmlCacheOk()) {
			return false;
		}
		
		return true;
	}

	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(List<String> keywords) {
		boolean dirty = false;
		
		if(keywords == null)
			return;
		for(int k=0; k < keywords.size(); k++){
			if(!this.keywords.contains(keywords.get(k))){
					this.keywords.add(keywords.get(k));
					dirty = true;
			}
		}
		setXMLCacheDirty(dirty);
	}

	/**
	 * @return the keywords
	 */
	public LinkedList<String> getKeywords() {
		return keywords;
	}

	/**
	 * @param gisLocation the gisLocation to set
	 */
	public void setGISLocation(GISLocation gisLocation) {
		this.gisLocation = gisLocation;
		setXMLCacheDirty(true);
	}

	/**
	 * @return the gisLocation
	 */
	public GISLocation getGISLocation() {
		return gisLocation;
	}

	/**
	 * @param symbolicLocation the symbolicLocation to set
	 */
	public void setSymbolicLocation(SymbolicLocation symbolicLocation) {
		this.symbolicLocation = symbolicLocation;
		setXMLCacheDirty(true);
	}

	/**
	 * @return the symbolicLocation
	 */
	public SymbolicLocation getSymbolicLocation() {
		return symbolicLocation;
	}
	
	public String asXML() {
		String str = "";
		if (isXmlCacheOk()) {
			return xmlCacheString;
		}
		str += "<location>";
			if (symbolicLocation != null) {
				str += symbolicLocation.asXML();
			}
			if (gisLocation != null) {
				str += gisLocation.asXML();
			}
		str += "</location>";
		str += "<keywords>";
		for (String keyword : keywords) {
			str += "<keyword>" + keyword + "</keyword>";
		}
		str += "</keywords>";		
		xmlCacheString = str;
		setXMLCacheDirty(false);
		return str;
	}
	
	public void setXMLCacheDirty(boolean dirty) {
		this.xmlCacheDirty = dirty;
	}
}
