package UI;

import Game.Battlefield;
import Game.Scoreboard;
import Model.Mutant;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class ScoreboardPanel extends JPanel {

    private static final int MIN_REFRESH_RATE = 16; // ms, ~60 repintados por segundo
    private static final int MAX_REFRESH_RATE = 2000; // ms
    private static final int REFRESH_STEP = 10;

    public JButton btnNewGame; // Botón para iniciar un juego nuevo sin cerrar la aplicación
    public JTextField txtTeamSize; // Campo de texto para que el usuario ingrese la cantidad de mutantes (3 a 11)
    public JSpinner spnRefreshRate; // Frecuencia de actualización de la pantalla en milisegundos

    private final JLabel lblTeam1 = new JLabel("Equipo 1");
    private final JLabel lblTeam2 = new JLabel("Equipo 2");
    private final JLabel lblAlive1 = new JLabel("Vivos: -");
    private final JLabel lblDead1 = new JLabel("Muertos: -");
    private final JLabel lblAlive2 = new JLabel("Vivos: -");
    private final JLabel lblDead2 = new JLabel("Muertos: -");
    private final JLabel lblStatus = new JLabel("Sin partida");

    private final JPanel energyList = new JPanel(); // lista de barras de energía, una por mutante
    private final Map<Mutant, JProgressBar> energyBars = new IdentityHashMap<>();
    private Battlefield battlefield;

    public ScoreboardPanel(int refreshRate) {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setPreferredSize(new Dimension(320, 700));

        add(buildStatsPanel(), BorderLayout.NORTH);
        add(buildEnergyPanel(), BorderLayout.CENTER);
        add(buildControlsPanel(refreshRate), BorderLayout.SOUTH);
    }

    private JPanel buildStatsPanel() {
        JPanel stats = new JPanel(new GridLayout(0, 1, 0, 4));

        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD, 15f));
        stats.add(lblStatus);

        lblTeam1.setForeground(BattlePanel.TEAM1_COLOR);
        lblTeam1.setFont(lblTeam1.getFont().deriveFont(Font.BOLD, 14f));
        stats.add(lblTeam1);
        stats.add(teamRow(lblAlive1, lblDead1));

        lblTeam2.setForeground(BattlePanel.TEAM2_COLOR);
        lblTeam2.setFont(lblTeam2.getFont().deriveFont(Font.BOLD, 14f));
        stats.add(lblTeam2);
        stats.add(teamRow(lblAlive2, lblDead2));
        return stats;
    }

    private JPanel teamRow(JLabel alive, JLabel dead) {
        JPanel row = new JPanel(new GridLayout(1, 2));
        row.add(alive);
        row.add(dead);
        return row;
    }

    private JScrollPane buildEnergyPanel() {
        energyList.setLayout(new BoxLayout(energyList, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(energyList);
        scroll.setBorder(BorderFactory.createTitledBorder("Energía de los mutantes"));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel buildControlsPanel(int refreshRate) {
        JPanel controls = new JPanel(new GridLayout(0, 2, 6, 6));
        controls.setBorder(BorderFactory.createTitledBorder("Partida"));

        txtTeamSize = new JTextField("5");
        spnRefreshRate = new JSpinner(new SpinnerNumberModel(
                Math.max(MIN_REFRESH_RATE, Math.min(MAX_REFRESH_RATE, refreshRate)),
                MIN_REFRESH_RATE, MAX_REFRESH_RATE, REFRESH_STEP));
        btnNewGame = new JButton("Nueva partida");

        controls.add(new JLabel("Mutantes por equipo (3-11):"));
        controls.add(txtTeamSize);
        controls.add(new JLabel("Refresco (ms):"));
        controls.add(spnRefreshRate);
        controls.add(Box.createGlue());
        controls.add(btnNewGame);
        return controls;
    }

    public void setBattlefield(Battlefield battlefield) {
        // Reconstruye la lista de energía con los mutantes de la nueva partida
        this.battlefield = battlefield;
        lblTeam1.setText("Equipo " + (int) battlefield.team1.name);
        lblTeam2.setText("Equipo " + (int) battlefield.team2.name);

        energyBars.clear();
        energyList.removeAll();
        for (Mutant mutant : battlefield.getAllMutants()) {
            JLabel name = new JLabel("Equipo " + (int) mutant.team + " · Mutante #" + (int) mutant.id);
            name.setForeground(BattlePanel.teamColor(battlefield, mutant));
            name.setAlignmentX(Component.LEFT_ALIGNMENT);

            JProgressBar bar = new JProgressBar(0, Math.max(mutant.energy, 1));
            bar.setStringPainted(true);
            bar.setAlignmentX(Component.LEFT_ALIGNMENT);
            bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

            energyList.add(name);
            energyList.add(bar);
            energyList.add(Box.createVerticalStrut(6));
            energyBars.put(mutant, bar);
        }
        energyList.revalidate();
        energyList.repaint();
        refresh(battlefield);
    }

    public void refresh(Battlefield battlefield) {
        // Consulta el estado actual de la partida y actualiza las etiquetas y barras
        if (battlefield != this.battlefield) {
            return;
        }
        updateCount(battlefield.scoreboard);
        drawPlayersEnergy(battlefield.getAllMutants());
    }

    public void setGameRunning(boolean running) {
        // Mientras la partida está en curso no se puede empezar otra
        btnNewGame.setEnabled(!running);
        txtTeamSize.setEnabled(!running);
        if (running) {
            showStatus("Batalla en curso...");
        }
    }

    public void showStatus(String text) {
        lblStatus.setText("<html>" + text + "</html>");
    }

    private void updateCount(Scoreboard currentScoreboard) {
        // Actualiza las etiquetas de vivos y muertos por equipo
        lblAlive1.setText("Vivos: " + currentScoreboard.aliveTeam1);
        lblDead1.setText("Muertos: " + currentScoreboard.deadTeam1);
        lblAlive2.setText("Vivos: " + currentScoreboard.aliveTeam2);
        lblDead2.setText("Muertos: " + currentScoreboard.deadTeam2);
    }

    private void drawPlayersEnergy(List<Mutant> mutants) {
        // Lista visual con la energía restante de cada mutante
        for (Mutant mutant : mutants) {
            JProgressBar bar = energyBars.get(mutant);
            if (bar == null) {
                continue;
            }
            bar.setValue(mutant.energy);
            if (mutant.isAlive) {
                bar.setForeground(BattlePanel.teamColor(battlefield, mutant));
                bar.setString(mutant.energy + " / " + bar.getMaximum());
            } else {
                bar.setForeground(BattlePanel.DEAD_COLOR);
                bar.setString("Muerto");
            }
        }
    }
}
