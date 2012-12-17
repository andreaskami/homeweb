package deviceLayer.smartMeters;
import java.sql.Time;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import java.util.StringTokenizer;

import deviceLayer.Device;
import deviceLayer.Devices;
/**
 * 
 * @author Massimo Dore
 * @version 1.0 2009
 *
 */
public class ParseBuffer {
	
    boolean cmddata = false;
    boolean enddata = false;
    int NumDevices = 0;
    Hashtable<String, PloggDevices> Hdev;
    
	public String getValue(String buff)
	{
	  int ix = buff.indexOf("=");
	  if (ix > -1) return buff.substring(ix+1, buff.length()); else
		  return null;
	}
	/**
	 * 
	 * @return pointer to plogg device hash table
	 */
	public Hashtable<String, PloggDevices> getHash()
	{
		return this.Hdev;
	}	
	/**
	 * 
	 */
	public ParseBuffer()
	{
		this.cmddata = false;
		this.enddata = false;
		this.NumDevices = 0;
		this.Hdev = new Hashtable<String, PloggDevices>();
	}
	public int NumDevices()
	{
		return this.NumDevices;
	}
	public PloggDevices getPlogg(String id)
	{
		PloggDevices pd = this.Hdev.get(id);
		return pd;
	}
	public String GetNumberFromString(String sInp)
	{
		String t = sInp.trim();
		int ix = t.indexOf(' ');
		if (ix > -1)
		    return(t.substring(0, ix));
		else return t;
	}
	public void exec(String buff, String ID)
	{
		StringTokenizer Tok = new StringTokenizer(buff,"~~");
        int n=0;
        boolean debug = false;
        
        if (debug) System.out.println(buff);
        PloggDevices p1 = null;
        String t = null;
        while (Tok.hasMoreElements())
        {
        	/*
        	 *  search elements
        	 */
        	    String Token = Tok.nextElement().toString();        	    
        	    t = getValue(Token);
        	    if (debug)	System.out.println( "" + ++n +": "+t);
        	    p1 = getPlogg(ID);
        	    if (t != null)
        	    {
        	    if (Token.indexOf("Time entry") > -1) 
        	    {
        	    	p1.SetTime(t, false);
        	    	if (t.length() < 20)
        	    		System.out.println("T:"+t+"*buff:"+buff);
        	    }
        	    
        	    else if (Token.indexOf("Watts (-Gen +Con)") > -1) 
   	    		  p1.SetWatts(GetNumberFromString(t));
        	    else if (Token.indexOf("Cumulative Watts (Con)") > -1) 
        	    	p1.SetCWatts(GetNumberFromString(t));
        	    
        	    else if (Token.indexOf("Cumulative Watts (Gen)") > -1) 
        	    {}
        	    else if (Token.indexOf("Frequency") > -1) 
    	    		{}
        	    else if (Token.indexOf("RMS Voltage") > -1) 
        	         p1.SetRms(GetNumberFromString(t));        	        	    		
        	    else if (Token.indexOf("RMS Current") > -1)
        	    {
    	    	// to do	
        	    }
        	    else if (Token.indexOf("Plogg on time") > -1)
        	    	p1.SetUpTime(t);       	    	    
        	    else if (Token.indexOf("Equipment on time") > -1) 
        	    {}
        	    else if (Token.indexOf("Unit friendly name") > -1) 
        	    {           	    	
  	    		    p1.SetName(t);
  	    		    System.out.println("Plogg Name : "+t+" ID: "+ID);    	    	   
        	    }
        	    
        	    }
        }
       
	}
	/**
	 * 
	 * @param buff the string tob searched for Plogg devices
	 * format : FFD:<16 byte id>
	 */
	public void ParseDevs(String buff)
	{
		int ix = buff.indexOf("FFD:");
		if (ix > -1)
		{
			/*
			 * add a new device
			 */
			String id = buff.substring(ix+4, ix+20);
			AddNewDevice(id);
		}
		
	}
	public void AddNewDevice(String id)
	{
		
		this.Hdev.put(id, new PloggDevices(id));
		this.NumDevices++; // set the number of discovered devices
	
	}
	
}


