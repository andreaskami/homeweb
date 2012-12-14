package controlLayer.eventing;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import deviceLayer.Device;

/**
 * the event management is responsible to retrieve the events from the 
 * observables (basically the low level physical devices). it registers itself 
 * upon a client registration and deregisters when the registration is over. 
 * @author sawielan
 *
 */
public class EventMgmt implements Observer {
	
	/** a handle to the eventing plugin. */
	private Eventing eventing = null;
	
	/**
	 * constructor.
	 * @param eventing handle to the eventing plugin.
	 */
	public EventMgmt(Eventing eventing) {
		this.eventing = eventing;
	}

	/**
	 * registers itself on the provided device.
	 * @param device the device where to register.
	 */
	public void register(Device device) {
		device.addObserver(this);
	}
	
	/**
	 * deregisters itself from the provided device.
	 * @param device the device where to deregister.
	 */
	public void deregister(Device device) {
		device.deleteObserver(this);
	}
	
	/**
	 * this method will be called from the low level devices whenever a sensor 
	 * changes its state.
	 * @param o the device observed that triggered the event.
	 * @param arg the event data from the device.
	 */
	@SuppressWarnings("unchecked")
	public void update(Observable o, Object arg) {
		if (arg instanceof Event) {
			eventing.notifyEvent((Event) arg);
		} else if (arg instanceof List) {
			for (Object obj : (List) arg) {
				try {
					if (obj instanceof Event) {
						eventing.notifyEvent((Event) obj);
					}
				} catch (ClassCastException e) {
					System.err.println(e.getMessage());
				}
			}
		}
	}
}
