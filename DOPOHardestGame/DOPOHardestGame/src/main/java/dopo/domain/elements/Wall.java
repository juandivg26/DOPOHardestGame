package dopo.domain.elements;

import dopo.domain.BoardEntity;

/**
 * Solid wall - impassable by all entities.
 */
public class Wall implements BoardEntity {
    private int x, y, width, height;

    public Wall(int x, int y, int width, int height) {
        this.x = x; this.y = y;
        this.width = width; this.height = height;
    }

    public boolean blocks(int ox, int oy, int oSize) {
        return ox < x + width && ox + oSize > x &&
               oy < y + height && oy + oSize > y;
    }

    public int getWidth()  { return width; }
    public int getHeight() { return height; }

    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public void setX(int x) { this.x = x; }
    @Override public void setY(int y) { this.y = y; }
    @Override public int getSize() { return Math.max(width, height); }
}
