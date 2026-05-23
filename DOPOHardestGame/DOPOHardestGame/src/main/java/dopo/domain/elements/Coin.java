package dopo.domain.elements;

import dopo.domain.BoardEntity;

/**
 * Abstract coin. Extensible for new coin types.
 */
public abstract class Coin implements BoardEntity {

    public enum CoinType { YELLOW, SKIN_RED, SKIN_BLUE, SKIN_GREEN }

    protected int x, y;
    protected boolean collected;
    protected CoinType type;

    public Coin(int x, int y, CoinType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.collected = false;
    }

    public void collect() { collected = true; }
    public boolean isCollected() { return collected; }
    public CoinType getType() { return type; }

    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public void setX(int x) { this.x = x; }
    @Override public void setY(int y) { this.y = y; }
    @Override public int getSize() { return 14; }
}
