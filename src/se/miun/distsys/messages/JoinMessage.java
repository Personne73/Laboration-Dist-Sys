package se.miun.distsys.messages;

public class JoinMessage extends Message {
    
    // The username of the user that is joining the chat
    public String username = "";
    public String chat = "";

    public String userId;

    public JoinMessage(String username, String userId) {
        this.username = username;
        this.userId = userId;
        this.chat = username + " (ID: " + userId + ") has joined the chat !";
    }

}
