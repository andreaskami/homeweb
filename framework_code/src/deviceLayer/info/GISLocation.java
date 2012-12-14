package deviceLayer.info;

/**
 * stub to encapsulate a gis location.
 * @author sawielan
 *
 */
public class GISLocation extends Location {
	
	/** the latitude. */
	private String latitude;
	
	/** the longitude. */
	private String longitude;
	
	/** the elevation. */
	private String elevation;
	
	/**
	 * constructor to create a GISLocation object.
	 * @param latitude the latitude.
	 * @param longitude the longitude.
	 * @param elevation the elevation.
	 */
	public GISLocation(String latitude, String longitude, String elevation) {
		setLatitude(latitude);
		setLongitude(longitude);
		setElevation(elevation);
		setXMLCacheDirty(true);
	}

	/**
	 * @return the latitude
	 */
	public String getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(String latitude) {
		this.latitude = latitude;
		setXMLCacheDirty(true);
	}

	/**
	 * @return the longitude
	 */
	public String getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(String longitude) {
		this.longitude = longitude;
		setXMLCacheDirty(true);
	}

	/**
	 * @return the elevation
	 */
	public String getElevation() {
		return elevation;
	}

	/**
	 * @param elevation the elevation to set
	 */
	public void setElevation(String elevation) {
		this.elevation = elevation;
		setXMLCacheDirty(true);
	}

	public String asXML() {
		String str = "";
		if (isXmlCacheOk()) {
			return xmlCacheString;
		}
		
		str += "<gis>";
			str += "<longitude>" + longitude + "</longitude>";
			str += "<latitude>" + latitude + "</latitude>";
			str += "<elevation>" + elevation + "</elevation>";
		str += "</gis>";
		
		xmlCacheString = str;
		setXMLCacheDirty(false);
		return str;
	}

}
