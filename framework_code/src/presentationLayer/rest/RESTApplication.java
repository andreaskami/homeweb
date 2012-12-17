package presentationLayer.rest;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;

import presentationLayer.rest.eventing.ReceiveEventRestlet;
import presentationLayer.rest.eventing.RegisterEventingRestlet;

import controlLayer.libraryCode.Constants;

/**
 * The main application in presentation layer for REST style
 * @author sawielan
 *
 */

public class RESTApplication extends Application {;

	/**
	 * constructor for the application.
	 * @param parentContext the context of the parent.
	 */
	public RESTApplication(Context parentContext) {
		super(parentContext);
	}
	
	/**
    * Creates a root Restlet that will receive all incoming calls.
	*/
	@Override
	public synchronized Restlet createRoot() {
		
		// Create a router Restlet that routes each call
		Router router = new Router(getContext());
		
		// eventing stuff
		router.attach(
				Constants.EVENTING_REGISTRATION,
				new RegisterEventingRestlet()
				);
		
		router.attach(
				Constants.EVENTING_SUBMIT_EVENT,
				new ReceiveEventRestlet()
				);
		
		// attach the delete facility
		router.attach(
				Constants.MODIFY_DEVICE + "/{device}",
				presentationLayer.rest.devices.ModifyDeviceResource.class
				);
		
		// attach the create facility
		router.attach(
				Constants.MODIFY_DEVICE,
				presentationLayer.rest.devices.ModifyDeviceResource.class
				);
		
		// attach the devices facility
		router.attach("/",
				presentationLayer.rest.devices.GatewayResource.class);
		
		// attach the devices facility
		router.attach("/devices",
				presentationLayer.rest.devices.DevicesResource.class);
		
		// attach the device link
		router.attach("/devices/{device}",
				presentationLayer.rest.devices.InvokeDeviceResource.class);
		
		// attach the general WADL link
		router.attach(Constants.WADL,
				presentationLayer.rest.wadl.WADL.class);
		
		// attach the WADL Service Description Data link for tinyOS
		router.attach(Constants.WADL_ROUTE_TINYOS,
				presentationLayer.rest.wadl.TinyOS.class);
		
		// attach the WADL Service Description Data link for tinyOSIPv6
		router.attach(Constants.WADL_ROUTE_TINYOS_IPv6,
				presentationLayer.rest.wadl.TinyOSIPv6.class);
		
		// attach the WADL Service Description Data link for Contiki
		router.attach(Constants.WADL_ROUTE_CONTIKI,
				presentationLayer.rest.wadl.Contiki.class);
		
		// attach the WADL Service Description Data link for Ploggs
		router.attach(Constants.WADL_ROUTE_PLOGG,
				presentationLayer.rest.wadl.Plogg.class);
		
		return router;
	}
	
	public Router getRouter() {
		return (Router) getRoot();
	}

}
