package dopo.presentation;

import dopo.domain.game.GameState;
import dopo.domain.players.Player;
import dopo.domain.ai.RandomAI;
import dopo.domain.ai.ExpertAI;
import dopo.domain.game.GameMode;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Game loop driven by a Swing Timer (~60 FPS).
 * Handles keyboard input and delegates to GameState for logic.
 */
public class GameLoop {

    private static final int TARGET_FPS = 60;
    private static final int TICK_MS    = 1000 / TARGET_FPS;

    private GameState state;
    private GamePanel panel;
    private Timer timer;
    private RandomAI randomAI;
    private ExpertAI expertAI;

    // Movement flags P1 (WASD + diagonals via combos)
    private boolean p1Up, p1Down, p1Left, p1Right;
    // Movement flags P2 (Arrow keys)
    private boolean p2Up, p2Down, p2Left, p2Right;

    private Runnable onWin, onLose, onEscape, onRestart;

    public GameLoop(GameState state, GamePanel panel) {
        this.state = state;
        this.panel = panel;
        panel.setGameState(state);
        setupKeyListener();
        setupTimer();
        if (state.getMode() == GameMode.PVM_RANDOM) {
            randomAI = new dopo.domain.ai.RandomAI();
        }
        if (state.getMode() == GameMode.PVM_EXPERT) {
            expertAI = new dopo.domain.ai.ExpertAI();
        }
    }

    private void setupTimer() {
        timer = new Timer(TICK_MS, e -> tick());
    }

    private void tick() {
        if (!state.isPaused() && isActiveStatus()) {
            movePlayer1();
            if (randomAI != null) randomAI.update(state);
            if (expertAI != null) expertAI.update(state);
            if (state.getPlayer2() != null) movePlayer2();
            state.update(TICK_MS);
            checkEndCondition();
        }
        panel.repaint();
    }

    private boolean isActiveStatus() {
        switch (state.getStatus()) {
            case PLAYING: return true;
            default: return false;
        }
    }

    private void movePlayer1() {
        Player p = state.getPlayer1();
        double sp = p.getCurrentSpeed();
        int dx = 0, dy = 0;
        if (p1Left)  dx -= (int) sp;
        if (p1Right) dx += (int) sp;
        if (p1Up)    dy -= (int) sp;
        if (p1Down)  dy += (int) sp;
        applyMove(p, dx, dy);
    }

    private void movePlayer2() {
        Player p = state.getPlayer2();
        double sp = p.getCurrentSpeed();
        int dx = 0, dy = 0;
        if (p2Left)  dx -= (int) sp;
        if (p2Right) dx += (int) sp;
        if (p2Up)    dy -= (int) sp;
        if (p2Down)  dy += (int) sp;
        applyMove(p, dx, dy);
    }

    private void applyMove(Player p, int dx, int dy) {
        int newX = p.getX() + dx;
        int newY = p.getY() + dy;

        // Board boundary
        int bw = state.getConfig().getBoardWidth();
        int bh = state.getConfig().getBoardHeight();
        int ps = p.getSize();
        newX = Math.max(0, Math.min(newX, bw - ps));
        newY = Math.max(0, Math.min(newY, bh - ps));

        // Wall collision
        boolean blockedX = false, blockedY = false;
        for (dopo.domain.elements.Wall w : state.getConfig().getWalls()) {
            if (w.blocks(newX, p.getY(), ps))  { blockedX = true; }
            if (w.blocks(p.getX(), newY, ps))  { blockedY = true; }
        }

        if (!blockedX) p.setX(newX);
        if (!blockedY) p.setY(newY);
    }

    private void checkEndCondition() {
        switch (state.getStatus()) {
	        case WON: case PVP_P1_WON: case PVP_P2_WON: case DRAW:
	            timer.stop();
	            // Solo esperar que el jugador presione ESC, no mostrar popup todavía
	            break;
	        case LOST:
	            timer.stop();
	            break;
            default: break;
        }
    }

    private void setupKeyListener() {
        panel.setFocusable(true);
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    // P1 - WASD
                    case KeyEvent.VK_W: p1Up    = true; break;
                    case KeyEvent.VK_S: p1Down  = true; break;
                    case KeyEvent.VK_A: p1Left  = true; break;
                    case KeyEvent.VK_D: p1Right = true; break;
                    // P2 - Arrows
                    case KeyEvent.VK_UP:    p2Up    = true; break;
                    case KeyEvent.VK_DOWN:  p2Down  = true; break;
                    case KeyEvent.VK_LEFT:  p2Left  = true; break;
                    case KeyEvent.VK_RIGHT: p2Right = true; break;
                    // Controls
                    case KeyEvent.VK_P:
                    case KeyEvent.VK_PAUSE:
                        if (state.isPaused()) state.resume(); else state.pause();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        GameState.Status st = state.getStatus();
                        switch (st) {
                            case WON: case PVP_P1_WON: case PVP_P2_WON: case DRAW:
                                if (onWin != null) SwingUtilities.invokeLater(onWin);
                                break;
                            case LOST:
                                if (onLose != null) SwingUtilities.invokeLater(onLose);
                                break;
                            default:
                                if (onEscape != null) onEscape.run();
                        }
                        break;
                    case KeyEvent.VK_R:
                        GameState.Status stR = state.getStatus();
                        if (stR == GameState.Status.WON || stR == GameState.Status.PVP_P1_WON
                                || stR == GameState.Status.PVP_P2_WON || stR == GameState.Status.DRAW
                                || stR == GameState.Status.LOST) {
                            if (onRestart != null) SwingUtilities.invokeLater(onRestart);
                        }
                        break;
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: p1Up    = false; break;
                    case KeyEvent.VK_S: p1Down  = false; break;
                    case KeyEvent.VK_A: p1Left  = false; break;
                    case KeyEvent.VK_D: p1Right = false; break;
                    case KeyEvent.VK_UP:    p2Up    = false; break;
                    case KeyEvent.VK_DOWN:  p2Down  = false; break;
                    case KeyEvent.VK_LEFT:  p2Left  = false; break;
                    case KeyEvent.VK_RIGHT: p2Right = false; break;
                }
            }
        });
    }
    public void removeKeyListeners(JPanel panel) {
        for (java.awt.event.KeyListener kl : panel.getKeyListeners()) {
            panel.removeKeyListener(kl);
        }
    }

    public void start() { timer.start(); panel.requestFocusInWindow(); }
    public void stop()  { timer.stop(); }

    public void setOnWin(Runnable r)    { onWin = r; }
    public void setOnLose(Runnable r)   { onLose = r; }
    public void setOnEscape(Runnable r) { onEscape = r; }
    public void setOnRestart(Runnable r) { onRestart = r; }
    public void setGameState(GameState s) { this.state = s; panel.setGameState(s); }
}
