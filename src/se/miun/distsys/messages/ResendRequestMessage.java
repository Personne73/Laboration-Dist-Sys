package se.miun.distsys.messages;

public class ResendRequestMessage extends Message {

    public String requesterUserId;
    public String requestedUserId;
    public int requestedMessageNumber;

    public ResendRequestMessage(String requesterUserId, String requestedUserId, int requestedMessageNumber) {
        this.requesterUserId = requesterUserId;
        this.requestedUserId = requestedUserId;
        this.requestedMessageNumber = requestedMessageNumber;
    }
    
}
