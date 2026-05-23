package dopo.presentation;

import dopo.domain.game.GameMode;
import dopo.domain.players.Player;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Menú de inicio: elige el modo de juego, tipo de jugador y configuración de nivel.
 */
public class MenuScreen extends JPanel {

    public interface MenuCallback {
    	void onStart(GameMode mode, Player.PlayerType p1Type, Player.PlayerType p2Type,
                Color p1Border, Color p2Border, String configPath,
                String p1Name, String p2Name);
        void onLoad(String saveFilePath);
    }

    private MenuCallback callback;
    private List<String> availableConfigs;

    // Componentes de interfaz de usuario
    private JComboBox<String> modeCombo;
    private JTextField p1NameField;
    private JTextField p2NameField;
    private JLabel p2NameLabel;
    private JComboBox<String> p1TypeCombo;
    private JComboBox<String> p2TypeCombo;
    private JButton p1ColorBtn, p2ColorBtn;
    private Color p1BorderColor = Color.WHITE;
    private Color p2BorderColor = Color.CYAN;
    private JComboBox<String> configCombo;
    private JLabel p2Label;
    

    public MenuScreen(MenuCallback callback, List<String> configs) {
        this.callback = callback;
        this.availableConfigs = configs;
        buildUI();
    }

    private void buildUI() {
        setBackground(new Color(15, 15, 30));
        setLayout(new BorderLayout());

        // titulo
        JLabel title = new JLabel("THE DOPO HARDEST GAME", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 28));
        title.setForeground(new Color(255, 70, 70));
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Escuela Colombiana de Ingeniería · DOPO 2026-1", SwingConstants.CENTER);
        subtitle.setFont(new Font("Monospaced", Font.PLAIN, 12));
        subtitle.setForeground(new Color(120, 120, 150));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(15, 15, 30));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        add(titlePanel, BorderLayout.NORTH);

        // Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(25, 25, 45));
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(20, 60, 20, 60),
            BorderFactory.createLineBorder(new Color(60, 60, 100), 1)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // modo
        addRow(form, gbc, 0, "Modo de juego:", 
            modeCombo = new JComboBox<>(new String[]{"Player (1 jugador)", "Player vs Player", "PvsM - Aleatorio", "PvsM - Experto"}));

        // Configuracion
        String[] cfgArr = availableConfigs.toArray(new String[0]);
        addRow(form, gbc, 1, "Configuración:", configCombo = new JComboBox<>(cfgArr));

        // P1 tipo
        addRow(form, gbc, 2, "Tipo P1 (Skin):",
            p1TypeCombo = new JComboBox<>(new String[]{"Rojo (Blinky) - Estándar", "Azul (Inky) - Rápido/Grande", "Verde (Clyde) - Resistente"}));

        // P1 borde de color
        p1ColorBtn = colorButton("Color borde P1", p1BorderColor);
        p1ColorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Color borde Jugador 1", p1BorderColor);
            if (c != null) { p1BorderColor = c; p1ColorBtn.setBackground(c); }
        });
        addRow(form, gbc, 3, "Borde P1:", p1ColorBtn);

        // Nombre P1
        p1NameField = new JTextField("Jugador 1");
        p1NameField.setBackground(new Color(40, 40, 65));
        p1NameField.setForeground(Color.WHITE);
        p1NameField.setCaretColor(Color.WHITE);
        addRow(form, gbc, 4, "Nombre P1:", p1NameField);

        // P2 sección
        p2Label = addLabel(form, gbc, 5, "Tipo P2 (Skin):");
        p2TypeCombo = new JComboBox<>(new String[]{"Rojo (Blinky)", "Azul (Inky)", "Verde (Clyde)"});
        addComponent(form, gbc, 5, p2TypeCombo);

        p2ColorBtn = colorButton("Color borde P2", p2BorderColor);
        p2ColorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Color borde Jugador 2", p2BorderColor);
            if (c != null) { p2BorderColor = c; p2ColorBtn.setBackground(c); }
        });
        addRow(form, gbc, 6, "Borde P2:", p2ColorBtn);

        // Nombre P2
        p2NameLabel = addLabel(form, gbc, 7, "Nombre P2:");
        p2NameField = new JTextField("Jugador 2");
        p2NameField.setBackground(new Color(40, 40, 65));
        p2NameField.setForeground(Color.WHITE);
        p2NameField.setCaretColor(Color.WHITE);
        addComponent(form, gbc, 7, p2NameField);

        // Actualizar visibilidad de P2
        modeCombo.addActionListener(e -> updateP2Visibility());
        updateP2Visibility();

        add(form, BorderLayout.CENTER);

        // Botones
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnPanel.setBackground(new Color(15, 15, 30));

        JButton startBtn = styledButton("▶  JUGAR", new Color(50, 180, 80));
        startBtn.addActionListener(e -> onStart());

        JButton loadBtn = styledButton("📂  CARGAR PARTIDA", new Color(60, 100, 180));
        loadBtn.addActionListener(e -> onLoad());

        btnPanel.add(startBtn);
        btnPanel.add(loadBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void updateP2Visibility() {
        boolean multi = modeCombo.getSelectedIndex() > 0;
        p2TypeCombo.setVisible(multi);
        p2ColorBtn.setVisible(multi);
        p2Label.setVisible(multi);
        p2NameField.setVisible(multi);
        p2NameLabel.setVisible(multi);
        // también ocultar la etiqueta del borde P2 — encontrarla por posición
    }

    private void onStart() {
        int modeIdx = modeCombo.getSelectedIndex();
        GameMode mode;
        switch (modeIdx) {
            case 1: mode = GameMode.PVP;        break;
            case 2: mode = GameMode.PVM_RANDOM; break;
            case 3: mode = GameMode.PVM_EXPERT; break;
            default: mode = GameMode.PLAYER;
        }

        Player.PlayerType p1t = playerTypeFromIndex(p1TypeCombo.getSelectedIndex());
        Player.PlayerType p2t = playerTypeFromIndex(p2TypeCombo.getSelectedIndex());
        String cfg = (String) configCombo.getSelectedItem();

        String p1Name = p1NameField.getText().trim().isEmpty() ? "Jugador 1" : p1NameField.getText().trim();
        String p2Name = p2NameField.getText().trim().isEmpty() ? "Jugador 2" : p2NameField.getText().trim();
        callback.onStart(mode, p1t, p2t, p1BorderColor, p2BorderColor, cfg, p1Name, p2Name);
    }

    private void onLoad() {
        JFileChooser fc = new JFileChooser("saves/");
        fc.setDialogTitle("Abrir partida guardada");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            callback.onLoad(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private Player.PlayerType playerTypeFromIndex(int idx) {
        switch (idx) {
            case 1: return Player.PlayerType.BLUE;
            case 2: return Player.PlayerType.GREEN;
            default: return Player.PlayerType.RED;
        }
    }

    // Ayudantes de IU

    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton colorButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        int luminancia = (bg.getRed() * 299 + bg.getGreen() * 587 + bg.getBlue() * 114) / 1000;
        b.setForeground(luminancia > 128 ? Color.BLACK : Color.WHITE);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        return b;
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        addLabel(p, gbc, row, label);
        addComponent(p, gbc, row, comp);
    }

    private JLabel addLabel(JPanel p, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(180, 180, 210));
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        p.add(lbl, gbc);
        return lbl;
    }

    private void addComponent(JPanel p, GridBagConstraints gbc, int row, JComponent comp) {
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.7;
        comp.setFont(new Font("SansSerif", Font.PLAIN, 13));
        if (comp instanceof JComboBox) {
            comp.setBackground(new Color(40, 40, 65));
            comp.setForeground(Color.WHITE);
            comp.setOpaque(true);
            ((JComboBox<?>) comp).setRenderer(new javax.swing.plaf.basic.BasicComboBoxRenderer() {
                @Override
                public java.awt.Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setBackground(isSelected ? new Color(70, 70, 100) : new Color(40, 40, 65));
                    setForeground(Color.WHITE);
                    return this;
                }
            });
        }
        p.add(comp, gbc);
    }
}
