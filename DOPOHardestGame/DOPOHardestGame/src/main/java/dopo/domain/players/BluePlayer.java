package dopo.domain.players;

/**
 * Inky - Blue square. 1.5x speed, 1.5x size. No shield.
 */
public class BluePlayer extends Player {

    private static final double SPEED = 4.5;  // 1.5x of 3.0
    private static final int SIZE = 30;       // 1.5x of 20

    public BluePlayer(int x, int y) {
        super(x, y, SPEED, PlayerType.BLUE);
    }

    @Override
    public boolean onHit() {
        return true; // Dies on hit
    }

    @Override
    protected boolean initShield() {
        return false;
    }

    @Override
    public int getSize() {
        return tempSize > 0 ? tempSize : SIZE;
    }
}
