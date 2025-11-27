package wagemaker.uk.network;

/**
 * Message sent by client to update player information (name and character sprite).
 * This is typically sent once after connecting to the server.
 */
public class PlayerInfoMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerName;
    private String characterSprite;
    
    /**
     * Default constructor for serialization.
     */
    public PlayerInfoMessage() {
        super();
    }
    
    /**
     * Creates a new player info message.
     * @param senderId The client ID
     * @param playerName The player's name
     * @param characterSprite The character sprite filename
     */
    public PlayerInfoMessage(String senderId, String playerName, String characterSprite) {
        super(senderId);
        this.playerName = playerName;
        this.characterSprite = characterSprite;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.PLAYER_INFO;
    }
    
    /**
     * Gets the player name.
     * @return The player name
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Gets the character sprite filename.
     * @return The character sprite filename
     */
    public String getCharacterSprite() {
        return characterSprite;
    }
}
