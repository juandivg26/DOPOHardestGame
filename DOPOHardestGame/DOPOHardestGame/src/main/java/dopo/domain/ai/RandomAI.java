package dopo.domain.ai;

import dopo.domain.game.GameState;
import dopo.domain.players.Player;
import dopo.domain.elements.Wall;

import java.util.Random;

public class RandomAI {

    private static final int CHANGE_DIRECTION_TICKS = 30;
    private int dx, dy;
    private int tickCount;
    private Random random;

    public RandomAI() {
        random = new Random();
        chooseNewDirection();
        tickCount = 0;
    }

    public void update(GameState state) {
        Player p = state.getPlayer2();
        if (p == null) return;

        tickCount++;
        if (tickCount >= CHANGE_DIRECTION_TICKS) {
            chooseNewDirection();
            tickCount = 0;
        }

        int sp = (int) p.getCurrentSpeed();
        int newX = p.getX() + dx * sp;
        int newY = p.getY() + dy * sp;

        // Limitar a los bordes del tablero
        int bw = state.getConfig().getBoardWidth();
        int bh = state.getConfig().getBoardHeight();
        int ps = p.getSize();
        newX = Math.max(0, Math.min(newX, bw - ps));
        newY = Math.max(0, Math.min(newY, bh - ps));

        // Verificar paredes
        boolean blockedX = false, blockedY = false;
        for (Wall w : state.getConfig().getWalls()) {
            if (w.blocks(newX, p.getY(), ps)) blockedX = true;
            if (w.blocks(p.getX(), newY, ps)) blockedY = true;
        }

        if (!blockedX) p.setX(newX);
        else { dx = -dx; }

        if (!blockedY) p.setY(newY);
        else { dy = -dy; }
    }

    private void chooseNewDirection() {
        int[] options = {-1, 0, 1};
        dx = options[random.nextInt(3)];
        dy = options[random.nextInt(3)];
        // Evitar que se quede quieta
        if (dx == 0 && dy == 0) dx = 1;
    }
}