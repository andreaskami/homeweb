package controlLayer.eventing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * container to hold registrations of clients to a specific keyword. 
 * @author sawielan
 *
 */
public class Registrations {
	
	/** handle to the eventing module. */
	private final Eventing eventing;
	
	/** the keyword that is managed by this container. */
	private final String keyword;
	
	/** a list of all the event registrations currently on the gateway. */
	private Map<String, Registration> registrations  = 
		new ConcurrentHashMap<String, Registration> (); 
	
	/**
	 * constructor.
	 * @param eventing handle to the eventing module.
	 * @param keyword the keyword that is managed by this container.
	 */
	public Registrations(Eventing eventing, String keyword) {
		this.eventing = eventing;
		this.keyword = keyword;
	}
	
	/**
	 * @return the keyword managed by this container.
	 */
	public String getKeyWord() {
		return keyword;
	}
	
	/**
	 * @return link to the eventing.
	 */
	public Eventing getEventing() {
		return eventing;
	}
	
	/**
	 * register a new client for this keyword. if the client is already 
	 * registered then we just update the leasetime.
	 * @param r the client registration.
	 */
	public void register(EventDescriptor r) {
		if (registrations.containsKey(r.getHash())) {
			// just refresh the lease time
			registrations.get(r.getHash()).renewLeaseTime(
					r.getLeaseTime()
					);
			
		} else {
			// create a new entry.
			synchronized (registrations) {
				
				Registration reg = new Registration(
						this,
						r.getLeaseTime(),
						r.getCallback()
						);
				
				registrations.put(r.getHash(), reg);
				reg.startTimer();
			}
		}
	}
	
	/**
	 * remove a client from the registration.
	 * @param r the client to remove.
	 */
	public void deregister(Registration r) {
		synchronized (registrations) {
			registrations.remove(r.getHash());
			
			if (registrations.size() == 0) {
				eventing.deregister(this);
			}
		}
	}
	
	/**
	 * handles an incoming event. the event gets dispatched to all the 
	 * clients that are registered.
	 * @param event the event to dispatch to the clients.
	 */
	public void notifyEvent(Event event) {
		for (Registration r : registrations.values()) {
			r.notifyEvent(event);
		}
	}
}
