package se.miun.distsys.listeners;

import se.miun.distsys.messages.JoinMessage;

public interface JoinMessageListener {
    public void onIncomingJoinMessage(JoinMessage joinMessage);
}
