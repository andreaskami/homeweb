package presentationLayer.rest.wadl;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;

import org.restlet.Context;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import presentationLayer.rest.devices.AbstractResource;

import deviceLayer.Device;
import deviceLayer.Devices;

/**
 * Restlet Resource to invoke a call on a device.
 * 
 * @author sawielan
 * 
 */
public class WADL extends AbstractResource {
	
	/**
	 * constructor of the resource.
	 * @param context the context to use.
	 * @param request the request to handle.
	 * @param response the response where to put the answer.
	 */
	public WADL(Context context, Request request,
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
		String msg = "WADL Service Description Data File Repository.\n ";
		msg       += "Currently available for:\n\n"; 
		msg       += "-tinyOS Client Software for telosb/tmote sky Sensor Motes (/tinyos/).\n";
		msg       += "-Contiki Client Software for telosb/tmote sky Sensor Motes (/contiki/).\n";
		Response response = getResponse();
		
		Representation rep = new StringRepresentation(msg, MediaType.TEXT_PLAIN);

		response.setEntity(rep);
		getResponse().setStatus(Status.SUCCESS_OK);
		return;
	}
}
