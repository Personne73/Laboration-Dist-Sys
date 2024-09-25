package se.miun.distsys.messages;

public class UserInfoMessage extends Message {

    public String username = "";
    public String userId;

    public UserInfoMessage(String username, String userId) {
        this.username = username;
        this.userId = userId;
    }
}
