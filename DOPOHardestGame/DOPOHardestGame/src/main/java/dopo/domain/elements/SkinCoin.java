package dopo.domain.elements;

import dopo.domain.players.Player;

/**
 * Skin coin. When collected, temporarily gives the player the associated skin.
 */
public class SkinCoin extends Coin {

    private Player.PlayerType associatedSkin;

    public SkinCoin(int x, int y, Player.PlayerType skin) {
        super(x, y, skinToCoinType(skin));
        this.associatedSkin = skin;
    }

    public Player.PlayerType getAssociatedSkin() { return associatedSkin; }

    private static CoinType skinToCoinType(Player.PlayerType skin) {
        switch (skin) {
            case RED:   return CoinType.SKIN_RED;
            case BLUE:  return CoinType.SKIN_BLUE;
            case GREEN: return CoinType.SKIN_GREEN;
            default:    return CoinType.SKIN_RED;
        }
    }
}
