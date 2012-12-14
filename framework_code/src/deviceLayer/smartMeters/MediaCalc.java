package deviceLayer.smartMeters;

public class MediaCalc {

	/**
	 * This class is used to calculate the hourly average consume in KWH Take as
	 * parameters the Up time of Plogg device since last clear and the KWH
	 * counted
	 * 
	 * @param Datetime
	 *            in Plogg device format "d days hour:min:sec"
	 * @author Massimo Dore 2009
	 * 
	 */
	String data;
	float hours;

	/**
	 * @param Datetime
	 *            in Plogg device format "d days hour:min:sec"
	 */
	public MediaCalc(String Datetime) {
		if (!(Datetime.length() == 0))
			this.data = Datetime.trim();
		else
			Datetime = "";
	}

	/**
	 * @param none 
	 * @return Float the number of hours. The minutes are rounded to
	 * hour
	 */
	public Float CalcHours() {
		String days;
		String hour;
		String min;
		// 1 days 03:16:30 timestamp format
		this.hours = 0f;
		if (!(this.data.length() == 0)) {
			int ix = data.indexOf("days");
			if (ix > -1)
			{
			days = data.substring(0, ix - 1).trim();
			// System.out.println("days ="+days);
			
			int i1 = data.indexOf(":", ix + 4);
			if (i1 > -1){
				hour = data.substring(ix + 4, i1).trim();
			// System.out.println("Hours ="+hour);
				int i2 = data.indexOf(":", i1 + 1);
				if (i2 > -1)
				{
					min = data.substring(i1 + 1, i2).trim();
			// System.out.println("min ="+min);
					try {											
					float f = Float.parseFloat(days) * 24 + Float.parseFloat(hour);
					this.hours = f;
					} catch (NumberFormatException n)
					{
						return new Float(this.hours);
					}
					if (Integer.parseInt(min) > 40)
						this.hours++;
				}
				}
			}
		}
		return new Float(this.hours);
	}
}
