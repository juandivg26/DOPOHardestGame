package dopo.presentation;

import dopo.domain.board.LevelConfig;
import dopo.domain.board.SafeZone;
import dopo.domain.elements.*;
import dopo.domain.enemies.Enemy;
import dopo.domain.game.GameState;
import dopo.domain.players.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Representa el tablero del juego, las entidades y el HUD usando Swing.
 */
public class GamePanel extends JPanel {

    private static final Color COLOR_SAFE_ZONE   = new Color(100, 200, 100, 180);
    private static final Color COLOR_INTER_ZONE  = new Color(150, 220, 150, 160);
    private static final Color COLOR_ENEMY       = new Color(60, 120, 230);
    private static final Color COLOR_COIN_YELLOW = new Color(255, 210, 0);
    private static final Color COLOR_WALL        = new Color(50, 50, 80);
    private static final Color COLOR_BOMB        = new Color(30, 30, 30);
    private static final Color COLOR_LIFE        = new Color(255, 100, 150);
    private static final Color COLOR_BG          = new Color(20, 20, 35);
    private static final Color COLOR_HUD_BG      = new Color(10, 10, 20, 220);

    private GameState state;
    private String p1Name = "Jugador 1";
    private String p2Name = "Jugador 2";

    public GamePanel() {
        setBackground(COLOR_BG);
        setDoubleBuffered(true);
    }

    public void setGameState(GameState state) {
        this.state = state;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (state == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        LevelConfig cfg = state.getConfig();

        // Calcular el desplazamiento para centrar la placa en el panel
        int offsetX = (getWidth()  - cfg.getBoardWidth())  / 2;
        int offsetY = (getHeight() - cfg.getBoardHeight()) / 2 + 30; // +30 for HUD

        // Antecedentes de la junta
        g2.setColor(new Color(35, 35, 55));
        g2.fillRect(offsetX, offsetY, cfg.getBoardWidth(), cfg.getBoardHeight());
        g2.setColor(new Color(70, 70, 100));
        g2.drawRect(offsetX, offsetY, cfg.getBoardWidth(), cfg.getBoardHeight());

        // Zonas seguras
        drawZone(g2, cfg.getStartZone(), COLOR_SAFE_ZONE, offsetX, offsetY);
        drawZone(g2, cfg.getFinalZone(), COLOR_SAFE_ZONE, offsetX, offsetY);
        if (cfg.getIntermediateZone() != null)
            drawZone(g2, cfg.getIntermediateZone(), COLOR_INTER_ZONE, offsetX, offsetY);

        // Paredes
        g2.setColor(COLOR_WALL);
        for (dopo.domain.elements.Wall w : cfg.getWalls()) {
            g2.fillRect(w.getX() + offsetX, w.getY() + offsetY, w.getWidth(), w.getHeight());
        }

        // Bombas
        for (Bomb b : cfg.getBombs()) {
            if (!b.isExploded()) {
                int bx = b.getX() + offsetX, by = b.getY() + offsetY, bs = b.getSize();
                g2.setColor(COLOR_BOMB);
                g2.fillOval(bx, by, bs, bs);
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(bx, by, bs, bs);
                // fuse
                g2.setColor(new Color(200, 150, 0));
                g2.drawLine(bx + bs/2, by, bx + bs/2 + 4, by - 6);
            }
        }

        // Fuentes de vida
        for (LifeSource ls : cfg.getLifeSources()) {
            if (!ls.isUsed()) {
                int lx = ls.getX() + offsetX, ly = ls.getY() + offsetY, lz = ls.getSize();
                g2.setColor(COLOR_LIFE);
                g2.fillRoundRect(lx, ly, lz, lz, 6, 6);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.drawString("+", lx + 4, ly + lz - 3);
            }
        }

        // Monedas
        for (Coin c : cfg.getCoins()) {
            if (!c.isCollected()) {
                int cx = c.getX() + offsetX, cy = c.getY() + offsetY, cs = c.getSize();
                Color coinColor;
                switch (c.getType()) {
                    case YELLOW:     coinColor = COLOR_COIN_YELLOW; break;
                    case SKIN_RED:   coinColor = new Color(220, 60, 60);  break;
                    case SKIN_BLUE:  coinColor = new Color(60, 100, 220); break;
                    case SKIN_GREEN: coinColor = new Color(60, 190, 80);  break;
                    default:         coinColor = Color.YELLOW;
                }
                g2.setColor(coinColor);
                g2.fillOval(cx, cy, cs, cs);
                g2.setColor(coinColor.brighter());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(cx, cy, cs, cs);
            }
        }

        // Enemigos
        for (Enemy e : cfg.getEnemies()) {
            if (e.isActive()) {
                int ex = e.getX() + offsetX, ey = e.getY() + offsetY, es = e.getSize();
                Color ec;
                switch (e.getType()) {
                    case FAST_BLUE:   ec = new Color(30, 60, 255);  break;
                    case PATROL_BLUE: ec = new Color(80, 150, 255); break;
                    default:          ec = COLOR_ENEMY;
                }
                g2.setColor(ec);
                g2.fillOval(ex, ey, es, es);
                g2.setColor(ec.brighter());
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(ex, ey, es, es);
            }
        }

        // Jugadores
        drawPlayer(g2, state.getPlayer1(), offsetX, offsetY);
        if (state.getPlayer2() != null)
            drawPlayer(g2, state.getPlayer2(), offsetX, offsetY);

        // HUD
        drawHUD(g2);

        // Mensajes superpuestos
        drawOverlay(g2);
    }

    private void drawZone(Graphics2D g2, SafeZone z, Color color, int ox, int oy) {
        if (z == null) return;
        g2.setColor(color);
        g2.fillRect(z.getX() + ox, z.getY() + oy, z.getWidth(), z.getHeight());
        g2.setColor(color.darker());
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(z.getX() + ox, z.getY() + oy, z.getWidth(), z.getHeight());
    }

    private void drawPlayer(Graphics2D g2, Player p, int ox, int oy) {
        int px = p.getX() + ox, py = p.getY() + oy, ps = p.getSize();

        // Color del cuerpo del jugador
        Color body;
        switch (p.getType()) {
            case RED:   body = new Color(220, 50, 50);  break;
            case BLUE:  body = new Color(50, 100, 220); break;
            case GREEN:
                // Verde con escudo: color vibrante. Verde débil (sin escudo): color apagado
                body = p.isShieldActive()
                    ? new Color(50, 190, 80)
                    : new Color(30, 90, 40);
                break;
            default: body = Color.RED;
        }

        // Indicador visual del jugador verde con escudo activo: halo brillante
        if (p.getType() == Player.PlayerType.GREEN && p.isShieldActive()) {
            g2.setColor(new Color(80, 255, 120, 80));
            g2.fillRoundRect(px - 5, py - 5, ps + 10, ps + 10, 10, 10);
            g2.setColor(new Color(80, 255, 120, 180));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(px - 5, py - 5, ps + 10, ps + 10, 10, 10);
        }

        // Indicador visual del jugador verde en estado DÉBIL: borde rojo parpadeante
        if (p.getType() == Player.PlayerType.GREEN && !p.isShieldActive()) {
            g2.setColor(new Color(255, 60, 60, 160));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(px - 4, py - 4, ps + 8, ps + 8, 8, 8);
        }

        g2.setColor(body);
        g2.fillRect(px, py, ps, ps);

        // Color de borde elegido por el jugador
        g2.setColor(p.getBorderColor());
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(px, py, ps, ps);

        // Etiqueta "!" sobre el verde débil para que sea muy obvio
        if (p.getType() == Player.PlayerType.GREEN && !p.isShieldActive()) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(new Color(255, 80, 80));
            g2.drawString("!", px + ps / 2 - 3, py - 6);
        }

        // Ojos
        g2.setColor(Color.WHITE);
        int ew = 4, eh = 4;
        g2.fillOval(px + ps/2 - 5, py + 4, ew, eh);
        g2.fillOval(px + ps/2 + 1, py + 4, ew, eh);
    }

    private void drawHUD(Graphics2D g2) {
        if (state == null) return;
        int panelW = getWidth();

        g2.setColor(COLOR_HUD_BG);
        g2.fillRect(0, 0, panelW, 32);

        g2.setFont(new Font("Monospaced", Font.BOLD, 14));

        // Tiempo restante
        int secs = state.getTimeRemainingSeconds();
        Color timeColor = secs <= 10 ? new Color(255, 80, 80) : new Color(100, 220, 100);
        g2.setColor(timeColor);
        g2.drawString(String.format("⏱ %02d:%02d", secs / 60, secs % 60), 16, 22);

        // Pausa justo al lado del tiempo
        if (state.isPaused()) {
            g2.setColor(new Color(255, 200, 50));
            g2.drawString("  ⏸ Pausa", 90, 22);
        }

        // Monedas totales en el centro
        g2.setColor(COLOR_COIN_YELLOW);
        g2.drawString("$ " + state.getCollectedCoins() + "/" + state.getConfig().getTotalCoins(), panelW / 2 - 40, 22);

        // Jugador 1: muertes y monedas
        g2.setColor(new Color(255, 120, 120));
        g2.drawString(p1Name + " ☠" + state.getPlayer1().getDeaths()
                + " $" + state.getPlayer1().getCoinsCollected(), panelW - 300, 22);

        // Jugador 2: muertes y monedas (si existe)
        if (state.getPlayer2() != null) {
            g2.drawString(p2Name + " ☠" + state.getPlayer2().getDeaths()
                    + " $" + state.getPlayer2().getCoinsCollected(), panelW - 140, 22);
        }
    }

    private void drawOverlay(Graphics2D g2) {
        if (state == null) return;
        GameState.Status status = state.getStatus();
        String msg = null;
        Color msgColor = Color.WHITE;

        switch (status) {
            case WON:        msg = "¡NIVEL COMPLETADO!"; msgColor = new Color(80, 255, 120); break;
            case LOST:       msg = "¡TIEMPO AGOTADO!";   msgColor = new Color(255, 80, 80);  break;
            case PVP_P1_WON: msg = "¡" + p1Name + " GANA!"; msgColor = new Color(255, 150, 50); break;
            case PVP_P2_WON: msg = "¡" + p2Name + " GANA!"; msgColor = new Color(80, 150, 255); break;
            case DRAW:       msg = "¡EMPATE!";           msgColor = new Color(220, 180, 0);  break;
            default: return;
        }

        // Superposición semitransparente
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setFont(new Font("SansSerif", Font.BOLD, 36));
        FontMetrics fm = g2.getFontMetrics();
        int mx = (getWidth() - fm.stringWidth(msg)) / 2;
        int my = getHeight() / 2;

        g2.setColor(new Color(0, 0, 0, 100));
        g2.drawString(msg, mx + 2, my + 2);
        g2.setColor(msgColor);
        g2.drawString(msg, mx, my);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        String sub = "Presiona R para reintentar o ESC para mostrar menú";
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString(sub, (getWidth() - g2.getFontMetrics().stringWidth(sub)) / 2, my + 40);

        // --- Estadísticas finales con puntaje ---
        // Fórmula: (monedas × 10) + (tiempo_restante × 2) - (muertes × 5)
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        Player p1 = state.getPlayer1();
        int score1 = state.calculateScore(p1);
        String stats1 = "P1 — Monedas: $" + p1.getCoinsCollected()
                + "  Muertes: " + p1.getDeaths()
                + "  |  PUNTAJE: " + score1;
        g2.setColor(new Color(255, 120, 120));
        g2.drawString(stats1, (getWidth() - g2.getFontMetrics().stringWidth(stats1)) / 2, my + 75);

        if (state.getPlayer2() != null) {
            Player p2 = state.getPlayer2();
            int score2 = state.calculateScore(p2);
            String stats2 = "P2 — Monedas: $" + p2.getCoinsCollected()
                    + "  Muertes: " + p2.getDeaths()
                    + "  |  PUNTAJE: " + score2;
            g2.setColor(new Color(100, 180, 255));
            g2.drawString(stats2, (getWidth() - g2.getFontMetrics().stringWidth(stats2)) / 2, my + 100);
        }

    }

    public void setPlayerNames(String p1, String p2) {
        this.p1Name = p1;
        this.p2Name = p2;
    }
}