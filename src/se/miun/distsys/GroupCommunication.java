package se.miun.distsys;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import se.miun.distsys.listeners.ActiveUserListener;
import se.miun.distsys.listeners.ChatMessageListener;
import se.miun.distsys.listeners.JoinMessageListener;
import se.miun.distsys.listeners.LeaveMessageListerner;
import se.miun.distsys.messages.ChatMessage;
import se.miun.distsys.messages.JoinMessage;
import se.miun.distsys.messages.LeaveMessage;
import se.miun.distsys.messages.UserInfoMessage;
import se.miun.distsys.messages.Message;
import se.miun.distsys.messages.MessageSerializer;

public class GroupCommunication {
	
	private final int datagramSocketPort = 1802; //You need to change this!
	DatagramSocket datagramSocket = null;
	boolean runGroupCommunication = true;
	MessageSerializer messageSerializer = new MessageSerializer();

	//Listeners
	ChatMessageListener chatMessageListener = null;	
	JoinMessageListener joinMessageListener = null;
	LeaveMessageListerner leaveMessageListerner = null;
	ActiveUserListener activeUserListener = null;

	// activeUsers list to keep track of users in the chat
	// key: userId, value: username
	private Map<String, String> activeUsers = new HashMap<>();
	private String ownUserId;
	private String ownUsername;
	
	public GroupCommunication(String ownUsername) {
		try {
			datagramSocket = new MulticastSocket(datagramSocketPort);
						
			ReceiveThread rt = new ReceiveThread();
			rt.start();

			// store own username and userId
			this.ownUsername = ownUsername;
			this.ownUserId = java.util.UUID.randomUUID().toString();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getOwnUserId() {
		return ownUserId;
	}

	public void shutdown() {
		runGroupCommunication = false;
	}
	

	class ReceiveThread extends Thread{
		Random r = new Random();
		int chanceToDropPackets = 0;

		@Override
		public void run() {
			byte[] buffer = new byte[65536];		
			DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
			
			while(runGroupCommunication) {
				try {
					datagramSocket.receive(datagramPacket);										
					byte[] packetData = datagramPacket.getData();					
					Message receivedMessage = messageSerializer.deserializeMessage(packetData);

					if(receivedMessage instanceof ChatMessage && r.nextInt(100) < chanceToDropPackets){
						System.out.println("Dropped packet during chat message");
					} else if (receivedMessage instanceof JoinMessage && r.nextInt(100) < chanceToDropPackets){
						System.out.println("Dropped packet during join message");
					} else if (receivedMessage instanceof LeaveMessage && r.nextInt(100) < chanceToDropPackets){
						System.out.println("Dropped packet during leave message");
					} else if (receivedMessage instanceof UserInfoMessage && r.nextInt(100) < chanceToDropPackets){
						System.out.println("Dropped packet during user info message");
					} else {
						handleMessage(receivedMessage);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
				
		// Method to handle incoming messages
		private void handleMessage (Message message) {
			
			if(message instanceof ChatMessage) {
				ChatMessage chatMessage = (ChatMessage) message;
				if(chatMessageListener != null){
					chatMessageListener.onIncomingChatMessage(chatMessage);
				}
			} else if (message instanceof JoinMessage) {
				JoinMessage joinMessage = (JoinMessage) message;
				
				// add user to activeUsers list
				activeUsers.put(joinMessage.userId, joinMessage.username);

				// notify listeners that active users list has changed
				if (activeUserListener != null) {
					activeUserListener.onActiveUserListChanged(activeUsers);
				}

				// send own user info to chat
				sendUserInfoMessage();

				// send join message to chat
				if(joinMessageListener != null){
					joinMessageListener.onIncomingJoinMessage(joinMessage);
				}
			} else if (message instanceof LeaveMessage) {
				LeaveMessage leaveMessage = (LeaveMessage) message;

				// remove user from activeUsers list
				activeUsers.remove(leaveMessage.userId);

				// notify listeners that active users list has changed
				if (activeUserListener != null) {
					activeUserListener.onActiveUserListChanged(activeUsers);
				}

				// send leave message to chat
				if(leaveMessageListerner != null){
					leaveMessageListerner.onIncomingLeaveMessage(leaveMessage);
				}
			} else if (message instanceof UserInfoMessage) {
				UserInfoMessage userInfoMessage = (UserInfoMessage) message;
				System.out.println("Received UserInfoMessage: userId=" + userInfoMessage.userId + ", username=" + userInfoMessage.username);

				// add user to activeUsers list if not own user
				if (!userInfoMessage.userId.equals(ownUserId)) {
					activeUsers.put(userInfoMessage.userId, userInfoMessage.username);
				}

				// notify listeners that active users list has changed
				if (activeUserListener != null) {
					activeUserListener.onActiveUserListChanged(activeUsers);
				}
			} else {				
				System.out.println("Unknown message type");
			}
		}
	}	
	
	public void sendChatMessage(String chat) {
		try {
			ChatMessage chatMessage = new ChatMessage(chat, ownUsername, ownUserId);
			byte[] sendData = messageSerializer.serializeMessage(chatMessage);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, 
					InetAddress.getByName("255.255.255.255"), datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	public void sendJoinMessage() {
		try {
			JoinMessage joinMessage = new JoinMessage(ownUsername, ownUserId);
			byte[] sendData = messageSerializer.serializeMessage(joinMessage);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, 
					InetAddress.getByName("255.255.255.255"), datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	public void sendLeaveMessage() {
		try {
			LeaveMessage leaveMessage = new LeaveMessage(ownUsername, ownUserId);
			byte[] sendData = messageSerializer.serializeMessage(leaveMessage);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, 
					InetAddress.getByName("255.255.255.255"), datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	public void sendUserInfoMessage() {
		try {
			UserInfoMessage userInfoMessage = new UserInfoMessage(ownUsername, ownUserId);
			System.out.println("Sending UserInfoMessage: userId=" + ownUserId + ", username=" + ownUsername);
			byte[] sendData = messageSerializer.serializeMessage(userInfoMessage);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, 
					InetAddress.getByName("255.255.255.255"), datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setChatMessageListener(ChatMessageListener listener) {
		this.chatMessageListener = listener;		
	}

	public void setJoinMessageListener(JoinMessageListener listener) {
		this.joinMessageListener = listener;		
	}

	public void setLeaveMessageListener(LeaveMessageListerner listener) {
		this.leaveMessageListerner = listener;
	}

	public void setActiveUserListener(ActiveUserListener listener) {
		this.activeUserListener = listener;
	}
	
}
