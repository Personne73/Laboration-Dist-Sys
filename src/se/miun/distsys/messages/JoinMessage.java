package se.miun.distsys.messages;

import java.util.ArrayList;

public class JoinMessage extends Message {
    
    // The username of the user that is joining the chat
    public String username = "";
    // The list of users all active users
    public ArrayList<String> users = new ArrayList<String>();

    public JoinMessage(String username) {
        this.username = username;
    }

}
