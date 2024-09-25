package se.miun.distsys.messages;

public class LeaveMessage extends Message {

    // The username of the user that is leaving the chat
    public String username = "";
    public String chat = "";

    public String userId; // userId of the leaving person

    public LeaveMessage(String username, String userId) {
        this.username = username;
        this.userId = userId;
        this.chat = username + " (ID: " + userId + ") has leaved the chat !";
    }
}
