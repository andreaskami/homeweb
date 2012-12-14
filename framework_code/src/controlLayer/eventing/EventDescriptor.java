package controlLayer.eventing;

import org.restlet.data.Form;

/**
 * holds an event descriptor. if someone registers for an event this 
 * data structure gets created. if the timer gets started the registration will 
 * be possibly removed after the expiration of the lease time.  
 * @author sawielan
 *
 */
public class EventDescriptor extends Hashable {
	
	/** how long this registration is valid. */
	protected long leaseTime;
			
	/** the request form that submitted all the parameters. */
	protected Form requestForm;

	/**
	 * constructor.
	 * @param callback the callback where to deliver notifications.
	 * @param leaseTime how long this registration is valid.
	 * @param keyword the keyword this registration waits for.
	 * @param requestForm the request form that submitted all the parameters.
	 */
	public EventDescriptor(String callback, long leaseTime, 
			String keyword, Form requestForm) {

		super(keyword, callback);
		
		this.leaseTime = leaseTime;
		this.requestForm = requestForm;
	}
	
	/**
	 * @return the callback
	 */
	public String getCallback() {
		return callback;
	}

	/**
	 * @return the leaseTime
	 */
	public long getLeaseTime() {
		return leaseTime;
	}

	/**
	 * @return the keyword
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * @return the requestForm
	 */
	public Form getRequestForm() {
		return requestForm;
	}	
}
