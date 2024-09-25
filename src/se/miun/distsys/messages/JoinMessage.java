package se.miun.distsys.messages;

// import java.util.UUID;

public class JoinMessage extends Message {
    
    // The username of the user that is joining the chat
    public String username = "";
    public String chat = "";

    public String userId;

    public JoinMessage(String username, String userId) {
        this.username = username;
        this.userId = userId;
        // this.userId = UUID.randomUUID().toString();
        this.chat = username + " (ID: " + userId + ") has joined the chat !";
    }

}
