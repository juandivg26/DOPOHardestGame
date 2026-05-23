package dopo.domain.players;

import dopo.domain.BoardEntity;
import java.awt.Color;

/**
 * Abstract base class for all player types.
 * Extensible for new player skins/types.
 */
public abstract class Player implements BoardEntity {

    public enum Direction { NORTH, SOUTH, EAST, WEST, NE, NW, SE, SW, NONE }
    public enum PlayerType { RED, BLUE, GREEN }

    protected int x, y;
    protected int startX, startY;
    protected int spawnX, spawnY;      // punto de reaparición actual (puede ser zona intermedia)
    protected double baseSpeed;
    protected double currentSpeed;
    protected int deaths;
    protected int coinsCollected;
    protected boolean alive;
    protected PlayerType type;
    protected Color borderColor;
    protected boolean shieldActive;   // Jugador VERDE recibe golpe
    protected int tempSize = 0;
    
    private int invulnerableTicks = 0;

    public Player(int x, int y, double baseSpeed, PlayerType type) {
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
        this.spawnX = x;
        this.spawnY = y;
        this.baseSpeed = baseSpeed;
        this.currentSpeed = baseSpeed;
        this.deaths = 0;
        this.coinsCollected = 0;
        this.alive = true;
        this.type = type;
        this.borderColor = Color.WHITE;
        this.shieldActive = false;
    }

    /**
     * llamado cuando el jugador es golpeado por un enemigo.
     * Las subclases pueden sobrescribirlo para implementar un comportamiento especial (por ejemplo, escudo VERDE).
     * @return true si el jugador realmente muere, false si el golpe fue absorbido
     */
    public abstract boolean onHit();

    /**
     * Reset player to its current spawn point (start or intermediate zone).
     */
    public void respawn() {
        this.x = spawnX;
        this.y = spawnY;
        this.currentSpeed = baseSpeed;
        this.alive = true;
        this.shieldActive = initShield();
    }

    /**
     * Whether this player type starts with a shield active.
     */
    protected abstract boolean initShield();

    public void die() {
        deaths++;
        respawn();
    }

    public void collectCoin() {
        coinsCollected++;
    }

    public void setSpawnPoint(int x, int y) {
        this.spawnX = x;
        this.spawnY = y;
    }

    public void resetSpawnToStart() {
        this.spawnX = startX;
        this.spawnY = startY;
    }

    // -- BoardEntity --
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public void setX(int x) { this.x = x; }
    @Override public void setY(int y) { this.y = y; }

    //Getters y Setters
    public double getCurrentSpeed() { return currentSpeed; }
    public void setCurrentSpeed(double s) { currentSpeed = s; }
    public double getBaseSpeed() { return baseSpeed; }
    public int getDeaths() { return deaths; }
    public int getCoinsCollected() { return coinsCollected; }
    public boolean isAlive() { return alive; }
    public PlayerType getType() { return type; }
    public Color getBorderColor() { return borderColor; }
    public void setBorderColor(Color c) { borderColor = c; }
    public boolean isShieldActive() { return shieldActive; }
    public int getSpawnX() { return spawnX; }
    public int getSpawnY() { return spawnY; }
    public void setTempSize(int size) { tempSize = size; }
    public void setShieldActive(boolean shield) { this.shieldActive = shield; }

    @Override
    public String toString() {
        return type.name() + " Player [x=" + x + ", y=" + y + ", deaths=" + deaths + "]";
    }
    public boolean isInvulnerable() { return invulnerableTicks > 0; }
    public void setInvulnerable(int ticks) { invulnerableTicks = ticks; }
    public void tickInvulnerability() { if (invulnerableTicks > 0) invulnerableTicks--; }
}
