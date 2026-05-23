package dopo.domain.players;

import dopo.exceptions.GameException;

/**
 * Factory for creating player instances by type.
 * Extensible: add new player types here.
 */
public class PlayerFactory {

    public static Player create(Player.PlayerType type, int x, int y) throws GameException {
        switch (type) {
            case RED:   return new RedPlayer(x, y);
            case BLUE:  return new BluePlayer(x, y);
            case GREEN: return new GreenPlayer(x, y);
            default:    throw new GameException("Unknown player type: " + type);
        }
    }
}
