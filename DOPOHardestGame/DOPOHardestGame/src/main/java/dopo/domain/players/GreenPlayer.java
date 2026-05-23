package dopo.domain.players;

/**
 * Clyde - Cuadro verde. Velocidad estándar, absorbe el primer impacto (reduce la velocidad a 0,7x).
 */

public class GreenPlayer extends Player {

    private static final double SPEED = 3.0;
    private static final int SIZE = 20;
    private static final double DAMAGED_SPEED_FACTOR = 0.7;

    public GreenPlayer(int x, int y) {
        super(x, y, SPEED, PlayerType.GREEN);
        this.shieldActive = true;
    }

    @Override
    public boolean onHit() {
        if (shieldActive) {
            // Absorber golpe: perder escudo, reducir velocidad
            shieldActive = false;
            currentSpeed = baseSpeed * DAMAGED_SPEED_FACTOR;
            return false; // NO muere
        }
        return true; // Muere en el segundo golpe
    }

    @Override
    protected boolean initShield() {
        return true; // Verde siempre comienza con escudo
    }

    @Override
    public void respawn() {
        this.x = spawnX;
        this.y = spawnY;
        this.alive = true;
        // NO restaurar velocidad ni escudo al reaparecer
    }
    @Override
    public int getSize() {
        return tempSize > 0 ? tempSize : SIZE;
    }
    public void restoreAbility() {
        this.shieldActive = true;
        this.currentSpeed = baseSpeed;
    }
}
