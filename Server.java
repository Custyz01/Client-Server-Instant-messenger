import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.ObjectOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Server extends JFrame {
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;
	
	public Server() {
		super("Server - Instant Messenger");
		createGUI();
		
	}
	
	private void createGUI() {
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							sendMessage(event.getActionCommand());
							userText.setText("");
						}
					}
				);
		add(userText, BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));
		setSize(300, 150);
		setVisible(true);
	}
	
	// Set up and run the server
	public void startRunning() {
		try {
			server = new ServerSocket(6789, 100);
			while(true) {
				try {
					waitForConnection();
					setupStreams();
					whileChatting();
				}catch(EOFException e) {
					showMessage("\n Server ended the connection_");
				}finally {
					closeEverything();
				}
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	// Wait for connection then display connection information
	private void waitForConnection() throws IOException {
		showMessage("\n Waiting for someone to connect... ");
		connection = server.accept();
		showMessage("\n Now connected to " + connection.getInetAddress().getHostName());
		
	}
	
	//Get stream to send and receive data
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are now setup!");
	}
	
	//During the conversation
	private void whileChatting() throws IOException{
		String message = "You are now connected!";
		sendMessage(message);
		ableToType(true);
		
		do {
			try {
				message = (String) input.readObject();
				showMessage("\n" + message);
			}catch(ClassNotFoundException e) {
				showMessage("\n IDK WHAT THE USER SENT BRO...");
			}
		}while(!message.equals("CLIENT - END"));
	}	


	//Close streams and sockets
	private void closeEverything() {
		showMessage("\n Closing connections... \n");
		ableToType(false);
		try {
			output.close();
			input.close();
			connection.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	// Send a message to client
	private void sendMessage(String message) {
		try {
			output.writeObject("SERVER - " + message);
			output.flush();
			showMessage("\n SERVER - " + message);
		}catch(IOException e){
			chatWindow.append("\n ERROR : CAN'T SEND THAT");
		}
	}

	// Updates chatWind
	private void showMessage(final String string) {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					chatWindow.append(string);
				}
			}
			
		);
	}

	// Lets the user type into their box
	private void ableToType(final boolean b) {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					userText.setEditable(b);
				}
			}
				
		);
	}

	

	

	
}
