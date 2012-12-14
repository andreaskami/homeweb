package presentationLayer.rest.eventing;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import controlLayer.Core;
import controlLayer.eventing.Event;
import controlLayer.eventing.Eventing;


/**
 * Receives an event from another gateway instance and delivers the event to 
 * the eventing plugin.
 * @author sawielan
 *
 */
public class ReceiveEventRestlet extends Restlet {
	
	/**
	 * process the request with the provided event.
	 * @param request the request delivering the event.
	 * @param response the response to return the response code.
	 */
	public void handle(Request request, Response response) {
		System.err.println("An event has been received.");
		Object obj;
		try {
			obj = request.getEntity().getText();
		} catch (IOException e) {
			System.err.println("could not extract entity from body:\n" + e.getCause());
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}
		if (obj instanceof String) {
			Event event = null;
			try {
				event = 
					Event.decodeFromJSON(
							new JSONObject(
									(String) obj
									)
							);
			} catch (JSONException e) {
				System.err.println("could not load json:\n" + e.getCause());
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return;
			}
			
			// notify the other Gateways about the event that has been received
			Core.getInstance().getEventManagement().notifyEvent(event);
			response.setStatus(Status.SUCCESS_OK);
			return;
		}
		response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	}
}
