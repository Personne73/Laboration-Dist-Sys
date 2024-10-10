package se.miun.distsys.messages;

import se.miun.distsys.clock.VectorClock;

public class ChatMessage extends Message {

	public String chat = "";	
	public String username = "";
    public String userId;
	private VectorClock vectorClock;
	
	public ChatMessage(String chat, String username, String userId, VectorClock vectorClock) {
		this.chat = chat;
		this.username = username;
		this.userId = userId;
		this.vectorClock = vectorClock;
	}

	public VectorClock getVectorClock() {
		return vectorClock.copy();
	}
}
