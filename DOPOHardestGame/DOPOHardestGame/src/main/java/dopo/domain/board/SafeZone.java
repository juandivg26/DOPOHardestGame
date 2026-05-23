package dopo.domain.board;

/**
 * Represents a safe zone (start, intermediate, or final).
 */
public class SafeZone {

    public enum ZoneType { START, INTERMEDIATE, FINAL }

    private int x, y, width, height;
    private ZoneType zoneType;

    public SafeZone(int x, int y, int width, int height, ZoneType zoneType) {
        this.x = x; this.y = y;
        this.width = width; this.height = height;
        this.zoneType = zoneType;
    }

    /**
     * Returns true if the given entity (by position and size) is inside this zone.
     */
    public boolean contains(int ex, int ey, int eSize) {
        return ex >= x && ey >= y &&
               ex + eSize <= x + width &&
               ey + eSize <= y + height;
    }

    public int getCenterX() { return x + width / 2; }
    public int getCenterY() { return y + height / 2; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public ZoneType getZoneType() { return zoneType; }

    @Override
    public String toString() {
        return zoneType + " zone at (" + x + "," + y + ") " + width + "x" + height;
    }
}
