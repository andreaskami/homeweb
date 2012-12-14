package simulation.eventing;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import controlLayer.eventing.AsynchronousEventReceiver;
import controlLayer.eventing.Event;


/**
 * a very simple GUI to receive events. you can specify two arguments on the 
 * console. the first argument is the port number where to listen for incoming 
 * events the default port is port 9994.
 * @author sawielan
 *
 */
public class ListenForEvents extends JFrame implements Observer {
	
	/** default serial version id.
	 */
	private static final long serialVersionUID = 3277964036504615895L;

	/** text area which contains the results */
	protected final JTextArea resultTextArea = new JTextArea(40, 40);
	
	/** the default port where to listen to events. */
	public static final int DEFAULT_PORT = 9994;
	
	/** the port where to listen for incoming events. */
	private int port = -1;
	
	/**
	 * constructor.
	 * @param port the port where to listen for events.
	 */
	public ListenForEvents(int port) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		resultTextArea.setEditable(false);
		
		add(resultTextArea);
		setSize(500, 500);
		
		setTitle("EventReceiver: port " + port);
		setVisible(true);
		
		this.port = port;
	}

	/**
	 * execute the event receiver.
	 * @throws Exception
	 */
	public void run() throws Exception {
		AsynchronousEventReceiver receiver = 
			new AsynchronousEventReceiver(port);
		new Thread(receiver).start();
		int nport = receiver.waitAndGetPort(this);
		if (nport == AsynchronousEventReceiver.ERROR_PORT) {
			throw new Exception("no free port found!");
		}
		this.port = nport;
		setTitle("EventReceiver: port " + port);
		System.err.println("receiver thread started on port: " + port);
	}

	/**
	 * receive an update from the observable.
	 * @param o the observable.
	 * @param arg the changed argument.
	 */
	public void update(Observable o, Object arg) {
		if (arg instanceof Event) {
			Event event = (Event) arg;
			
			// clear the text area...
			if (resultTextArea.getText().length() > 65500) {
				resultTextArea.setText("");
			}
			String text = event.toString()
				+ resultTextArea.getText();
			resultTextArea.setText(text);
		}
	}

	
	/**
	 * @param args console arguments.
	 * @throws Exception something goes wrong...
	 */
	public static void main(String[] args) throws Exception {
		
		int port = DEFAULT_PORT;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}

		new ListenForEvents(port).run();
	}
}
