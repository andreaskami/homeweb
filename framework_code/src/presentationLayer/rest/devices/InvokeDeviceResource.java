package presentationLayer.rest.devices;

import org.restlet.Context;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import deviceLayer.Device;
import deviceLayer.Devices;

/**
 * Restlet Resource to invoke a call on a device.
 * 
 * @author sawielan
 * 
 */
public class InvokeDeviceResource extends AbstractResource {
	
	/**
	 * constructor of the resource.
	 * @param context the context to use.
	 * @param request the request to handle.
	 * @param response the response where to put the answer.
	 */
	public InvokeDeviceResource(Context context, Request request,
			Response response) {

		super(context, request, response);

		getVariants().add(new Variant(MediaType.TEXT_HTML));
		getVariants().add(new Variant(MediaType.ALL));
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

	public void handleEveryting() {
		Devices devs = controlLayer.Core.getInstance().getDevices();

		// get the device name to process/invoke
		String deviceID = (String) getRequest().getAttributes().get("device");

		if (deviceID == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}

		if (devs.containsDevice(deviceID)) {
			Request request = getRequest();
			Response response = getResponse();
			String xml = null;

			Device dev = devs.getDevice(deviceID);
			
			// if xml != null then xml will be handled the same way as
			// the response of a locally attached device.
			xml = dev.handle(response, request);

			if (xml == null) {
				if (!getVariants().contains(response.getEntity().getMediaType())) {
					// add this mediatype.
					getVariants().add(new Variant(response.getEntity().getMediaType()));
				}
				return;
			}

			try {
				Representation rep = handle(
						devs.getDevice(deviceID),
						xml,
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
		String error = "There is no such Device in the Smart Devices' List";
		getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		getResponse().setEntity(new StringRepresentation(error));
	}
}
