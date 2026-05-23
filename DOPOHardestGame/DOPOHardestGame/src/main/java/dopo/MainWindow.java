package dopo;
import dopo.presentation.*;

import dopo.domain.board.LevelConfig;
import dopo.domain.game.GameMode;
import dopo.domain.game.GameState;
import dopo.domain.players.*;
import dopo.exceptions.GameException;
import dopo.persistence.GameSaveManager;
import dopo.persistence.LevelLoader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal de la aplicación. Gestiona las transiciones de pantalla: 
 * Menú → Juego → Ganar/Perder → Menú
 */
public class MainWindow extends JFrame {

    private static final int WINDOW_W = 900;
    private static final int WINDOW_H = 620;
    private static final String CONFIGS_DIR = "configs/";
    private static final String SAVES_DIR   = "saves/";

    private CardLayout cardLayout;
    private JPanel     cardPanel;

    private GamePanel gamePanel;
    private GameLoop  gameLoop;
    private GameState currentState;
    private String p1Name = "Jugador 1";
    private String p2Name = "Jugador 2";

    public MainWindow() {
        super("The DOPO Hardest Game 2026-1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_W, WINDOW_H);
        setLocationRelativeTo(null);
        setResizable(false);

        // Asegúrar de que el directorio de guardado exista
        new File(SAVES_DIR).mkdirs();

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(Color.BLACK);

        gamePanel = new GamePanel();
        cardPanel.add(gamePanel, "game");

        add(cardPanel);

        showMenu();
    }

    // Transiciones de pantalla

    private void showMenu() {
        List<String> configs = findConfigs();
        MenuScreen menu = new MenuScreen(new MenuScreen.MenuCallback() {
            @Override
            public void onStart(GameMode mode, Player.PlayerType p1Type, Player.PlayerType p2Type,
                    Color p1Border, Color p2Border, String configName,
                    String p1Name, String p2Name) {
			    MainWindow.this.p1Name = p1Name;
			    MainWindow.this.p2Name = p2Name;
			    startGame(mode, p1Type, p2Type, p1Border, p2Border, CONFIGS_DIR + configName);
			}
            
            @Override
            public void onLoad(String saveFilePath) {
                loadGame(saveFilePath);
            }
        }, configs);

        cardPanel.add(menu, "menu");
        cardLayout.show(cardPanel, "menu");
    }

    private void startGame(GameMode mode, Player.PlayerType p1t, Player.PlayerType p2t,
                           Color p1Border, Color p2Border, String configPath) {
        try {
            LevelConfig config = LevelLoader.load(configPath);

            Player p1 = PlayerFactory.create(p1t, config.getP1StartX(), config.getP1StartY());
            p1.setBorderColor(p1Border);

            Player p2 = null;
            if (mode != GameMode.PLAYER) {
                p2 = PlayerFactory.create(p2t, config.getP2StartX(), config.getP2StartY());
                p2.setBorderColor(p2Border);
            }
            if (p1 instanceof GreenPlayer) ((GreenPlayer) p1).restoreAbility();
            if (p2 instanceof GreenPlayer) ((GreenPlayer) p2).restoreAbility();

            currentState = new GameState(config, mode, p1, p2);

            if (gameLoop != null) {
                gameLoop.stop();
                gameLoop.removeKeyListeners(gamePanel);
            }
            gameLoop = new GameLoop(currentState, gamePanel);
            gamePanel.setPlayerNames(p1Name, p2Name);
            gameLoop.setOnWin(this::onWin);
            gameLoop.setOnLose(this::onLose);
            gameLoop.setOnEscape(this::onEscapeToMenu);
            gameLoop.setOnRestart(this::restartLevel);

            cardLayout.show(cardPanel, "game");
            gameLoop.start();

            // Agregar elemento de menú guardar
            addGameMenu();

        } catch (GameException ex) {
            JOptionPane.showMessageDialog(this,
                "Error cargando el juego:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadGame(String saveFilePath) {
        try {
            var props = GameSaveManager.loadMeta(saveFilePath);
            String levelName = props.getProperty("levelName", "");
            String configPath = CONFIGS_DIR + levelName + ".txt";
            GameMode mode = GameMode.valueOf(props.getProperty("mode", "PLAYER"));

            LevelConfig config = LevelLoader.load(configPath);

            // Reconstruir jugadores a partir de tipos guardados
            Player.PlayerType p1t = Player.PlayerType.valueOf(props.getProperty("p1.type", "RED"));
            Player p1 = PlayerFactory.create(p1t, config.getP1StartX(), config.getP1StartY());

            Player p2 = null;
            if (props.containsKey("p2.type")) {
                Player.PlayerType p2t = Player.PlayerType.valueOf(props.getProperty("p2.type", "RED"));
                p2 = PlayerFactory.create(p2t, config.getP2StartX(), config.getP2StartY());
            }

            currentState = new GameState(config, mode, p1, p2);
            GameSaveManager.restore(currentState, saveFilePath);

            if (gameLoop != null) gameLoop.stop();
            gameLoop = new GameLoop(currentState, gamePanel);
            gameLoop.setOnWin(this::onWin);
            gameLoop.setOnLose(this::onLose);
            gameLoop.setOnEscape(this::onEscapeToMenu);

            cardLayout.show(cardPanel, "game");
            gameLoop.start();
            addGameMenu();

        } catch (GameException ex) {
            JOptionPane.showMessageDialog(this,
                "Error cargando partida:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onWin() {
        gamePanel.repaint();
        String nivelActual = currentState.getConfig().getName();
        List<String> configs = findConfigs();
        int indiceActual = configs.indexOf(nivelActual + ".txt");
        boolean haySiguiente = indiceActual >= 0 && indiceActual < configs.size() - 1;

        String[] opciones = haySiguiente
            ? new String[]{"Siguiente nivel", "Menú principal", "Salir"}
            : new String[]{"Menú principal", "Salir"};

        String mensaje = haySiguiente
            ? "¡Felicitaciones! Nivel completado.\n¿Quieres continuar al siguiente nivel?"
            : "¡Felicitaciones! Completaste todos los niveles.";

        int choice = JOptionPane.showOptionDialog(this, mensaje, "¡Victoria!",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, opciones, opciones[0]);

        if (haySiguiente && choice == 0) {
            String siguienteConfig = configs.get(indiceActual + 1);
            startGame(currentState.getMode(),
                currentState.getPlayer1().getType(),
                currentState.getPlayer2() != null ? currentState.getPlayer2().getType() : Player.PlayerType.RED,
                currentState.getPlayer1().getBorderColor(),
                currentState.getPlayer2() != null ? currentState.getPlayer2().getBorderColor() : java.awt.Color.CYAN,
                CONFIGS_DIR + siguienteConfig);
        } else if (choice == opciones.length - 2) {
            showMenu();
        } else {
            System.exit(0);
        }
    }

    private void onLose() {
        gamePanel.repaint();
        int choice = JOptionPane.showOptionDialog(this,
            "Se agotó el tiempo. ¿Quieres intentarlo de nuevo?",
            "Tiempo agotado",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
            null, new String[]{"Reintentar", "Menú principal", "Salir"}, "Reintentar");
        if (choice == 0) {
            // Restart with same config
            GameState s = currentState;
            startGame(s.getMode(),
                s.getPlayer1().getType(),
                s.getPlayer2() != null ? s.getPlayer2().getType() : Player.PlayerType.RED,
                s.getPlayer1().getBorderColor(),
                s.getPlayer2() != null ? s.getPlayer2().getBorderColor() : Color.CYAN,
                CONFIGS_DIR + s.getConfig().getName() + ".txt");
        } else if (choice == 1) showMenu();
        else System.exit(0);
    }

    private void onEscapeToMenu() {
        if (gameLoop != null) gameLoop.stop();
        int choice = JOptionPane.showOptionDialog(this,
            "¿Deseas guardar la partida antes de salir?",
            "Pausar juego",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, new String[]{"Guardar y salir", "Salir sin guardar", "Continuar"}, "Continuar");
        if (choice == 0) {
            saveGame();
            showMenu();
        } else if (choice == 1) {
            showMenu();
        } else {
            gameLoop.start();
        }
    }

    private void saveGame() {
        if (currentState == null) return;
        new File(SAVES_DIR).mkdirs();
        String path = SAVES_DIR + currentState.getConfig().getName() + "_save.properties";
        try {
            GameSaveManager.save(currentState, path);
            JOptionPane.showMessageDialog(this, "Partida guardada en:\n" + path, "Guardado", JOptionPane.INFORMATION_MESSAGE);
        } catch (GameException ex) {
            JOptionPane.showMessageDialog(this, "Error guardando:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addGameMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Juego");
        JMenuItem saveItem = new JMenuItem("Guardar partida");
        saveItem.addActionListener(e -> saveGame());
        JMenuItem menuItem = new JMenuItem("Menú principal");
        menuItem.addActionListener(e -> onEscapeToMenu());
        JMenuItem exitItem = new JMenuItem("Salir");
        exitItem.addActionListener(e -> System.exit(0));
        menu.add(saveItem); menu.addSeparator(); menu.add(menuItem); menu.addSeparator(); menu.add(exitItem);
        bar.add(menu);
        setJMenuBar(bar);
    }

    private List<String> findConfigs() {
        List<String> list = new ArrayList<>();
        File dir = new File(CONFIGS_DIR);
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                if (f.getName().endsWith(".txt")) list.add(f.getName());
            }
        }
        if (list.isEmpty()) list.add("level1.txt");
        return list;
    }
    
    private void restartLevel() {
        startGame(
            currentState.getMode(),
            currentState.getPlayer1().getType(),
            currentState.getPlayer2() != null ? currentState.getPlayer2().getType() : null,
            currentState.getPlayer1().getBorderColor(),
            currentState.getPlayer2() != null ? currentState.getPlayer2().getBorderColor() : null,
            "configs/" + currentState.getConfig().getName() + ".txt"
        );
    }

    // Punto de entrada

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new MainWindow().setVisible(true);
        });
    }
}
