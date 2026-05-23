package dopo.domain.elements;

import dopo.domain.BoardEntity;

/**
 * Bomb: destroys any element (player OR enemy) that passes through it.
 */
public class Bomb implements BoardEntity {
    private int x, y;
    private boolean exploded;

    public Bomb(int x, int y) { this.x = x; this.y = y; this.exploded = false; }

    public boolean isExploded() { return exploded; }
    public void explode() { exploded = true; }

    public boolean collidesWith(int ox, int oy, int oSize) {
        int s = getSize();
        return !exploded && ox < x + s && ox + oSize > x && oy < y + s && oy + oSize > y;
    }

    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public void setX(int x) { this.x = x; }
    @Override public void setY(int y) { this.y = y; }
    @Override public int getSize() { return 16; }
}
