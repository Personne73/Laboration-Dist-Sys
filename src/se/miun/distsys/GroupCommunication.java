package se.miun.distsys;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import se.miun.distsys.clock.VectorClock;
import se.miun.distsys.listeners.ActiveUserListener;
import se.miun.distsys.listeners.ChatMessageListener;
import se.miun.distsys.listeners.JoinMessageListener;
import se.miun.distsys.listeners.LeaveMessageListerner;
import se.miun.distsys.listeners.VectorClockListener;
import se.miun.distsys.messages.ChatMessage;
import se.miun.distsys.messages.JoinMessage;
import se.miun.distsys.messages.LeaveMessage;
import se.miun.distsys.messages.UserInfoMessage;
import se.miun.distsys.messages.Message;
import se.miun.distsys.messages.MessageSerializer;
import se.miun.distsys.messages.ResendRequestMessage;
import se.miun.distsys.messages.ResendResponseMessage;

public class GroupCommunication {
	
	private final int datagramSocketPort = 2506; //You need to change this!
	DatagramSocket datagramSocket = null;
	boolean runGroupCommunication = true;
	MessageSerializer messageSerializer = new MessageSerializer();

	//Listeners
	ChatMessageListener chatMessageListener = null;	
	JoinMessageListener joinMessageListener = null;
	LeaveMessageListerner leaveMessageListerner = null;
	ActiveUserListener activeUserListener = null;
	VectorClockListener vectorClockListener = null;

	// activeUsers list to keep track of users in the chat
	// key: userId, value: username
	private Map<String, String> activeUsers = new HashMap<>();
	private String ownUserId;
	private String ownUsername;
	private VectorClock ownVectorClock;

	// Keep track of the messages received and sent
	// key: message id, value: chat message
	private Map<String, ChatMessage> chatMessagesHistory = new HashMap<>();
	// list of messages that are not causally ordered
	private List<ChatMessage> pendingMessages = new ArrayList<>();
	
	public GroupCommunication(String ownUsername) {
		try {
			datagramSocket = new MulticastSocket(datagramSocketPort);
						
			ReceiveThread rt = new ReceiveThread();
			rt.start();

			// store own username and userId
			this.ownUsername = ownUsername;
			this.ownUserId = java.util.UUID.randomUUID().toString(); // generate random UUID
			// create own vector clock
			this.ownVectorClock = new VectorClock(ownUserId);
			
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
		int chanceToDropPackets = 50;

		@Override
		public void run() {
			byte[] buffer = new byte[65536];		
			DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
			
			while(runGroupCommunication) {
				try {
					datagramSocket.receive(datagramPacket);										
					byte[] packetData = datagramPacket.getData();					
					Message receivedMessage = messageSerializer.deserializeMessage(packetData);

					if(receivedMessage instanceof ChatMessage){
						// if the sender is not the own user, drop the packet with a certain probability
						ChatMessage chatMessage = (ChatMessage) receivedMessage;
						if(!chatMessage.userId.equals(ownUserId) && r.nextInt(100) < chanceToDropPackets){
							System.out.println("Dropped packet during chat message");
						} else {
							handleMessage(receivedMessage);
						}
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

				// check if the message is already in the chat history
				if(chatMessagesHistory.containsKey(getChatMessageId(chatMessage))){
					System.out.println("Message already in chat history, ignoring message");
					return;
				}

				chatMessagesHistory.put(getChatMessageId(chatMessage), chatMessage);

				if(chatMessage.userId.equals(ownUserId)){
					// update own vector clock if the message is causally ordered
					ownVectorClock.update(chatMessage.getVectorClock());

					// notify listeners that vector clock has changed
					if(vectorClockListener != null){
						vectorClockListener.onVectorClockChanged(ownVectorClock);
					}

					if(chatMessageListener != null){
						chatMessageListener.onIncomingChatMessage(chatMessage);
					}
				} else {

					if(ownVectorClock.isCausallyOrder(chatMessage.getVectorClock(), chatMessage.userId)){
						// update own vector clock if the message is causally ordered
						ownVectorClock.update(chatMessage.getVectorClock());
	
						// notify listeners that something has changed
						if(vectorClockListener != null){
							vectorClockListener.onVectorClockChanged(ownVectorClock);
						}
	
						if(chatMessageListener != null){
							chatMessageListener.onIncomingChatMessage(chatMessage);
						}

						// check if there are pending messages that can be delivered
						checkPendingMessages();
					} else {
						System.out.println("Message not causally ordered, cannot deliver message");
						pendingMessages.add(chatMessage);

						requestMissingMessages(chatMessage);
					}
				}

			} else if (message instanceof ResendResponseMessage) { 
				ResendResponseMessage resendResponseMessage = (ResendResponseMessage) message;
				ChatMessage chatMessage = resendResponseMessage.chatMessage;

				if(!chatMessagesHistory.containsKey(getChatMessageId(chatMessage))) {
					chatMessagesHistory.put(getChatMessageId(chatMessage), chatMessage);

					if(ownVectorClock.isCausallyOrder(chatMessage.getVectorClock(), chatMessage.userId)){
						// update own vector clock if the message is causally ordered
						ownVectorClock.update(chatMessage.getVectorClock());
	
						// notify listeners that something has changed
						if(vectorClockListener != null){
							vectorClockListener.onVectorClockChanged(ownVectorClock);
						}
	
						if(chatMessageListener != null){
							chatMessageListener.onIncomingChatMessage(chatMessage);
						}

						// check if there are pending messages that can be delivered
						checkPendingMessages();
					} else {
						System.out.println("Message not causally ordered, cannot deliver message yet (ResendResponseMessage)");
						pendingMessages.add(chatMessage);
					}
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
			} else if (message instanceof ResendRequestMessage) {
				ResendRequestMessage resendRequestMessage = (ResendRequestMessage) message;
				System.out.println("Received ResendRequestMessage: userId =" + resendRequestMessage.requestedUserId + ", messageNumber =" + resendRequestMessage.requestedMessageNumber);

				String requestMessageId = resendRequestMessage.requestedUserId + "/" + resendRequestMessage.requestedMessageNumber;

				// check if the requested message is in the chat history
				if(chatMessagesHistory.containsKey(requestMessageId)){
					ChatMessage missingMessage = chatMessagesHistory.get(requestMessageId);
					System.out.println("Requested message found in chat history, sending message to requester : " + missingMessage.chat);
					// send the requested message to the requester
					sendResendResponseMessage(missingMessage);
				}
				
			} else {				
				System.out.println("Unknown message type");
			}
		}
	}	

	// Request missing messages before delivering the message
	private void requestMissingMessages(ChatMessage chatMessage) {
		VectorClock messageClock = chatMessage.getVectorClock();
		String senderId = chatMessage.userId;
		Map<String, Integer> messageVectorClock = messageClock.getMapVectorClock();

		for(String userId : messageVectorClock.keySet()){
			int messageValue = messageVectorClock.get(userId);
			int ownValue = ownVectorClock.getMapVectorClock().getOrDefault(userId, 0);

			if(userId.equals(senderId)){
				// miss messages from the sender
				if(messageValue > ownValue){
					System.out.println("Requesting missing messages from the sender user : " + userId);
					for(int i = ownValue + 1; i < messageValue; i++){
						sendResendMessage(userId, i);
					}
				}
			} else {
				// miss message from other users
				if(messageValue > ownValue){
					System.out.println("Requesting missing messages from other user : " + userId);
					for(int i = ownValue + 1; i <= messageValue; i++){
						sendResendMessage(userId, i);
					}
				}
			}
		}

	}

	public void sendResendMessage(String userId, int messageNumber) {
		try {
			ResendRequestMessage resendRequestMessage = new ResendRequestMessage(ownUserId, userId, messageNumber);
			byte[] sendData = messageSerializer.serializeMessage(resendRequestMessage);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, 
					InetAddress.getByName("255.255.255.255"), datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	public void sendResendResponseMessage(ChatMessage message) {
		try {
			System.out.println("Sending ResendResponseMessage: userId=" + message.userId + ", messageNumber=" + getChatMessageId(message));
			ResendResponseMessage resendResponse = new ResendResponseMessage(message);
			byte[] sendData = messageSerializer.serializeMessage(resendResponse);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, 
					InetAddress.getByName("255.255.255.255"), datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	private void checkPendingMessages() {
		System.out.println("Checking pending messages");
		List<ChatMessage> messagesToRemove = new ArrayList<>();

		for(ChatMessage pendingMessage : pendingMessages){
			if(ownVectorClock.isCausallyOrder(pendingMessage.getVectorClock(), pendingMessage.userId)){

				ownVectorClock.update(pendingMessage.getVectorClock());

				if(vectorClockListener != null){
					vectorClockListener.onVectorClockChanged(ownVectorClock);
				}

				if(chatMessageListener != null){
					chatMessageListener.onIncomingChatMessage(pendingMessage);
				}

				messagesToRemove.add(pendingMessage);
			}
		}

		pendingMessages.removeAll(messagesToRemove);
	}
	
	private String getChatMessageId(ChatMessage chatMessage) {
		return chatMessage.userId + "/" + chatMessage.getVectorClock().getMapVectorClock().get(chatMessage.userId);
	}

	// Method to send chat message :
	// increment own vector clock
	// send chat message + username + userId + vector clock of the sender
	public void sendChatMessage(String chat) {
		try {
			ownVectorClock.increment(ownUserId);
			// create a copy of the vector clock because the vector clock is mutable
			VectorClock vectorClockCopy = ownVectorClock.copy();

			ChatMessage chatMessage = new ChatMessage(chat, ownUsername, ownUserId, vectorClockCopy);

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

	// Setters for listeners

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

	public void setVectorClockListener(VectorClockListener listener) {
		this.vectorClockListener = listener;
	}
	
}
