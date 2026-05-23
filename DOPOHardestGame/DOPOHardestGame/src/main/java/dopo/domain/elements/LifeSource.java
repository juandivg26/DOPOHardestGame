package dopo.domain.elements;

import dopo.domain.BoardEntity;

/**
 * Life source: gives +1 life (extra hit absorption) to the first player who reaches it.
 * Disappears after use.
 */
public class LifeSource implements BoardEntity {
    private int x, y;
    private boolean used;

    public LifeSource(int x, int y) { this.x = x; this.y = y; this.used = false; }

    public boolean isUsed() { return used; }
    public void use() { used = true; }

    public boolean collidesWith(int ox, int oy, int oSize) {
        int s = getSize();
        return !used && ox < x + s && ox + oSize > x && oy < y + s && oy + oSize > y;
    }

    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public void setX(int x) { this.x = x; }
    @Override public void setY(int y) { this.y = y; }
    @Override public int getSize() { return 16; }
}
