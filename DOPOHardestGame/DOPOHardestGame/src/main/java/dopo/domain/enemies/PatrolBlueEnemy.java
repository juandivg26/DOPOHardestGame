package dopo.domain.enemies;

/**
 * Patrol blue dot. Follows a list of waypoints in a loop.
 */
public class PatrolBlueEnemy extends Enemy {

    private int[][] waypoints;
    private int currentWP;
    private double subX, subY; // sub-pixel position for smooth movement

    public PatrolBlueEnemy(int startX, int startY, int[][] waypoints, double speed) {
        super(startX, startY, speed, EnemyType.PATROL_BLUE);
        this.waypoints = waypoints;
        this.currentWP = 0;
        this.subX = startX;
        this.subY = startY;
    }

    @Override
    public void update() {
        if (waypoints == null || waypoints.length == 0) return;

        int[] target = waypoints[currentWP];
        double tx = target[0];
        double ty = target[1];

        double dist = Math.sqrt(Math.pow(tx - subX, 2) + Math.pow(ty - subY, 2));
        if (dist <= speed) {
            subX = tx;
            subY = ty;
            currentWP = (currentWP + 1) % waypoints.length;
        } else {
            subX += speed * (tx - subX) / dist;
            subY += speed * (ty - subY) / dist;
        }

        x = (int) subX;
        y = (int) subY;
    }
}
