package deviceLayer.info;

/**
 * simple interface for all description parts that are xml representable.
 * @author sawielan
 *
 */
public interface XMLRepresentable {

	/**
	 * sets this object to dirty. basically this means that the cached xml 
	 * representation is invalidated.
	 * @param dirty
	 */
	public void setXMLCacheDirty(boolean dirty);
	
	/**
	 * @return true if the cached version is invalid, false otherwise.
	 */
	public boolean isXmlCacheOk();
	
	/**
	 * returns the content of the implementing class as an xml into the 
	 * string buffer. 
	 * @return the representation as an xml string.
	 */
	public abstract String asXML();
}
