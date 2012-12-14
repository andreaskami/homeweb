package deviceLayer.smartMeters;

public class PrepString {
	private String result = "";
	private boolean bInit = false;
	private boolean endcmd = false;
	private String DevID = "";

	/**
	 * Compose a string coming from the zigbee device in order to parse the content.
	 * When you send an <i>UCAST:PloggId=sv</i> command to a Plogg device
	 * the answer is a variable number of lines of 88 bytes.
	 * The measures are reported in verbose mode.
	 * This function searches first occurrence of  string  '~~'  and ignore each character before
	 * Then concat every line coming from serial port, replacing each \n \r character.
	 * The function searches the '~~~~' string to set end message flag (endcmd)
	 * .Also set the Plogg device ID 
	 * @param bf the string to be formatted
	 */
	public void getText(String bf)
	{
	 /*
	  *  Format the text without newline and linefeed 
	  */
		int pos;
		if (!bInit) {
		    pos = bf.indexOf("~~");
		    if (pos > 0)
		    {
		    	int ix = 0;
		    	ix = bf.indexOf(':');
		    	//get device id for further parsing
		    	this.DevID = bf.substring(ix+1, pos - 1);		    	
		    	this.result = bf.substring(pos, bf.length());		    	
		    	this.bInit = true;
		    }
		  }  else
		  {
			 
			 pos = bf.indexOf("=");
			 if (pos > 0)
			     this.result = this.result + bf.substring(pos+1, bf.length());
			 pos = bf.indexOf("~~~~");
			 
			 if (pos > 0) 
				 {
				    this.endcmd = true;
				 }
		  }		
	}
	/**
	 * 
	 * @return the string processed
	 */
	public String  printStr()
	{
		final boolean debug = false;
		if (debug) System.out.println(this.result);
		return this.result;
	}
	/**
	 * 
	 * @return boolean true if end of message has been detected
	 */
	public boolean isEndString()
	{
		return this.endcmd;
	}
	/**
	 * Initialize the class
	 */
	public void init()
	{
	  this.endcmd = false;
	  this.bInit = false;
	  this.DevID = "";
	}
	/**
	 * 
	 * @return string the Plogg ID
	 */
	public String getId()
	{
		return this.DevID;
	}

}
