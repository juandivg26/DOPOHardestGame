package dopo.persistence;

import dopo.domain.game.GameState;
import dopo.domain.game.GameMode;
import dopo.domain.elements.Coin;
import dopo.domain.elements.Bomb;
import dopo.domain.elements.LifeSource;
import dopo.domain.players.Player;
import dopo.exceptions.GameException;

import java.io.*;
import java.util.Properties;

/**
 * Guarda y carga instantáneas del estado del juego desde/hacia archivos .properties.
 * Esto cubre el requisito de la Versión 2: guardar y abrir partidas.
 */
public class GameSaveManager {

    /**
     * Guardar el estado actual del juego en un archivo.
     */
    public static void save(GameState state, String filePath) throws GameException {
        Properties props = new Properties();

        props.setProperty("mode", state.getMode().name());
        props.setProperty("timeRemainingMs", String.valueOf(state.getTimeRemainingMs()));
        props.setProperty("levelName", state.getConfig().getName());
        props.setProperty("status", state.getStatus().name());

        Player p1 = state.getPlayer1();
        props.setProperty("p1.type",   p1.getType().name());
        props.setProperty("p1.x",      String.valueOf(p1.getX()));
        props.setProperty("p1.y",      String.valueOf(p1.getY()));
        props.setProperty("p1.deaths", String.valueOf(p1.getDeaths()));
        props.setProperty("p1.coins",  String.valueOf(p1.getCoinsCollected()));
        props.setProperty("p1.spawnX", String.valueOf(p1.getSpawnX()));
        props.setProperty("p1.spawnY", String.valueOf(p1.getSpawnY()));
        props.setProperty("p1.shield", String.valueOf(p1.isShieldActive()));
        props.setProperty("p1.borderColor", colorToHex(p1.getBorderColor()));

        if (state.getPlayer2() != null) {
            Player p2 = state.getPlayer2();
            props.setProperty("p2.type",   p2.getType().name());
            props.setProperty("p2.x",      String.valueOf(p2.getX()));
            props.setProperty("p2.y",      String.valueOf(p2.getY()));
            props.setProperty("p2.deaths", String.valueOf(p2.getDeaths()));
            props.setProperty("p2.coins",  String.valueOf(p2.getCoinsCollected()));
            props.setProperty("p2.spawnX", String.valueOf(p2.getSpawnX()));
            props.setProperty("p2.spawnY", String.valueOf(p2.getSpawnY()));
            props.setProperty("p2.shield", String.valueOf(p2.isShieldActive()));
        }

        // Guardar estado de la colección de monedas
        StringBuilder coinStates = new StringBuilder();
        for (Coin c : state.getConfig().getCoins()) {
            coinStates.append(c.isCollected() ? "1" : "0");
        }
        props.setProperty("coins.collected", coinStates.toString());

        // Guardar estados de la bomba
        StringBuilder bombStates = new StringBuilder();
        for (Bomb b : state.getConfig().getBombs()) {
            bombStates.append(b.isExploded() ? "1" : "0");
        }
        props.setProperty("bombs.exploded", bombStates.toString());

        // Guardar estados de la fuente de vida
        StringBuilder lsStates = new StringBuilder();
        for (LifeSource ls : state.getConfig().getLifeSources()) {
            lsStates.append(ls.isUsed() ? "1" : "0");
        }
        props.setProperty("lifesources.used", lsStates.toString());

        try (FileWriter fw = new FileWriter(filePath)) {
            props.store(fw, "DOPO Hardest Game - Save File");
        } catch (IOException e) {
            throw new GameException("Failed to save game to: " + filePath, e);
        }
    }

    /**
     * Carga los metadatos guardados (nombre del nivel, modo) para mostrarlos antes de restaurar.
     */
    public static Properties loadMeta(String filePath) throws GameException {
        Properties props = new Properties();
        try (FileReader fr = new FileReader(filePath)) {
            props.load(fr);
        } catch (IOException e) {
            throw new GameException("Failed to load save file: " + filePath, e);
        }
        return props;
    }

    /**
     * Aplicar el estado guardado del jugador a un GameState existente (ya construido a partir de la configuración).
     */
    public static void restore(GameState state, String filePath) throws GameException {
        Properties props = loadMeta(filePath);

        Player p1 = state.getPlayer1();
        p1.setX(Integer.parseInt(props.getProperty("p1.x", "0")));
        p1.setY(Integer.parseInt(props.getProperty("p1.y", "0")));
        p1.setSpawnPoint(
            Integer.parseInt(props.getProperty("p1.spawnX", "0")),
            Integer.parseInt(props.getProperty("p1.spawnY", "0"))
        );

        // Restaurar estados de monedas
        String coinStr = props.getProperty("coins.collected", "");
        java.util.List<dopo.domain.elements.Coin> coins = state.getConfig().getCoins();
        for (int i = 0; i < coinStr.length() && i < coins.size(); i++) {
            if (coinStr.charAt(i) == '1') coins.get(i).collect();
        }

        // Restaurar estados de la bomba
        String bombStr = props.getProperty("bombs.exploded", "");
        java.util.List<dopo.domain.elements.Bomb> bombs = state.getConfig().getBombs();
        for (int i = 0; i < bombStr.length() && i < bombs.size(); i++) {
            if (bombStr.charAt(i) == '1') bombs.get(i).explode();
        }

        // Restaurar estados de la fuente de vida
        String lsStr = props.getProperty("lifesources.used", "");
        java.util.List<dopo.domain.elements.LifeSource> lsList = state.getConfig().getLifeSources();
        for (int i = 0; i < lsStr.length() && i < lsList.size(); i++) {
            if (lsStr.charAt(i) == '1') lsList.get(i).use();
        }
        
        String savedStatus = props.getProperty("status", "PLAYING");
        if (savedStatus.equals("PAUSED")) state.pause();
    }

    private static String colorToHex(java.awt.Color c) {
        if (c == null) return "#FFFFFF";
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
