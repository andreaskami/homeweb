package presentationLayer.rest.wadl;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import presentationLayer.rest.devices.AbstractResource;

/**
 * Restlet Resource to invoke WADL Service Description file for tinyOS.
 * 
 * @author kami
 * 
 */
public class TinyOS extends AbstractResource {
	
	/**
	 * constructor of the resource.
	 * @param context the context to use.
	 * @param request the request to handle.
	 * @param response the response where to put the answer.
	 */
	public TinyOS(Context context, Request request,
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
		try {
			handleEveryting();
		} catch (IOException e) {
			System.err.println("WADL file does not exist.");
			//e.printStackTrace();
		}
	}

	@Override
	public void handlePut() {
		try {
			handleEveryting();
		} catch (IOException e) {
			System.err.println("WADL file does not exist.");
			//e.printStackTrace();
		}
	}

	@Override
	public void handleGet() {
		try {
			handleEveryting();
		} catch (IOException e) {
			System.err.println("WADL file does not exist.");
			//e.printStackTrace();
		}
	}
	
	@Override
	public void handleDelete() {
		try {
			handleEveryting();
		} catch (IOException e) {
			System.err.println("WADL file does not exist.");
			//e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public void handleEveryting() throws IOException {
	
		String xml = "";
		FileInputStream fstream;
		BufferedInputStream bufRead;
		DataInputStream wadlFile;
		
		Response response = getResponse();
		String filename = "wadl/tinyos.wadl";
		fstream  = new FileInputStream(filename);
		bufRead  = new BufferedInputStream(fstream);
		wadlFile = new DataInputStream(bufRead);
		
		while(wadlFile.available() != 0)
			xml = xml + wadlFile.readLine();

		if(xml != null){
			try {
				Representation rep = new StringRepresentation(xml, MediaType.APPLICATION_WADL_XML);

				response.setEntity(rep);
				getResponse().setStatus(Status.SUCCESS_OK);
				return;

			} catch (Exception e) {
				e.printStackTrace();
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
				return;
			}
		}
		else{
			String error = "There is no WADL Service Description file in this URL.";
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			getResponse().setEntity(new StringRepresentation(error));
		}
	}
}
