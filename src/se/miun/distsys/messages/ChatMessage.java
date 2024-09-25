package se.miun.distsys.messages;

public class ChatMessage extends Message {

	public String chat = "";	
	public String username = "";
    public String userId;
	
	public ChatMessage(String chat, String username, String userId) {
		this.chat = chat;
		this.username = username;
		this.userId = userId;
	}
}
