package deviceLayer.Sensaris;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

/**
 * Class tha represents the senspod object
 * 
 * @author Harris-Theo
 * 
 */
public class senspodDevice implements SerialPortEventListener {// extends Thread
																// {
	final int INTERVAL=4000; // 4 seconds
	// private final String GPRMC = "$GPRMC";
	private final String PSEN = "$PSEN";
	private String portName;
	private double humidity = -1;
	private double temperature = -1;
	private double battery = -1;
	private long date = -1;
	private long time = -1;
	private double noise = -1;
	private double nox = -1;
	private double cox = -1;
	private double co2 = -1;
	private double uv = -1;
	private double o3 = -1;

	protected String device_name;
	protected SerialPort serialPort;
	protected InputStream inputStream;
	protected DataOutputStream sp_output;
	protected String device_address;
	protected String formattedBDADDR;
	protected CommPortIdentifier portId;
	protected Enumeration portList;

	/**
	 * Constructor
	 * 
	 * @param device
	 *            - Serial Port device
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public senspodDevice(String port) {
		portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (portId.getName().equals(port)) {
					// if (portId.getName().equals("/dev/term/a")) {
					SimpleRead();
				}
			}
		}
		portName=port;
	}

	public void SimpleRead() {
		try {
			serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);
		} catch (PortInUseException e) {
			System.out.println(e);
		}
		try {
			inputStream = serialPort.getInputStream();
		} catch (IOException e) {
			System.out.println(e);
		}
		try {
			serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			System.out.println(e);
		}
		serialPort.notifyOnDataAvailable(true);
		try {
			serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
			System.out.println(e);
		}
	

	}
	
	
	public void serialEvent(SerialPortEvent event) {
        switch(event.getEventType()) {
        case SerialPortEvent.BI:
        case SerialPortEvent.OE:
        case SerialPortEvent.FE:
        case SerialPortEvent.PE:
        case SerialPortEvent.CD:
        case SerialPortEvent.CTS:
        case SerialPortEvent.DSR:
        case SerialPortEvent.RI:
        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
            break;
        case SerialPortEvent.DATA_AVAILABLE:
            

            try {
            	String finalData = new String();
        		// boolean stop = false;
        		int i = 0;
        		char temp;
                while (inputStream.available() > 0) {
                	temp = (char) (byte) inputStream.read();
        			finalData += temp;
        			if (temp == '\n')
        				i++;
        			if (finalData.contains("N*7") && i >= 10)
        				break;
                }
                System.out.print(new String(portName +": "+ finalData));
                parseData(finalData);
                try {
					Thread.sleep(INTERVAL);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } catch (IOException e) {System.out.println(e);}
            break;
        }
    }
	
	



	/**
	 * Parse String data to Object
	 * 
	 * @return
	 * @throws IOException
	 */

	private void parseData(String frame) {
		String[] buffer1 = frame.split("\r\n|\n");
		String[] buffer2 = null;
		for (int i = 0; i < buffer1.length; i++) {
			buffer2 = buffer1[i].split(",");
			if (buffer2.length > 1)
				if (buffer2[1].equals(PSEN)) {
					if (buffer2[2].equals("Hum")) {
						humidity = Double.parseDouble(buffer2[4]);
						temperature = Double.parseDouble(buffer2[6]);
					} else if (buffer2[2].equals("Batt")) {
						battery = Double.parseDouble(buffer2[4]);
					} else if (buffer2[2].equals("RTC")) {
						date = Long.parseLong(buffer2[4]);
						time = Long.parseLong(buffer2[6]);
					} else if (buffer2[2].equals("Noise")) {
						noise = Double.parseDouble(buffer2[4]);
					} else if (buffer2[2].equals("NOx")) {
						nox = Double.parseDouble(buffer2[4]);
					} else if (buffer2[2].equals("COx")) {
						cox = Double.parseDouble(buffer2[4]);
					} else if (buffer2[2].equals("CO2")) {
						co2 = Double.parseDouble(buffer2[4]);
					} else if (buffer2[2].equals(" UV")) {
						uv = Double.parseDouble(buffer2[4]);
					} else if (buffer2[2].equals(" O3")) {
						o3 = Double.parseDouble(buffer2[4]);
					}
				}
		}

	}

	
	/**
	 * 
	 * @return humidity value
	 */
	public double getHumidity() {
		return humidity;
	}

	/**
	 * 
	 * @return temperature value
	 */
	public double getTemperature() {
		return temperature;
	}

	/**
	 * 
	 * @return battery level
	 */
	public double getBattery() {
		return battery;
	}

	/**
	 * 
	 * @return date from sensor
	 */
	public long getDate() {
		return date;
	}

	/**
	 * 
	 * @return time from sensor
	 */
	public long getTime() {
		return time;
	}

	/**
	 * 
	 * @return noise value
	 */
	public double getNoise() {
		return noise;
	}

	/**
	 * 
	 * @return NOx value
	 */
	public double getNox() {
		return nox;
	}

	/**
	 * 
	 * @return COx value
	 */
	public double getCox() {
		return cox;
	}

	/**
	 * 
	 * @return CO2 value
	 */
	public double getCo2() {
		return co2;
	}

	/**
	 * @return the uv value
	 */
	public double getUv() {
		return uv;
	}

	/**
	 * @return the o3 value
	 */
	public double getO3() {
		return o3;
	}

}