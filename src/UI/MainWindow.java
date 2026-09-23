package UI;

import Game.Battlefield;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class MainWindow extends JFrame implements BattleObserver {

    private static final int DEFAULT_REFRESH_RATE = 40; // ms entre cada repintado por defecto

    public BattlePanel battlePanel; // Objeto tipo canvas o JPanel donde se dibujan y mueven los mutantes
    public ScoreboardPanel scoreboardPanel; // Objeto para mostrar estadísticas y controles
    public int refreshRate; // Tasa de refresco configurable para el renderizado en tiempo real

    public MainWindow() {
        this(DEFAULT_REFRESH_RATE);
    }

    public MainWindow(int refreshRate) {
        super("Mutant Battle");
        this.refreshRate = refreshRate;
        this.battlePanel = new BattlePanel();
        this.scoreboardPanel = new ScoreboardPanel(refreshRate);
        configureWindow();
    }

    private void configureWindow() {
        // Configura el tamaño del JFrame y acomoda los paneles
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(battlePanel, BorderLayout.CENTER);
        add(scoreboardPanel, BorderLayout.EAST);
        setMinimumSize(new Dimension(900, 550));
        setSize(1200, 750);
        setLocationRelativeTo(null);
    }

    public void showNewGame(Battlefield battlefield) {
        // Reinicia ambos paneles con los mutantes de la nueva partida
        battlePanel.setBattlefield(battlefield);
        scoreboardPanel.setBattlefield(battlefield);
    }

    public void showWinnerMessage(String message) {
        // Muestra un aviso cuando el juego detecta un equipo ganador
        battlePanel.showBanner(message);
        scoreboardPanel.showStatus(message);
        JOptionPane.showMessageDialog(this, message + "\nPuedes iniciar una nueva partida.",
                "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Dato inválido", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void updateScreen(Battlefield battlefield) {
        // Sobrescribe el método de la interfaz para invocar el repintado de los gráficos
        if (battlefield == null) {
            return;
        }
        battlePanel.repaint();
        scoreboardPanel.refresh(battlefield);
    }
}
