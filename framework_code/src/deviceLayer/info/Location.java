package deviceLayer.info;

public abstract class Location implements XMLRepresentable {
	
	/** flags whether cache is ok or not. */
	protected boolean xmlCacheDirty = true;
	
	/** the cache string. */
	protected String xmlCacheString = null;
	
	public Location() {
		
	}
	
	public void setXMLCacheDirty(boolean dirty) {
		this.xmlCacheDirty = dirty;
	}
	
	public boolean isXmlCacheOk() {
		return (false == xmlCacheDirty);
	}
}
