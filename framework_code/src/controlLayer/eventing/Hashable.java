package controlLayer.eventing;

/**
 * to identify an event registration or event registration request we need a 
 * unique hash value. this abstract class provides a simple uniform approach 
 * how to obtain such a hash value. <br/>
 * for the computation of the hash value, currently we use:
 * <ul>
 * <li>the keyword</li>
 * <li>the callback url</li>
 * </ul>
 * @author sawielan
 *
 */
public abstract class Hashable {
	
	/** the keyword this registration waits for. */
	protected final String keyword;
	
	/** the callback where to deliver notifications. */
	protected final String callback;
	
	/**
	 * constructor.
	 * @param keyword the keyword to use for the hash value.
	 * @param callback the callback to use for the hash value.
	 */
	public Hashable(String keyword, String callback) {
		this.keyword = keyword;
		this.callback = callback;
	}
	
	/**
	 * returns a unique hash for this event descriptor. basically this is the 
	 * keyword concatenated with the callback uri.
	 * @return a hash to identify this registration.
	 */
	public String getHash() {
		return keyword + "_" + callback;
	}
}
