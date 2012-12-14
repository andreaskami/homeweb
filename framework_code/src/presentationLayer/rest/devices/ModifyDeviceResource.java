package presentationLayer.rest.devices;

import java.util.HashMap;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;

import controlLayer.Core;
import controlLayer.libraryCode.Constants;
import deviceLayer.Devices;

/**
 * resource that handles the creation of new devices.
 * @author sawielan
 *
 */
public class ModifyDeviceResource extends Resource {
	
			
	/** the name of the parameter in the request 
	 * (moved to constants, here for backward compatibility). */
	public static final String PARAM_NAME = Constants.CREATE_DEVICE_PARAM_NAME;
	
	/** the name of the parameter in the request
	 * (moved to constants, here for backward compatibility). */
	public static final String PARAM_CLASS = Constants.CREATE_DEVICE_PARAM_CLASS;
	
	/**
	 * constructor of the resource.
	 * @param context the context to use.
	 * @param request the request to handle.
	 * @param response the response where to put the answer.
	 */
	public ModifyDeviceResource(Context context, 
			Request request, Response response) {
		
		super(context, request, response);
	}
	
	@Override
	public boolean allowPost() {
		return false;
	}
		
	@Override
	public boolean allowPut() {
		return true;
	}
	
	@Override
	public boolean allowGet() {
		return false;
	}
	
	@Override
	public boolean allowDelete() {
		return true;
	}
	
	@Override
	public void handlePut() {
		// we need at least the name of the device
		Form form = getRequest().getEntityAsForm();
		Parameter nameParam = form.getFirst(PARAM_NAME);
		Parameter classParam = form.getFirst(PARAM_CLASS);
		if ((null == nameParam) || (null == classParam)) {
			//log.error(
				//	String.format(
					//		"missing parameter: (name=%s,class=%s",
						//	nameParam,
							//classParam
							//)
					//);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}
		
		String name = nameParam.getValue();

		Devices devs = controlLayer.Core.getInstance().getDevices();
		if (devs.containsDevice(name)) {
			//log.error("device already exists.");
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}
		
		Map<String, Object> prms = new HashMap<String, Object> ();
		final int len = form.size();
		for (int i=0; i<len; i++) {
			Parameter p = form.get(i);
			prms.put(p.getName(), p.getValue());
		}
		
		//log.debug("registering new device: " + name);
		if (devs.invokeAdd(prms)) {
			getResponse().setEntity("device created", MediaType.TEXT_PLAIN);
		} else {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}
	
	@Override
	public void handleDelete() {
		// we need at least the name of the device
		String name = (String) getRequest().getAttributes().get("device");
		if (null == name) {
			//log.debug(
				//	String.format(
					//		"missing parameter: name=%s",
						//	name
							//)
				//	);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}
		
		Devices devs = Core.getInstance().getDevices();
		if (!devs.containsDevice(name)) {
		//	log.debug(
			//		String.format(
				//			"No such device: %s",
					//		name
						//	)
				//	);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}
		//log.debug(String.format("remove device: %s", name));
		devs.removeDevice(name);
		getResponse().setEntity("device removed", MediaType.TEXT_PLAIN);
		getResponse().setStatus(Status.SUCCESS_OK);
	}

}