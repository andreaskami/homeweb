package deviceLayer.smartMeters;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * This class store the values of Plogg measures
 * 
 * @author Massimo Dore 2009
 * @version 1.0
 * 
 */
public final class PloggDevices {
	
	private String id;
	private String Nome;
	private String Watts;
	private String CWatts;
	private String TimeStamp;
	private String Up;
	private String Hz;
	private String Rms;

    public PloggDevices(String id)
    {
    	this.id = id;
    	this.Nome = "";
    	this.Watts = "";
    	this.CWatts = "";
    	this.TimeStamp = "";
    	this.Up = "";
    }
    /**
     * 
     * @return the id of Plogg (String)
     */
    public String getId()
    {
    	return this.id;
    }
    /**
     * Set the Plogg name of device
     * @param name the Plogg name
     */
    public void SetName(String name)
    {
    	this.Nome = name;
    }
    /**
     * 
     * @param time the Plogg current time 
     * @param append flag to append string
     */
    public void SetTime(String time, boolean append)
    {
    	if (append)
    	this.TimeStamp += time;
    	else
    		this.TimeStamp = time;
    }
    /**
     * Get the Plogg time
     * @return the datetime string
     */
    public String GetTime()
    {
    	return this.TimeStamp;
    }
    /**
     * Set the Plogg uptime
     * @param up 
     */
    public void  SetUpTime(String up)
    {
    	if  (IsValidDate(up))
    		this.Up = up;
    	else
    		this.Up = "";
    }
    /**
     * Check if a date string is well formatted
     * @param inDate
     * @return
     */
    public boolean IsValidDate(String inDate)
    {
    	Locale loc = Locale.ENGLISH;
    	 if (inDate == null)
    	      return false;

    	    //set the format to use as a constructor argument
    	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MMM dd HH:mm:ss", loc);
    	    
    	    if (inDate.trim().length() != dateFormat.toPattern().length())
    	      return false;

    	    dateFormat.setLenient(false);
    	    
    	    try {
    	      //parse the inDate parameter
    	      dateFormat.parse(inDate.trim());    	     
    	    }
    	    catch (ParseException pe) {
    	      return false;
    	    }
    	    return true;
    }
    
    /**
     * 
     * @return the Plogg uptime
     */
    public String  GetUpTime()
    {
    	 return this.Up;
    }
    
    /**
     * 
     * @return the Plogg name
     */
    public String  GetName()
    {
    	return this.Nome;
    }
    
    /**
     * Set the frequency (hz)
     * @param hz String the frequency
     */
    public void SetFreq(String hz)
    {
    	this.Hz = hz;
    }
    
    /**
     * 
     * @return the frequency
     */
    public String GetFreq()
    {
    	return this.Hz ;
    }
    
    /**
     * 
     * @param wtt the rms voltage
     */
    public void SetRms(String wtt)
    {
    	this.Rms = wtt;
    }
    
    /**
     * 
     * @return the rms voltage
     */
    public String GetRms()
    {
    	return this.Rms ;
    }
    /**
     * 
     * @param wtt the accumulated watts
     */
    public void SetWatts(String wtt)
    {
    	this.Watts = wtt;
    }
    
    public String GetWatts()
    {
    	return this.Watts ;
    }
    
    public void SetCWatts(String wtt)
    {
    	this.CWatts = wtt;
    }
    
    public String GetCWatts()
    {
    	return this.CWatts ;
    }
}
