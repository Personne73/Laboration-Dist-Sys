import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import se.miun.distsys.GroupCommunication;
import se.miun.distsys.listeners.ChatMessageListener;
import se.miun.distsys.listeners.JoinMessageListener;
import se.miun.distsys.listeners.ActiveUserListener;
import se.miun.distsys.messages.ChatMessage;
import se.miun.distsys.messages.JoinMessage;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Map;

import javax.swing.JScrollPane;

//Skeleton code for Distributed systems

public class WindowProgram implements ChatMessageListener, JoinMessageListener, ActiveUserListener, ActionListener {

	JFrame frame; // The window
	JTextPane txtpnChat = new JTextPane(); // The chat window
	JTextPane txtpnMessage = new JTextPane(); // The message window

	JList<String> userList; // The list of users
	DefaultListModel<String> listModel; // The list model
	
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

		gc = new GroupCommunication(username);
		gc.setChatMessageListener(this);
		gc.setJoinMessageListener(this);
		gc.setActiveUserListener(this);

		System.out.println("Group Communication Started");

		//String username = "User" + (int) (Math.random() * 1000);
		gc.sendJoinMessage(username);

	}

	private void initializeFrame() {
		// // Create the main window
		// frame = new JFrame();
		// frame.setBounds(100, 100, 450, 300);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// frame.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		
		// // Add the chat area
		// JScrollPane scrollPane = new JScrollPane();
		// frame.getContentPane().add(scrollPane);
		// scrollPane.setViewportView(txtpnChat);
		// txtpnChat.setEditable(false);	
		// txtpnChat.setText("--== Group Chat ==--");
		
		// // Add the message area
		// txtpnMessage.setText("Message");
		// frame.getContentPane().add(txtpnMessage);
		
		// // Add the send button
		// JButton btnSendChatMessage = new JButton("Send Chat Message");
		// btnSendChatMessage.addActionListener(this);
		// btnSendChatMessage.setActionCommand("send");
		
		// frame.getContentPane().add(btnSendChatMessage);
		
		// // Add a window listener to handle the window close event
		// frame.addWindowListener(new java.awt.event.WindowAdapter() {
	    //     public void windowClosing(WindowEvent winEvt) {
	    //         gc.shutdown();
	    //     }
	    // });

		// Create the window
		frame = new JFrame();
		frame.setBounds(100, 100, 600, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		
		// Main container
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		// Chat area
		txtpnChat.setEditable(false);	
		txtpnChat.setText("--== Group Chat ==--");
		JScrollPane chatScrollPane = new JScrollPane(txtpnChat);
		mainPanel.add(chatScrollPane, BorderLayout.CENTER);

		// Message area container
		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BorderLayout());

		// Message area
		txtpnMessage.setText("");
		JScrollPane messageScrollPane = new JScrollPane(txtpnMessage);
		messageScrollPane.setPreferredSize(new Dimension(0, 50));
		messagePanel.add(messageScrollPane, BorderLayout.CENTER);

		// Send button
		JButton btnSendChatMessage = new JButton("Send");
		btnSendChatMessage.addActionListener(this);
		btnSendChatMessage.setActionCommand("send");
        btnSendChatMessage.setPreferredSize(new Dimension(100, 50)); // Ajustez la taille si nécessaire
        messagePanel.add(btnSendChatMessage, BorderLayout.EAST);

		// Add the message panel to the main panel
		mainPanel.add(messagePanel, BorderLayout.SOUTH);

		// Add the main panel to the frame
		frame.getContentPane().add(mainPanel, BorderLayout.CENTER);

		// Users list
		listModel = new DefaultListModel<String>();
		userList = new JList<String>(listModel);
		userList.setBorder(BorderFactory.createTitledBorder("Active Users"));
		JScrollPane userListScrollPane = new JScrollPane(userList);
		userListScrollPane.setPreferredSize(new Dimension(150, 0));

		frame.getContentPane().add(userListScrollPane, BorderLayout.EAST);

		// Add a window listener to handle the window close event
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
	        public void windowClosing(WindowEvent winEvt) {
	            gc.shutdown();
	        }
	    });
	}

	// public void updateUsersList(Map<String, String> activeUsers) {
	// 	SwingUtilities.invokeLater(new Runnable() {
	// 		@Override
	// 		public void run() {
	// 			listModel.clear();
	// 			for (String username : activeUsers.values()) {
	// 				listModel.addElement(username);
	// 			}
	// 		}
	// 	});
	// }

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

	@Override
	public void onActiveUserListChanged(Map<String, String> activeUsers) {
		// listModel.clear();
		// for (String username : activeUsers.values()) {
		// 	listModel.addElement(username);
		// }
		listModel.clear();
		for (Map.Entry<String, String> entry : activeUsers.entrySet()) {
			String userId = entry.getKey();
			String username = entry.getValue();
			String displayName = username + " (ID: " + userId + ")";
			listModel.addElement(displayName);
		}
	}
}
