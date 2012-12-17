package deviceLayer.sensorMotes;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

// it is being used extensively by Contiki Driver, in order to communicate with Sink Sensor, through the serial connection 
public abstract class ContikiSerialConn {

	// location of the scripts used to support serial connection
	private static final String SERIALDUMP_WINDOWS = "./tools/serialdump-windows.exe";
	private static final String SERIALDUMP_LINUX = "./tools/serialdump-linux";

	// serial connection general characteristics
	private String comPort;
	private Process serialDumpProcess;
	private PrintWriter serialOutput;
	protected boolean isOpen;
	private boolean hasOpened;
	protected boolean isClosed = true;
	protected String lastError;
	
	protected abstract void serialData(String line) throws IOException;

	/* used to open comPort and listen to messages on that serial communication port */
	public void open(String comPort) {
		System.out.println("Trying to open Serial connection port...");
		if (comPort == null) {
			throw new IllegalStateException("no com port");
		}
		//close();
		this.comPort = comPort;

		/* Connect to COM using external serialdump application */
		String osName = System.getProperty("os.name").toLowerCase();
		String fullCommand;
		if (osName.startsWith("win")) {
			fullCommand = SERIALDUMP_WINDOWS + " " + "-b115200" + " " + getMappedComPortForWindows(comPort);
		} else {
			fullCommand = SERIALDUMP_LINUX + " " + "-b115200" + " " + comPort;
		}

		isClosed = false;
		try {
			String[] cmd = fullCommand.split(" ");

			serialDumpProcess = Runtime.getRuntime().exec(cmd);
			final BufferedReader input = new BufferedReader(new InputStreamReader(serialDumpProcess.getInputStream()));
			final BufferedReader err = new BufferedReader(new InputStreamReader(serialDumpProcess.getErrorStream()));
			serialOutput = new PrintWriter(new OutputStreamWriter(serialDumpProcess.getOutputStream()));

			/* Start thread listening on stdout */
			Thread readInput = new Thread(new Runnable() {
				public void run() {
					String line;
					System.out.println("Trying to read Input from Sensors...");
					try {
						while ((line = input.readLine()) != null) {
							serialData(line);
						}
						input.close();
						System.out.println("Serialdump process terminated.");
						closeConnection();
					} catch (IOException e) {
						lastError = "Error when reading from serialdump process: " + e;
						System.err.println(lastError);
						if (!isClosed) {
							e.printStackTrace();
							closeConnection();
						}
					}
				}
			}, "read input stream thread");

			/* Start thread listening on stderr */
			Thread readError = new Thread(new Runnable() {
				public void run() {
					String line;
					try {
						while ((line = err.readLine()) != null) {
							if (!isOpen && line.startsWith("connecting") && line.endsWith("[OK]")) {
								isOpen = true;
								serialOpened();
								System.out.println("Serial port opened.");
							} else {
								System.err.println("Serialdump error stream> " + line);
							}
						}
						err.close();
					} catch (IOException e) {
						if (!isClosed) {
							System.err.println("Error when reading from serialdump process: " + e);
							e.printStackTrace();
						}
					}
				}
			}, "read error stream thread");

			readInput.start();
			readError.start();
      
		} catch (Exception e) {
			lastError = "Failed to execute '" + fullCommand + "': " + e;
			System.err.println(lastError);
			e.printStackTrace();
			closeConnection();
		}
	}

	private String getMappedComPortForWindows(String comPort) {
		if (comPort.startsWith("COM")) {
			comPort = "/dev/com" + comPort.substring(3);
		}
		return comPort;
	}

	/*  sends data to the sensor connected to the serial, which is used as Base Station */
	public void writeSerialData(String data) {
		PrintWriter serialOutput = this.serialOutput;
		if (serialOutput != null) {
			serialOutput.println(data);
			serialOutput.flush();
		}
	}

	/* closes the connection */
	public void close() {
		isClosed = true;
		lastError = null;
		closeConnection();
	}

	/* closes the serial connection */
	protected void closeConnection() {
		isOpen = false;
		if (serialOutput != null) {
			serialOutput.close();
			serialOutput = null;
		}
		if (serialDumpProcess != null) {
			serialDumpProcess.destroy();
			serialDumpProcess = null;
		}
		serialClosed();
	}
	
	/* sets Serial Port as opened */
	protected void serialOpened(){
		hasOpened = true;
	}

	/* terminates serial connection if it was open */
	protected  void serialClosed(){
		String comPort = getComPort();
		if (hasOpened) {
			System.out.println("Serial connection terminated.");
			hasOpened = false;
		}else {
			System.out.println("Closing... Failed to connect to " + getComPort() + ".");
		}
		if (!isClosed) {
          // Select new serial port
          //comPort = MoteFinder.selectComPort(window);
          if (comPort == null) {
          	System.out.println("No communication Port available.");
          }
      }	    
	}
	
	// Getter & Setter Functions
	
	public boolean isOpen() {
		return isOpen;
	}

	public String getComPort() {
		return comPort;
	}

	public void setComPort(String comPort) {
		this.comPort = comPort;
	}

	public String getLastError() {
		return lastError;
	}

}
