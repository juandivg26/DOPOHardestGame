package dopo.domain.enemies;

/**
 * Fast (Tipo A) blue dot. 2x speed, linear movement, bounces off walls.
 */
public class FastBlueEnemy extends Enemy {

    private boolean horizontal;
    private double dx, dy;
    private int minBound, maxBound;

    public FastBlueEnemy(int x, int y, boolean horizontal, int minBound, int maxBound, double baseSpeed) {
        super(x, y, baseSpeed * 2.0, EnemyType.FAST_BLUE);
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
