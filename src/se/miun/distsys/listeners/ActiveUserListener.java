package se.miun.distsys.listeners;

import java.util.Map;

public interface ActiveUserListener {
    public void onActiveUserListChanged(Map<String, String> activeUsers);
}
