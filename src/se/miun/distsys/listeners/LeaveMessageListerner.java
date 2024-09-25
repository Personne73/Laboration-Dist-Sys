package se.miun.distsys.listeners;

import se.miun.distsys.messages.LeaveMessage;

public interface LeaveMessageListerner {
    public void onIncomingLeaveMessage(LeaveMessage leaveMessage);
}
