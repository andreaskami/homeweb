package controlLayer.eventing;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * an event encapsulates a low level event triggered by a physical device. the
 * event stores the keyword(s) that triggered the event plus the event data
 * itself. With the two static methods encodeToJSON and decodeFromJSON you can
 * encode/decode the event to/from json.
 * 
 * @author sawielan
 * 
 */
public class Event {

	/** the keyword that triggered the event. */
	protected String keyword = null;

	/** the value delivered by the event FIXME::makeHigherLevel . */
	protected String value = null;

	/** when the event was triggered. */
	protected long time = -1;

	/** the name of the device which triggered the event. */
	protected String name = null;
	
	/** the location of the device triggering the event. */
	private String location = null;

	/** the name for the keyword parameter in the json. */
	public static final String KEYWORD = "kwd";

	/** the name for the value parameter in the json. */
	public static final String VALUE = "val";

	/** the name of the time parameter in the json. */
	public static final String TIME = "time";

	/** the name of the name parameter in the json. */
	public static final String NAME = "name";
	
	/** the name of the location parameter in the json. */
	public static final String LOCATION = "location";
	

	/**
	 * constructor of a event.
	 * 
	 * @param keyword a keyword fulfilled by this event.
	 * @param value the value of the event.
	 * @param name the name of the device that triggered the event.
	 */
	public Event(String keyword, String value, String name) {
		setKeyword(keyword);
		setValue(value);
		setTime(System.currentTimeMillis());
		setName(name);
	}
	
	/**
	 * constructor of a event.
	 * 
	 * @param keyword a keyword fulfilled by this event.
	 * @param value the value of the event.
	 * @param name the name of the device that triggered the event.
	 * @param location the location of the device triggering the event.
	 */
	public Event(String keyword, String value, String name, String location) {
		setKeyword(keyword);
		setValue(value);
		setTime(System.currentTimeMillis());
		setName(name);
		setLocation(location);
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the value encapsulating the event data.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param keyword the keyword to set
	 */
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	/**
	 * @return the keyword
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * @return the time when the event was triggered.
	 */
	public long getTime() {
		return time;
	}

	/**
	 * sets the time to the provided number.
	 * 
	 * @param time the time when the event was triggered.
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @return the name of the device which triggered the event.
	 */

	public String getName() {
		return name;
	}

	/**
	 * sets the name to the provided value.
	 * 
	 * @param name the name of the device which triggered the event.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @return a pretty print of the event.
	 */
	public String toString() {
		String str = "";
		str += "------------------------" + "\n";
		str += "name: " + name + "\n";
		str += "location: " + location + "\n";
		str += "time: " + new Date(time).toString() + "\n";
		str += "kwrd: " + keyword + "\n";
		str += "val: " + value + "\n";
		return str;
	}

	/**
	 * encodes a given event into a json object.
	 * @param event the event to encode.
	 * @return the json object representing the provided event.
	 * @throws JSONException when the event could not be encoded.
	 */
	public static JSONObject encodeToJSON(Event event) throws JSONException {

		JSONObject json = new JSONObject();
		json.append(KEYWORD, event.getKeyword());
		json.append(VALUE, event.getValue());
		json.append(TIME, event.getTime());
		json.append(NAME, event.getName());
		json.append(LOCATION, event.getLocation());

		return json;
	}
	
	/**
	 * encodes a given event into a json string.
	 * @param event the event to encode.
	 * @return the json string representing the provided event.
	 * @throws JSONException when the event could not be encoded.
	 */
	public static String encodeToJSONString(Event event) throws JSONException {
		JSONObject obj = encodeToJSON(event);
		return obj.toString();
	}

	/**
	 * decodes a json object into an event object.
	 * @param json the json object to decode.
	 * @return an event holding the parameters from the json object.
	 * @throws JSONException when the json string could not be decoded.
	 */
	public static Event decodeFromJSON(JSONObject json) throws JSONException {

		Event event = null;
		try {
			String keyword = json.getJSONArray(KEYWORD).getString(0);
			String value = json.getJSONArray(VALUE).getString(0);
			String name = json.getJSONArray(NAME).getString(0);
			String location = json.getJSONArray(LOCATION).getString(0);

			event = new Event(keyword, value, name, location);
			event.setTime(Long.parseLong(json.getJSONArray(TIME).getString(0)));

		} catch (Exception e) {
			if (e instanceof JSONException) {
				throw (JSONException) e;
			}
			throw new JSONException("missing entry");
		}

		return event;
	}

	/**
	 * decodes a json string into an event object.
	 * @param json the json string to decode.
	 * @return an event holding the parameters from the json string.
	 * @throws JSONException when the json string could not be decoded.
	 */
	public static Event decodeFromJSON(String json) throws JSONException {
		return decodeFromJSON(new JSONObject(json));
	}
}
