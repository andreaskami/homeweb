package presentationLayer.rest.eventing;

import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import controlLayer.Core;
import controlLayer.eventing.EventDescriptor;
import controlLayer.libraryCode.Constants;

/**
 * the client can post an event registration to this restlet. the registration 
 * will then be forwarded to the corresponding event handler plugin.
 * @author sawielan
 *
 */
public class RegisterEventingRestlet extends Restlet {
	
	/**
	 * standard constructor.
	 */
	public RegisterEventingRestlet() {
		super();
	}
	
	/**
	 * handle the request.
	 * @param request the request delivering the parameters needed.
	 * @param response the response to the client.
	 */
	public void handle(Request request, Response response) {
    	Form form = request.getEntityAsForm();
    	String leaseStr = form.getFirstValue("leasetime");
    	String callbackStr = form.getFirstValue("callback");
    	String kwrdStr = form.getFirstValue("keyword");
    	
    	if ((leaseStr != null) && (callbackStr !=null)  && (kwrdStr != null)) {
    		// we can go on.
    		// however we cannot guarantee that this gateway/device is able 
    		// to provide the requested keyword...
			EventDescriptor e = new EventDescriptor(
					callbackStr, 
					Long.parseLong(leaseStr), 
					kwrdStr,
					form
					);
			
			System.err.println("registering new event...");
			Core.getInstance().getEventManagement().register(e);
			System.err.println("registered new event");
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(Constants.ACK, MediaType.TEXT_PLAIN);
    	} else {
    		System.err.println("missing parameters...");
    		//inform the client about the bad request.
    		String mp = "";
    		if (leaseStr == null) {
    			mp += "\t- no leasetime provided [ex. leasetime=XYZ]\n";
    		}
    		if (callbackStr == null) {
    			mp += "\t- no callback provided [ex. callback=XYZ]\n";
    		}
    		if (kwrdStr == null) {
    			mp += "\t- no keyword provided [ex. keyword=XYZ]\n";
    		}
    		
    		response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    		response.setEntity(
    				"missing parameter(s):\n" + mp, 
    				MediaType.TEXT_PLAIN);
    	}
	}
}
