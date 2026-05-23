package dopo.domain.players;

/**
 * Blinky - Red square. Standard speed and size. No special abilities.
 */
public class RedPlayer extends Player {

    private static final double SPEED = 3.0;
    private static final int SIZE = 20;

    public RedPlayer(int x, int y) {
        super(x, y, SPEED, PlayerType.RED);
    }

    @Override
    public boolean onHit() {
        // Red player always dies on hit
        return true;
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
