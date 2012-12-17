package deviceLayer;
import java.io.IOException;
import net.tinyos.message.Message;

// Abstract class Driver indicates the interface of the modules implementing low-level communication with Smart Devices
public abstract class Driver {

	protected Devices devices;	// list of Devices, for which Driver is responsible for communication and interaction
	
	// Driver constructor
	protected Driver(Devices devices) {
		this.devices = devices;
	}
	
	// all the Driver's initializations are performed inside this function
	public abstract void startDevice();
	
	// the event-triggered procedure of receiving a new message from a Smart Device
	public synchronized void messageReceived(int dest_addr, Message msg) {}
	
	// the event-triggered procedure of receiving a new message from a Smart Device
	public synchronized void messageReceived(String message) throws IOException {}
	
	// the module which is responsible for sending a message in DISCOVERY and DESCRIPTION mode
    public abstract void sendMessage(String nodeid, char message_type, String content) throws IOException;
	
    // the module which is responsible for sending a Service Request message in OPERATION mode
    public abstract void sendMessage(String nodeid, char message_type, Request request) throws IOException;
}
