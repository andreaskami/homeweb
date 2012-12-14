package simulation.eventing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.data.Status;

import controlLayer.libraryCode.Constants;


/**
 * post an event registration to a gateway.
 * @author sawielan
 *
 */
public class RegisterForEvent extends JFrame {

	/** the default serial version id. */
	private static final long serialVersionUID = -1076165359098348922L;

	/** text area which contains the results */
	private final JTextArea resultTextArea = new JTextArea(30, 30);
	
	/** the keyword to use for the subscription. */
	private JTextArea keywordArea = new JTextArea(4, 5);
	
	/** the host to call for the subscription. */
	private JTextArea hostNameArea = new JTextArea(4, 20);
	
	/** the callback (thats the URI to the event sink). */
	private JTextArea callbackArea = new JTextArea(4, 20);
	
	/** the lease time how long  the subscription is valid. */
	private JTextArea leaseTimeArea = new JTextArea(4, 5);
	
	/** the button to launch the subscription. */
	private JButton goButton = null;
	
	/** scroll pane which contains the result text area */
	private final JScrollPane resultScrollPane = new JScrollPane(resultTextArea, 
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
	/**
	 * constructor.
	 */
	public RegisterForEvent() {
		initializeGUI();
	}
	
	/**
	 * setup the GUI.
	 */
	private void initializeGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 150);
		setTitle("PutEvent");
		
		resultTextArea.setEditable(false);
		
		setLayout(new BorderLayout());
		add(createCommandPanel(), BorderLayout.NORTH);
		add(resultScrollPane, BorderLayout.SOUTH);
		
		add(resultTextArea);
		
		setVisible(true);
	}
	
	/**
	 * display the results.
	 * @param result the results string.
	 */
	private void showResult(String result) {
		resultTextArea.setText(result);
	}
	
	/**
	 * constructs the command panel.
	 * @return the JPanel holding the command panel.
	 */
	private JPanel createCommandPanel() {
		JPanel commandPanel = new JPanel();
		keywordArea.setText("fire");
		commandPanel.add(keywordArea);
		hostNameArea.setText("http://localhost:8080" 
				+ Constants.EVENTING_REGISTRATION);
		commandPanel.add(hostNameArea);
		leaseTimeArea.setText("6000");
		commandPanel.add(leaseTimeArea);
		callbackArea.setText("http://localhost:" + ListenForEvents.DEFAULT_PORT + 
				Constants.EVENTING_SUBMIT_EVENT);
		commandPanel.add(callbackArea);
		
		goButton = new JButton();
		goButton.setText("execute");
		goButton.addActionListener(
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						String keyword = keywordArea.getText();
						String url = hostNameArea.getText();
						String cb = callbackArea.getText();
						String lt = leaseTimeArea.getText();
						try {
							Client client = new Client(Protocol.HTTP);
							Form form = new Form();
							form.add("leasetime", lt);
							form.add("callback", cb);
							form.add("keyword", keyword);
							Response response = client.post(
									url, 
									form.getWebRepresentation());
							
							if (response.getStatus().getCode() != 
								Status.SUCCESS_OK.getCode()) {
							
								String text = response.getEntity().getText();
								if (text != null) {
									resultTextArea.setText(text);
								} else {
									resultTextArea.setText(
											"the server did not " +
										"understand the request");
								}
							} else {
								resultTextArea.setText(
									response.getEntity().getText());
							}
											
						} catch (Exception e1) {
							showResult(e1.toString());
						}
					}
					
				}
		);
		
		commandPanel.add(goButton);
		return commandPanel;
	}
	
	/**
	 * @param args the system arguments.
	 */
	public static void main(String[] args) {
		new RegisterForEvent();
	}

}
