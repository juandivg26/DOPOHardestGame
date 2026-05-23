package dopo.domain.board;

import dopo.domain.elements.*;
import dopo.domain.enemies.Enemy;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the full configuration for one game level.
 * Loaded from a .txt config file.
 */
public class LevelConfig {

    private String name;
    private int boardWidth, boardHeight;
    private int timeLimitSeconds;

    private SafeZone startZone;
    private SafeZone intermediateZone; // may be null
    private SafeZone finalZone;

    // Player 1 start position (from startZone center by default)
    private int p1StartX, p1StartY;
    // Player 2 start position (from finalZone center - PvP mode)
    private int p2StartX, p2StartY;

    private List<Coin> coins = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Wall> walls = new ArrayList<>();
    private List<LifeSource> lifeSources = new ArrayList<>();
    private List<Bomb> bombs = new ArrayList<>();

    public LevelConfig(String name, int boardWidth, int boardHeight, int timeLimitSeconds) {
        this.name = name;
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    // -- Getters & Setters --
    public String getName() { return name; }
    public int getBoardWidth() { return boardWidth; }
    public int getBoardHeight() { return boardHeight; }
    public int getTimeLimitSeconds() { return timeLimitSeconds; }

    public SafeZone getStartZone() { return startZone; }
    public void setStartZone(SafeZone z) { this.startZone = z; }
    public SafeZone getIntermediateZone() { return intermediateZone; }
    public void setIntermediateZone(SafeZone z) { this.intermediateZone = z; }
    public SafeZone getFinalZone() { return finalZone; }
    public void setFinalZone(SafeZone z) { this.finalZone = z; }

    public int getP1StartX() { return p1StartX; }
    public int getP1StartY() { return p1StartY; }
    public void setP1Start(int x, int y) { p1StartX = x; p1StartY = y; }
    public int getP2StartX() { return p2StartX; }
    public int getP2StartY() { return p2StartY; }
    public void setP2Start(int x, int y) { p2StartX = x; p2StartY = y; }

    public List<Coin> getCoins() { return coins; }
    public void addCoin(Coin c) { coins.add(c); }
    public List<Enemy> getEnemies() { return enemies; }
    public void addEnemy(Enemy e) { enemies.add(e); }
    public List<Wall> getWalls() { return walls; }
    public void addWall(Wall w) { walls.add(w); }
    public List<LifeSource> getLifeSources() { return lifeSources; }
    public void addLifeSource(LifeSource ls) { lifeSources.add(ls); }
    public List<Bomb> getBombs() { return bombs; }
    public void addBomb(Bomb b) { bombs.add(b); }

    public int getTotalCoins() { return coins.size(); }
}
