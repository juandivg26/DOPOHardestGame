package dopo.domain.game;

import dopo.domain.board.LevelConfig;
import dopo.domain.board.SafeZone;
import dopo.domain.elements.*;
import dopo.domain.enemies.Enemy;
import dopo.domain.players.Player;
import dopo.domain.players.GreenPlayer;
import dopo.exceptions.GameException;

import java.util.List;

/**
 * Core game state machine. Handles all game logic per tick.
 * Decoupled from rendering.
 */
public class GameState {

    public enum Status { PLAYING, PAUSED, WON, LOST, PVP_P1_WON, PVP_P2_WON, DRAW }

    private LevelConfig config;
    private GameMode mode;
    private Player player1;
    private Player player2; // null en un solo jugador

    private int timeRemainingMs;
    private Status status;

    // Temporary skin override for each player
    private Player.PlayerType p1TempSkin;
    private Player.PlayerType p2TempSkin;

    // Extra shield granted by LifeSource
    private boolean p1ExtraShield;
    private boolean p2ExtraShield;

    public GameState(LevelConfig config, GameMode mode, Player p1, Player p2) throws GameException {
        if (config == null) throw new GameException("LevelConfig cannot be null");
        if (p1 == null)     throw new GameException("Player 1 cannot be null");

        this.config = config;
        this.mode = mode;
        this.player1 = p1;
        this.player2 = p2;
        this.timeRemainingMs = config.getTimeLimitSeconds() * 1000;
        this.status = Status.PLAYING;
        this.p1TempSkin = null;
        this.p2TempSkin = null;
        this.p1ExtraShield = false;
        this.p2ExtraShield = false;
    }

    /**
     * Avanzar el juego un tick (llamado aproximadamente 60 veces por segundo desde el ciclo del juego).
     * @param deltaMs milisegundos transcurridos desde el último tick
     */
    public void update(int deltaMs) {
        if (status != Status.PLAYING) return;

        // Cuenta regresiva del temporizador
        timeRemainingMs -= deltaMs;
        if (timeRemainingMs <= 0) {
            timeRemainingMs = 0;
            status = Status.LOST;
            return;
        }
        // Actualizar invulnerabilidad
        player1.tickInvulnerability();
        if (player2 != null) player2.tickInvulnerability();
        // Mover enemigos
        for (Enemy e : config.getEnemies()) {
            if (e.isActive()) e.update();
        }

        // Check player-enemy collisions
        handleEnemyCollisions(player1, false);
        if (player2 != null) handleEnemyCollisions(player2, true);

        // PvP: check player-player collision
        if (player2 != null) handlePlayerPlayerCollision();

        // Check bomb collisions
        handleBombCollisions();

        // Check coin pickups
        handleCoinPickups(player1, false);
        if (player2 != null) handleCoinPickups(player2, true);

        // Check life source pickups
        handleLifeSourcePickups(player1, false);
        if (player2 != null) handleLifeSourcePickups(player2, true);

        // Check intermediate zone entry
        checkIntermediateZone(player1);
        if (player2 != null) checkIntermediateZone(player2);

        // Check win condition
        checkWinCondition();
    }

    // ---- Private helpers ----

    private void handleEnemyCollisions(Player player, boolean isP2) {
        for (Enemy e : config.getEnemies()) {
            if (!e.isActive()) continue;
            if (!player.isInvulnerable() && !isInSafeZone(player) && e.collidesWith(player.getX(), player.getY(), player.getSize())) {
            	boolean dies;
            	if (!isP2 && p1ExtraShield) {
            	    p1ExtraShield = false;
            	    dies = false;
            	} else if (isP2 && p2ExtraShield) {
            	    p2ExtraShield = false;
            	    dies = false;
            	} else if (player.isShieldActive() && 
            	           ((isP2 ? p2TempSkin : p1TempSkin) == Player.PlayerType.GREEN)) {
            	    // Escudo de SkinCoin verde: absorbe el golpe y quita la skin
            	    player.setShieldActive(false);
            	    if (isP2) p2TempSkin = null;
            	    else p1TempSkin = null;
            	    dies = false;
            	} else {
            	    dies = player.onHit();
            	}
                player.setInvulnerable(60); // 60 ticks = 1 segundo de invulnerabilidad
                if (dies) {
                    player.die();
                    if (isP2) p2TempSkin = null;
                    else      p1TempSkin = null;
                }
                break;
            }
        }
    }

    private void handlePlayerPlayerCollision() {
        boolean overlap =
            player1.getX() < player2.getX() + player2.getSize() &&
            player1.getX() + player1.getSize() > player2.getX() &&
            player1.getY() < player2.getY() + player2.getSize() &&
            player1.getY() + player1.getSize() > player2.getY();

        if (overlap && !isInSafeZone(player1) && !isInSafeZone(player2)) {
            player1.die();
            player2.die();
            p1TempSkin = null;
            p2TempSkin = null;
        }
    }

    private void handleBombCollisions() {
        for (Bomb b : config.getBombs()) {
            if (b.isExploded()) continue;
            if (b.collidesWith(player1.getX(), player1.getY(), player1.getSize())) {
                b.explode();
                player1.die();
                p1TempSkin = null;
            }
            if (player2 != null && b.collidesWith(player2.getX(), player2.getY(), player2.getSize())) {
                b.explode();
                player2.die();
                p2TempSkin = null;
            }
            // Bombs also destroy enemies
            for (Enemy e : config.getEnemies()) {
                if (e.isActive() && b.collidesWith(e.getX(), e.getY(), e.getSize())) {
                    b.explode();
                    e.setActive(false);
                }
            }
        }
    }

    private void handleCoinPickups(Player player, boolean isP2) {
        for (Coin c : config.getCoins()) {
            if (c.isCollected()) continue;
            int cs = c.getSize();
            int ps = player.getSize();
            if (player.getX() < c.getX() + cs && player.getX() + ps > c.getX() &&
                player.getY() < c.getY() + cs && player.getY() + ps > c.getY()) {
                c.collect();
                player.collectCoin();
                if (c instanceof SkinCoin) {
                    SkinCoin sc = (SkinCoin) c;
                    Player.PlayerType skin = sc.getAssociatedSkin();
                    if (isP2) p2TempSkin = skin;
                    else      p1TempSkin = skin;
                    switch (skin) {
                        case RED:
                            player.setCurrentSpeed(3.0);
                            player.setTempSize(0);
                            break;
                        case BLUE:
                            player.setCurrentSpeed(4.5);
                            player.setTempSize(30);
                            break;
                        case GREEN:
                            player.setCurrentSpeed(3.0);
                            player.setTempSize(0);
                            player.setShieldActive(true);
                            break;
                    }
                }
            }
        }
    }

    private void handleLifeSourcePickups(Player player, boolean isP2) {
        for (LifeSource ls : config.getLifeSources()) {
            if (ls.isUsed()) continue;
            if (ls.collidesWith(player.getX(), player.getY(), player.getSize())) {
                ls.use();
                if (isP2) p2ExtraShield = true;
                else      p1ExtraShield = true;
            }
        }
    }

    private void checkIntermediateZone(Player player) {
        SafeZone iz = config.getIntermediateZone();
        if (iz != null && iz.contains(player.getX(), player.getY(), player.getSize())) {
            player.setSpawnPoint(iz.getCenterX() - player.getSize() / 2,
                                 iz.getCenterY() - player.getSize() / 2);
        }
    }

    private void checkWinCondition() {
        SafeZone fz1 = config.getFinalZone();
        boolean allCoins = allCoinsCollected();

        if (mode == GameMode.PLAYER) {
            if (allCoins && fz1.contains(player1.getX(), player1.getY(), player1.getSize())) {
                status = Status.WON;
            }
        } else {
            SafeZone zonaP1 = config.getFinalZone();  // P1 gana en la zona final
            SafeZone zonaP2 = config.getStartZone();  // P2 gana en la zona de inicio del P1

            boolean p1EnZona = zonaP1.contains(player1.getX(), player1.getY(), player1.getSize());
            boolean p2EnZona = player2 != null && zonaP2.contains(player2.getX(), player2.getY(), player2.getSize());

            if (allCoins && (p1EnZona || p2EnZona)) {
                int c1 = player1.getCoinsCollected();
                int c2 = player2 != null ? player2.getCoinsCollected() : 0;

                if (p1EnZona && !p2EnZona) {
                    if (c1 > c2) status = Status.PVP_P1_WON;
                    else if (c2 > c1) status = Status.PVP_P2_WON;
                    else if (player1.getDeaths() <= player2.getDeaths()) status = Status.PVP_P1_WON;
                    else status = Status.PVP_P2_WON;
                } else if (p2EnZona && !p1EnZona) {
                    if (c2 > c1) status = Status.PVP_P2_WON;
                    else if (c1 > c2) status = Status.PVP_P1_WON;
                    else if (player1.getDeaths() <= player2.getDeaths()) status = Status.PVP_P1_WON;
                    else status = Status.PVP_P2_WON;
                } else {
                    // Llegaron al mismo tick: monedas → muertes → empate
                    if (c1 > c2) status = Status.PVP_P1_WON;
                    else if (c2 > c1) status = Status.PVP_P2_WON;
                    else if (player1.getDeaths() < player2.getDeaths()) status = Status.PVP_P1_WON;
                    else if (player2.getDeaths() < player1.getDeaths()) status = Status.PVP_P2_WON;
                    else status = Status.DRAW;
                }
            }
        }
    }
    private boolean isInSafeZone(Player player) {
        int px = player.getX(), py = player.getY(), ps = player.getSize();
        if (config.getStartZone() != null && config.getStartZone().contains(px, py, ps)) return true;
        if (config.getFinalZone() != null && config.getFinalZone().contains(px, py, ps)) return true;
        if (config.getIntermediateZone() != null && config.getIntermediateZone().contains(px, py, ps)) return true;
        return false;
    }

    private boolean allCoinsCollected() {
        for (Coin c : config.getCoins()) {
            if (!c.isCollected()) return false;
        }
        return true;
    }
    
    public void pause() {
        if (status == Status.PLAYING) status = Status.PAUSED;
    }

    public void resume() {
        if (status == Status.PAUSED) status = Status.PLAYING;
    }

    public boolean isPaused() { return status == Status.PAUSED; }
    public Status getStatus() { return status; }
    public int getTimeRemainingMs() { return timeRemainingMs; }
    public int getTimeRemainingSeconds() { return timeRemainingMs / 1000; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public LevelConfig getConfig() { return config; }
    public GameMode getMode() { return mode; }
    public Player.PlayerType getP1TempSkin() { return p1TempSkin; }
    public Player.PlayerType getP2TempSkin() { return p2TempSkin; }

    public int getCollectedCoins() {
        int count = 0;
        for (Coin c : config.getCoins()) if (c.isCollected()) count++;
        return count;
    }
    /**
     * Calcula el puntaje de un jugador al finalizar el nivel.
     * Fórmula: (monedas × 10) + (tiempo_restante × 2) - (muertes × 5)
     * El puntaje nunca es negativo.
     */
    public int calculateScore(Player p) {
        int score = (p.getCoinsCollected() * 10)
                  + (getTimeRemainingSeconds() * 2)
                  - (p.getDeaths() * 5);
        return Math.max(0, score);
    }
}
