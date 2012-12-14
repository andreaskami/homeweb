package deviceLayer.Sensaris;
/*

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

*//**
 * Class tha represents the senspod object
 * @author harisefstathiades
 *
 *//*
public class SensPod {// extends Thread {

//	private final String GPRMC = "$GPRMC";
	private final String PSEN = "$PSEN";

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

	protected BluetoothDevice bluetoothDevice;
	protected String device_name;
	protected BluetoothSocket bluetoothSocket;
	protected InputStream bt_input;
	protected DataOutputStream bt_output;
	protected String device_address;
	protected String formattedBDADDR;

	*//**
	 * Constructor
	 * @param device - Bluetooth device
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 *//*
	public SensPod(BluetoothDevice device) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		// Use a temporary object that is later assigned to mmSocket,
		// because mmSocket is final
		bluetoothDevice = device;

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		device_name = bluetoothDevice.getName();

		Method method = bluetoothDevice.getClass().getMethod(
				"createInsecureRfcommSocket", new Class[] { int.class });
		bluetoothSocket = (BluetoothSocket) method.invoke(bluetoothDevice,
				new Object[] { 1 });
	}

	*//**
	 * Connect to a bluetooth device
	 *//*
	public void connect() throws Exception {
		bluetoothSocket.connect();
	}

	*//**
	 * Returns a string with actual sensor data
	 * 
	 * @throws IOException
	 *//*
	public String readDataFromSensor() throws IOException {
		bt_input = bluetoothSocket.getInputStream();
		String finalData = new String();
//		boolean stop = false;
		int i = 0;
		char temp;
		while (true) {
			temp= (char) (byte) bt_input.read();
			finalData += temp;
			if(temp=='\n')
				i++;
			if (finalData.contains("N*7") && i>=10)
				break;
		}
		parseData(finalData);
		return finalData;
	}

	*//**
	 * Parse String data to Object
	 * 
	 * @return
	 * @throws IOException
	 *//*

	private void parseData(String frame) {
		String[] buffer1 = frame.split("\r\n|\n");
		String[] buffer2 = null;
		for (int i = 0; i < buffer1.length; i++) {
			buffer2 = buffer1[i].split(",");
			if (buffer2.length > 1)
				if (buffer2[1].equals(PSEN)) {
					if (buffer2[2].equals("Hum")) {
						humidity=Double.parseDouble(buffer2[4]);
						temperature=Double.parseDouble(buffer2[6]);
					}
					else if (buffer2[2].equals("Batt")) {
						battery=Double.parseDouble(buffer2[4]);
					}
					else if (buffer2[2].equals("RTC")) {
						date=Long.parseLong(buffer2[4]);
						time=Long.parseLong(buffer2[6]);
					}
					else if (buffer2[2].equals("Noise")) {
						noise = Double.parseDouble(buffer2[4]);
					}
					else if (buffer2[2].equals("NOx")) {
						nox = Double.parseDouble(buffer2[4]);
					}
					else if (buffer2[2].equals("COx")) {
						cox = Double.parseDouble(buffer2[4]);
					}
					else if (buffer2[2].equals("CO2")) {
						co2 = Double.parseDouble(buffer2[4]);
					}else if (buffer2[2].equals(" UV")) {
						uv = Double.parseDouble(buffer2[4]);
					}else if (buffer2[2].equals(" O3")) {
						o3 = Double.parseDouble(buffer2[4]);
					}
				}
		}

	}

	*//**
	 * @return true if sensor is available
	 * @throws IOException
	 *//*
	public int available() throws IOException {
		return bt_input.available();
	}

	*//**
	 * 
	 * @return humidity value
	 *//*
	public double getHumidity() {
		return humidity;
	}

	*//**
	 * 
	 * @return temperature value
	 *//*
	public double getTemperature() {
		return temperature;
	}

	*//**
	 * 
	 * @return battery level
	 *//*
	public double getBattery() {
		return battery;
	}

	*//**
	 * 
	 * @return date from sensor
	 *//*
	public long getDate() {
		return date;
	}

	*//**
	 * 
	 * @return time from sensor
	 *//*
	public long getTime() {
		return time;
	}

	*//**
	 * 
	 * @return noise value
	 *//*
	public double getNoise() {
		return noise;
	}

	*//**
	 * 
	 * @return NOx value
	 *//*
	public double getNox() {
		return nox;
	}

	*//**
	 * 
	 * @return COx value
	 *//*
	public double getCox() {
		return cox;
	}

	*//**
	 * 
	 * @return CO2 value
	 *//*
	public double getCo2() {
		return co2;
	}

	*//**
	 * @return the uv value
	 *//*
	public double getUv() {
		return uv;
	}

	*//**
	 * @return the o3 value
	 *//*
	public double getO3() {
		return o3;
	}

}*/