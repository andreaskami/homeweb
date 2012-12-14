package presentationLayer.rest.devices;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import controlLayer.Core;
import deviceLayer.Device;
//import ch.ethz.inf.vs.gateway.api.cache.Cache;
//import ch.ethz.inf.vs.gateway.translator.Translator;
//import ch.ethz.inf.vs.gateway.translator.TranslatorManager;

/**
 * creates a representation of the mapping from devices to keywords.
 * @author sawielan
 *
 */
public class GatewayResource extends AbstractResource {
	
	public GatewayResource(Context context, 
			Request request, Response response) {
		
		super(context, request, response);
		
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));
		getVariants().add(new Variant(MediaType.TEXT_XML));
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}
		
	
	/**
	* Returns a full representation for a given variant.
	*/
   @Override
   public Representation getRepresentation(Variant variant) {
	   try {
		   /*  presents the XML representation of the Gateway, which resides in the Core (in Control Layer) */
		   return handle(
				   Core.getInstance().getGateway(),
				   null,
				   variant.getMediaType());
		 
	   } catch (Exception e) {
		   getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		   return null;
	   }
   }
  
}
