package wagemaker.uk.network;

import java.util.List;
import java.util.Map;

/**
 * Network message for fence state synchronization.
 * Sent to synchronize all fence structures when a client joins or when bulk updates are needed.
 */
public class FenceSyncMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private Map<String, FenceState> fenceStates;
    
    /**
     * Default constructor for serialization.
     */
    public FenceSyncMessage() {
        super();
    }
    
    /**
     * Creates a new fence sync message.
     * @param senderId The ID of the sender
     * @param fenceStates Map of fence ID to fence state for all fences
     */
    public FenceSyncMessage(String senderId, Map<String, FenceState> fenceStates) {
        super(senderId);
        this.fenceStates = fenceStates;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.FENCE_SYNC;
    }
    
    /**
     * Gets the map of fence states.
     * @return Map of fence ID to fence state
     */
    public Map<String, FenceState> getFenceStates() {
        return fenceStates;
    }
    
    /**
     * Sets the map of fence states.
     * @param fenceStates Map of fence ID to fence state
     */
    public void setFenceStates(Map<String, FenceState> fenceStates) {
        this.fenceStates = fenceStates;
    }
    
    /**
     * Gets the number of fence pieces in this sync message.
     * @return The number of fence pieces
     */
    public int getFenceCount() {
        return fenceStates != null ? fenceStates.size() : 0;
    }
    
    @Override
    public String toString() {
        return "FenceSyncMessage{" +
                "fenceCount=" + getFenceCount() +
                ", senderId='" + getSenderId() + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}