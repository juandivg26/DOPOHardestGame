package dopo.domain.enemies;

import dopo.domain.BoardEntity;

/**
 * Abstract base for all enemy types.
 * Extensible for new enemy behaviors.
 */
public abstract class Enemy implements BoardEntity {

    public enum EnemyType { BASIC_BLUE, PATROL_BLUE, FAST_BLUE }

    protected int x, y;
    protected double speed;
    protected EnemyType type;
    protected boolean active;

    public Enemy(int x, int y, double speed, EnemyType type) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.type = type;
        this.active = true;
    }

    /**
     * Update the enemy's position by one game tick.
     */
    public abstract void update();

    /**
     * Check if this enemy overlaps with a given rectangle.
     */
    public boolean collidesWith(int ox, int oy, int oSize) {
        int mySize = getSize();
        return x < ox + oSize && x + mySize > ox &&
               y < oy + oSize && y + mySize > oy;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public EnemyType getType() { return type; }
    public double getSpeed() { return speed; }

    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public void setX(int x) { this.x = x; }
    @Override public void setY(int y) { this.y = y; }
    @Override public int getSize() { return 16; }
}
