package presentationLayer.rest.devices;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;

import controlLayer.Core;
import controlLayer.libraryCode.Cache;

import deviceLayer.Device;
//import ch.ethz.inf.vs.gateway.translator.Translator;
//import ch.ethz.inf.vs.gateway.translator.TranslatorManager;

/**
 * abstract class that extends the restlet resource. it provides a simple 
 * standard handler that will generate the representation in the standard 
 * formats available.
 * @author sawielan
 *
 */
public abstract class AbstractResource extends Resource {
	
	
	/** the initial MIME type from where the translation starts. */
	public static final String INITIAL_TYPE = "text/xml";
	
	/**
	 * constructor.
	 * @param context the context.
	 * @param request the request to handle.
	 * @param response the response where to put answers.
	 */
	public AbstractResource(Context context, 
			Request request, Response response) {
		
		super(context, request, response);
	}
	
	/**
	 * generates a key for the caching item.
	 * @param device the device to use for the cache key generation.
	 * @return the key to use for the cache.
	 */
	public String xmlKey(Device device) {
		return device.getDeviceName()+ Cache.SUFFIX[Cache.XML];
	}
	
	/**
	 * generates a key for the caching item.
	 * @param device the device to use for the cache key generation.
	 * @return the key to use for the cache.
	 */
	public String htmlKey(Device device) {
		return device.getDeviceName() + Cache.SUFFIX[Cache.HTML];
	}
	
	/**
	 * generates a key for the caching item.
	 * @param device the device to use for the cache key generation.
	 * @return the key to use for the cache.
	 */
	public String plainKey(Device device) {
		return device.getDeviceName() + Cache.SUFFIX[Cache.PLAIN];
	}
	
	/**
	 * handles all default requests on a device.
	 * @param xml if XML provided this XML is used. if null the device gets 
	 * polled with asXML().
	 * @param device the device to represent.
	 * getAsXML.
	 * @return the representation generated.
	 * @throws Exception whenever the intermediate representation could not be 
	 * transformed.
	 */
	public synchronized Representation handle(
			Device device, 
			String xml,
			MediaType mt) 
		throws Exception {
		
		if (xml == null) {
			//log.debug("xml is null - therefore acquire from device.");
			xml = device.asXML();
		}
				
		Cache cache = Core.getInstance().getCache();
		//cache the XML.
		cache.cache(xmlKey(device), xml);
		// serve the best translation.
		//TranslatorManager mgr = TranslatorManager.getInstance();
		//String mtStr = mt.getName();
		//if (mgr.has(INITIAL_TYPE, mtStr)){
			
			//				//log.debug("cached item.");
				//} else {	
					//log.debug("no cached item.");
					//Translator t = mgr.getTranslator(INITIAL_TYPE, mtStr);
					//c = t.translate(xml);
					//cache.cache(cStr, c);
				
				//}

				//if (c instanceof String) {
					//return new StringRepresentation((String) c, mt);
				//} else {
					// TODO: do something about non string representations..					
					//return new StringRepresentation((String) c, mt);
				//}
			//} catch (Exception e) {
				//log.debug("could not invoke translator: " + e.getMessage());
			//}
		//}
			
		// return XML representation if everything else fails...
		return new StringRepresentation(xml, MediaType.TEXT_XML);
	}
}
