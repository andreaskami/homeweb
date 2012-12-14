package deviceLayer.info;

/**
 * wraps a symbolic location string.
 * @author sawielan
 *
 */
public class SymbolicLocation extends Location {

	/** the location string. */
	private String location = "/";
	
	/**
	 * default constructor.
	 */
	public SymbolicLocation() {
		
	}
	
	/**
	 * @param location the location string.
	 * @param parent the parent.
	 */
	public SymbolicLocation(String location, String parent) {
		super();
		this.location = location;
		setXMLCacheDirty(true);
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
		setXMLCacheDirty(true);
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	public String asXML() {
		String str = "";
		if (isXmlCacheOk()) {
			return xmlCacheString;
		}
		str += "<symbolical>";
			str += "<current>" + location + "</current>";
		str += "</symbolical>";
		
		xmlCacheString = str;
		setXMLCacheDirty(false);
		return str;
	}

}
