import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame{
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
	
	public Client(String host) {
		super("Client - Instant Messenger");
		serverIP = host;
		createGUI();
	}
	
	private void createGUI() {
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sendMessage(e.getActionCommand());
					userText.setText("");
				}
			}
		);
		add(userText, BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		setSize(300, 150);
		setVisible(true);
	}
	
	public void startRunning() {
		try {
			connectToServer();
			setupStreams();
			whileChatting();
		}catch(EOFException e) {
			showMessage("\n Client terminated connection.");
		}catch(IOException e) {
			e.printStackTrace();
		}finally {
			closeEverything();
		}
		
	}
	
	// Connects to server
	private void connectToServer() throws IOException {
		showMessage("Attemption connect... \n");
		connection = new Socket(InetAddress.getByName(serverIP), 6789);
		showMessage("Connected to: " + connection.getInetAddress().getHostName());
	}
	
	//Setup streams to send and receive messages
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are good to go!");
	}
	
	private void whileChatting() throws IOException{
		ableToType(true);
		do {
			try {
				message = (String) input.readObject();
				showMessage("\n" + message);
			}catch(ClassNotFoundException e) {
				showMessage("\n IDK WHAT THE SERVER SENT");
			}
		}while(!message.equals("SERVER - END"));
	}
	
	//Close the streams and sockets
	private void closeEverything() {
		showMessage("\n Closing everything");
		ableToType(false);
		try {
			output.close();
			input.close();
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Send messages to server
	private void sendMessage(String message) {
		try {
			output.writeObject("CLIENT - " + message);
			output.flush();
			showMessage("\n CLIENT - " + message);
		}catch(IOException e) {
			chatWindow.append("\n Something went wrong");
		}
	}
	
	//Updates the GUI to display a message
	private void showMessage(final String string) {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					chatWindow.append(string);
				}
			}
		);
	}
	
	//Gives the user permission to type
	private void ableToType(boolean b) {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					userText.setEditable(b);
				}
			}
		);
	}

	
}
