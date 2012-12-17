package presentationLayer.rest.devices;

import org.restlet.Context;
import org.restlet.data.Language;
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
public class DevicesResource extends AbstractResource {
	
	public DevicesResource(Context context, 
			Request request, Response response) {
		
		super(context, request, response);
		
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));
		getVariants().add(new Variant(MediaType.TEXT_XML));
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public boolean allowPut() {
		return true;
	}

	@Override
	public boolean allowGet() {
		return true;
	}
	
	@Override
	public boolean allowDelete() {
		return true;
	}

	@Override
	public void handlePost() {
		handleEveryting();
	}

	@Override
	public void handlePut() {
		handleEveryting();
	}

	@Override
	public void handleGet() {
		handleEveryting();
	}
	
	@Override
	public void handleDelete() {
		handleEveryting();
	}
	
	
	/**
	 * handles all requests on a device.
	 */	
	public void handleEveryting() {

		try {

			/*  presents the XML representation of the Gateway, which resides in the Core (in Control Layer) */
			Request request = getRequest();
			Response response = getResponse();
			
			Representation rep = handle(
					Core.getInstance().getGateway(),
					null,
					request.getClientInfo().getPreferredVariant(
							getVariants(), Language.ENGLISH).getMediaType());

			response.setEntity(rep);
			getResponse().setStatus(Status.SUCCESS_OK);
			return;

		} catch (Exception e) {
			e.printStackTrace();
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			return;
		}
	   
   }
   
}
