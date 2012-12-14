package controlLayer.libraryCode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import deviceLayer.Device;

/**
 * the caching module that allows "lookup" and retrieval of cached items. 
 * basically this is a bridge to the java hash map {@link ConcurrentHashMap}.
 * @author sawielan
 *
 */
public class Cache {
	
	/** the suffix to use for items to cache as xml. */
	public static final int XML = 0;
	
	/** the suffix to use for items to cache as html. */
	public static final int HTML = 1;
	
	/** the suffix to use for items to cache as plain. */
	public static final int PLAIN = 2;
	
	/** the cached items. */
	private Map<String, Object> cache =
		new ConcurrentHashMap<String, Object> ();
	
	/** the different suffices to use. */
	public static  final String [] SUFFIX = new String[] {
			"_text/xml",
			"_text/html",
			"_text/plain"
	};
	
	/**
	 * constructor of the cache module.
	 */
	public Cache() {
		
	}
	
	/**
	 * invalidates a cache entry.
	 * @param key the key to the cached item.
	 */
	public synchronized void invalidate(String key) {
		cache.remove(key);
	}
	
	public synchronized void invalidate(Device device) {

		for  (String suffix : SUFFIX) {
			invalidate(device.getDeviceName() + suffix);
		}
	}
	
	/**
	 * performs a lookup that checks whether an item is in cache.
	 * @param key the key to the cached item.
	 * @return true if item is cached, false otherwise.
	 */
	public synchronized boolean hasCache(String key) {
		if (cache.containsKey(key)) {
			return true;
		}
		return false;
	}
	
	/**
	 * tries to lookup a cached item and returns this item.
	 * @param key the key to the cached item.
	 * @return the cached object or null if not available.
	 */
	public synchronized Object getCache(String key) {
		return cache.get(key);
	}
	
	/**
	 * put the item into the cache. if there is already an item in the cache, then 
	 * this item will be replaced.
	 * @param key
	 * @param item
	 */
	public synchronized void cache(String key, Object item) {
		cache.put(key, item);
	}
	
	/**
	 * clear out the cache.
	 */
	public synchronized void clear() {
		cache.clear();
	}
}
