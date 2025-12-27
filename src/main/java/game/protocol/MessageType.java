package game.protocol;

public enum MessageType {
    PLAYER_POSITION,  // POSITION|playerId|x|y|direction
    GAME_START,       // START|playerId|mazeSeed|startX|startY|exitX|exitY
    GAME_END,         // END|winnerId|time
    PLAYER_MOVE,      // MOVE|playerId|direction
    SYNC_REQUEST      // SYNC|playerId
}