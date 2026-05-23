package dopo.domain.ai;

import dopo.domain.board.SafeZone;
import dopo.domain.elements.Coin;
import dopo.domain.elements.Wall;
import dopo.domain.enemies.Enemy;
import dopo.domain.game.GameState;
import dopo.domain.players.Player;

import java.util.*;

public class ExpertAI {

    private static final int DANGER_RADIUS = 60;
    private static final int GRID = 20; // tamaño de cada celda del grid

    public void update(GameState state) {
        Player p = state.getPlayer2();
        if (p == null) return;

        // Decidir objetivo
        int targetX, targetY;
        Coin nearest = getNearestCoin(state, p);
        if (nearest != null) {
            targetX = nearest.getX() + nearest.getSize() / 2 - p.getSize() / 2;
            targetY = nearest.getY() + nearest.getSize() / 2 - p.getSize() / 2;
        } else {
            SafeZone zona = state.getConfig().getStartZone();
            targetX = zona.getCenterX() - p.getSize() / 2;
            targetY = zona.getCenterY() - p.getSize() / 2;
        }

        // Buscar camino con A*
        int[] nextStep = astar(state, p.getX(), p.getY(), targetX, targetY, p.getSize());

        int dx = 0, dy = 0;
        if (nextStep != null) {
            dx = Integer.compare(nextStep[0], p.getX());
            dy = Integer.compare(nextStep[1], p.getY());
        }

        // Esquivar enemigos cercanos
        Enemy danger = getNearestEnemy(state, p);
        if (danger != null) {
            // Solo esquivar si el enemigo está en la misma fila o columna
            boolean mismaFila    = Math.abs(danger.getY() - p.getY()) < GRID * 2;
            boolean mismaColumna = Math.abs(danger.getX() - p.getX()) < GRID * 2;
            if (mismaFila && !mismaColumna) dy = Integer.compare(p.getY(), danger.getY());
            if (mismaColumna && !mismaFila) dx = Integer.compare(p.getX(), danger.getX());
        }

        // Aplicar movimiento
        int sp = (int) p.getCurrentSpeed();

	     // Si está muy cerca del objetivo, teletransportar directo
	     int distX = Math.abs(targetX - p.getX());
	     int distY = Math.abs(targetY - p.getY());
	     if (distX <= sp && distY <= sp) {
	         p.setX(targetX);
	         p.setY(targetY);
	         return;
	     }
	
	     int newX = p.getX() + dx * sp;
	     int newY = p.getY() + dy * sp;
	
	     int bw = state.getConfig().getBoardWidth();
	     int bh = state.getConfig().getBoardHeight();
	     int ps = p.getSize();
	     newX = Math.max(0, Math.min(newX, bw - ps));
	     newY = Math.max(0, Math.min(newY, bh - ps));
	
	     boolean blockedX = false, blockedY = false;
	     for (Wall w : state.getConfig().getWalls()) {
	         if (w.blocks(newX, p.getY(), ps)) blockedX = true;
	         if (w.blocks(p.getX(), newY, ps)) blockedY = true;
	     }
	
	     if (!blockedX) p.setX(newX);
	     if (!blockedY) p.setY(newY);
    }

    /**
     * A* pathfinding sobre un grid de celdas de tamaño GRID.
     * Retorna las coordenadas del siguiente paso hacia el objetivo,
     * o null si no hay camino.
     */
    private int[] astar(GameState state, int startX, int startY, int goalX, int goalY, int playerSize) {
        int bw = state.getConfig().getBoardWidth();
        int bh = state.getConfig().getBoardHeight();
        int cols = bw / GRID;
        int rows = bh / GRID;

        int sc = startX / GRID, sr = startY / GRID;
        int gc = goalX  / GRID, gr = goalY  / GRID;

        if (sc == gc && sr == gr) {
            return new int[]{goalX, goalY};
        }

        // Nodo: [col, row, g, f, parentCol, parentRow]
        Map<Integer, int[]> open   = new HashMap<>();
        Map<Integer, int[]> closed = new HashMap<>();
        Map<Integer, int[]> parent = new HashMap<>();

        int startKey = key(sc, sr, cols);
        int[] startNode = {sc, sr, 0, heuristic(sc, sr, gc, gr)};
        open.put(startKey, startNode);

        while (!open.isEmpty()) {
            // Nodo con menor f
            int[] current = open.values().stream()
                .min(Comparator.comparingInt(n -> n[3]))
                .orElse(null);
            if (current == null) break;

            int cc = current[0], cr = current[1];
            int ck = key(cc, cr, cols);

            if (cc == gc && cr == gr) {
                // Reconstruir camino y retornar el primer paso
                return firstStep(sc, sr, cc, cr, parent, cols);
            }

            open.remove(ck);
            closed.put(ck, current);

            // Vecinos (4 direcciones)
            int[][] dirs = {{0,-1},{0,1},{-1,0},{1,0}};
            for (int[] d : dirs) {
                int nc = cc + d[0], nr = cr + d[1];
                if (nc < 0 || nr < 0 || nc >= cols || nr >= rows) continue;
                int nk = key(nc, nr, cols);
                if (closed.containsKey(nk)) continue;
                if (isBlocked(state, nc * GRID, nr * GRID, playerSize)) continue;

                int g = current[2] + 1;
                int f = g + heuristic(nc, nr, gc, gr);

                if (!open.containsKey(nk) || open.get(nk)[2] > g) {
                    open.put(nk, new int[]{nc, nr, g, f});
                    parent.put(nk, new int[]{cc, cr});
                }
            }
        }
        return null; // no hay camino
    }

    /**
     * Reconstruye el camino desde el destino hasta el origen y retorna
     * las coordenadas del primer paso (el más cercano al inicio).
     */
    private int[] firstStep(int sc, int sr, int gc, int gr,
                             Map<Integer, int[]> parent, int cols) {
        int cc = gc, cr = gr;
        int pc = gc, pr = gr;
        while (true) {
            int k = key(cc, cr, cols);
            int[] p = parent.get(k);
            if (p == null) break;
            if (p[0] == sc && p[1] == sr) {
                return new int[]{cc * GRID, cr * GRID};
            }
            pc = cc; pr = cr;
            cc = p[0]; cr = p[1];
        }
        return new int[]{pc * GRID, pr * GRID};
    }

    /**
     * Verifica si una celda del grid está bloqueada por una pared.
     */
    private boolean isBlocked(GameState state, int px, int py, int playerSize) {
        for (Wall w : state.getConfig().getWalls()) {
            if (w.blocks(px, py, playerSize)) return true;
        }
        return false;
    }

    private int heuristic(int c1, int r1, int c2, int r2) {
        return Math.abs(c1 - c2) + Math.abs(r1 - r2);
    }

    private int key(int col, int row, int cols) {
        return row * cols + col;
    }

    private Coin getNearestCoin(GameState state, Player p) {
        Coin nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Coin c : state.getConfig().getCoins()) {
            if (c.isCollected()) continue;
            double dist = distance(p.getX(), p.getY(), c.getX(), c.getY());
            if (dist < minDist) { minDist = dist; nearest = c; }
        }
        return nearest;
    }

    private Enemy getNearestEnemy(GameState state, Player p) {
        Enemy nearest = null;
        double minDist = DANGER_RADIUS;
        for (Enemy e : state.getConfig().getEnemies()) {
            if (!e.isActive()) continue;
            double dist = distance(p.getX(), p.getY(), e.getX(), e.getY());
            if (dist < minDist) { minDist = dist; nearest = e; }
        }
        return nearest;
    }

    private double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}