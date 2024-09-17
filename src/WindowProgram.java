import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import se.miun.distsys.GroupCommunication;
import se.miun.distsys.listeners.ChatMessageListener;
import se.miun.distsys.listeners.JoinMessageListener;
import se.miun.distsys.messages.ChatMessage;
import se.miun.distsys.messages.JoinMessage;

import javax.swing.JButton;
import javax.swing.JTextPane;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JScrollPane;

//Skeleton code for Distributed systems

public class WindowProgram implements ChatMessageListener, JoinMessageListener, ActionListener {

	JFrame frame; // The window
	JTextPane txtpnChat = new JTextPane(); // The chat window
	JTextPane txtpnMessage = new JTextPane(); // The message window
	
	GroupCommunication gc = null;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WindowProgram window = new WindowProgram();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public WindowProgram() {
		String username = JOptionPane.showInputDialog(frame, "Enter your username :", "Username", JOptionPane.PLAIN_MESSAGE);

		// if(username == null || username.isEmpty()) {
		// 	System.out.println("Username cannot be empty");
		// 	System.exit(0);
		// }
		if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "You must provide a username to join the session !!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

		initializeFrame();

		gc = new GroupCommunication();
		gc.setChatMessageListener(this);
		gc.setJoinMessageListener(this);

		System.out.println("Group Communication Started");

		//String username = "User" + (int) (Math.random() * 1000);
		gc.sendJoinMessage(username);

	}

	private void initializeFrame() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane);
		scrollPane.setViewportView(txtpnChat);
		txtpnChat.setEditable(false);	
		txtpnChat.setText("--== Group Chat ==--");
		
		txtpnMessage.setText("Message");
		frame.getContentPane().add(txtpnMessage);
		
		JButton btnSendChatMessage = new JButton("Send Chat Message");
		btnSendChatMessage.addActionListener(this);
		btnSendChatMessage.setActionCommand("send");
		
		frame.getContentPane().add(btnSendChatMessage);
		
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
	        public void windowClosing(WindowEvent winEvt) {
	            gc.shutdown();
	        }
	    });
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equalsIgnoreCase("send")) {
			gc.sendChatMessage(txtpnMessage.getText());
		}		
	}
	
	@Override
	public void onIncomingChatMessage(ChatMessage chatMessage) {	
		txtpnChat.setText(chatMessage.chat + "\n" + txtpnChat.getText());				
	}

	@Override
	public void onIncomingJoinMessage(JoinMessage joinMessage) {
		txtpnChat.setText(joinMessage.chat + "\n" + txtpnChat.getText());
	}
}
