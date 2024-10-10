package se.miun.distsys.clock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import se.miun.distsys.messages.Message;

public class VectorClock implements Serializable {

    // vector clock = map of userIds to their respective clock values
    private  Map<String, Integer> vectorClock;

    public VectorClock(String userId) {
        this.vectorClock = new HashMap<>();
        this.vectorClock.put(userId, 0);
    }

    public VectorClock(Map<String, Integer> vectorClock) {
        this.vectorClock = new HashMap<>(vectorClock);
    }

    public Map<String, Integer> getMapVectorClock() {
        return new HashMap<>(vectorClock); // Return a copy of the vector clock
    }

    public VectorClock copy() {
        return new VectorClock(vectorClock);
    }

    // Increment the clock value for a specific the user
    public void increment(String userId) {
        this.vectorClock.put(userId, vectorClock.getOrDefault(userId, 0) + 1);
    }

    public void update(VectorClock other) {
        for (Map.Entry<String, Integer> entry : other.vectorClock.entrySet()) {
            String userId = entry.getKey();
            int otherValue = entry.getValue();
            int value = vectorClock.getOrDefault(userId, 0);
            vectorClock.put(userId, Math.max(value, otherValue));
        }
    }

    // Check if the message is causally ordered i.e. the message is received in the order it was sent
    public boolean isCausallyOrder(VectorClock messageClock, String senderId) {
        Map<String, Integer> messageVectorClock = messageClock.getMapVectorClock();

        // for(Map.Entry<String, Integer> entry : vectorClock.entrySet()) {
        //     String userId = entry.getKey();
        //     int value = entry.getValue(); // value of the receiver
        //     // clock of the sender message
        //     int messageValue = messageVectorClock.getOrDefault(userId, 0);

        //     if(userId.equals(senderId)) {
        //         // If the sender's clock is not one more than the receiver's clock
        //         // or the sender's clock is > receiver's clock
        //         // then the message is not causally ordered
        //         if(messageValue != value) {
        //             return false;
        //         }
        //     } else {
        //         if(messageValue > value) {
        //             return false;
        //         }
        //     }
        // }
        for(Map.Entry<String, Integer> entry : messageVectorClock.entrySet()) {
            String userId = entry.getKey();
            int messageValue = entry.getValue(); // value of the sender
            // clock of the receiver
            int value = vectorClock.getOrDefault(userId, 0);

            if(userId.equals(senderId)) {
                // If the sender's clock is not one more than the receiver's clock
                // or the sender's clock is > receiver's clock
                // then the message is not causally ordered
                if(messageValue != value + 1) {
                    return false;
                }
            } else {
                if(messageValue > value) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return vectorClock.toString();
    }
}
