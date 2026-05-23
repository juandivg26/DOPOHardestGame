package dopo.domain.enemies;

/**
 * Basic blue dot. Moves in a straight line (H or V), bounces off walls.
 */
public class BasicBlueEnemy extends Enemy {

    private boolean horizontal;
    private double dx, dy;
    private int minBound, maxBound;

    /**
     * @param x         start x
     * @param y         start y
     * @param horizontal true = moves left/right, false = up/down
     * @param minBound  lower wall coordinate
     * @param maxBound  upper wall coordinate
     * @param speed     movement speed in pixels per tick
     */
    public BasicBlueEnemy(int x, int y, boolean horizontal, int minBound, int maxBound, double speed) {
        super(x, y, speed, EnemyType.BASIC_BLUE);
        this.horizontal = horizontal;
        this.minBound = minBound;
        this.maxBound = maxBound;
        if (horizontal) { dx = speed; dy = 0; }
        else            { dx = 0;    dy = speed; }
    }

    @Override
    public void update() {
        x += (int) dx;
        y += (int) dy;

        if (horizontal) {
            if (x <= minBound) { x = minBound; dx = Math.abs(dx); }
            if (x + getSize() >= maxBound) { x = maxBound - getSize(); dx = -Math.abs(dx); }
        } else {
            if (y <= minBound) { y = minBound; dy = Math.abs(dy); }
            if (y + getSize() >= maxBound) { y = maxBound - getSize(); dy = -Math.abs(dy); }
        }
    }
}
