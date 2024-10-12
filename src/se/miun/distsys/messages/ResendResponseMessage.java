package se.miun.distsys.messages;

public class ResendResponseMessage extends Message {

    public ChatMessage chatMessage;

    public ResendResponseMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
