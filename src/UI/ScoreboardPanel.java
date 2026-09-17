package UI;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.List;
import Game.Scoreboard;
import Model.Mutant;

public class ScoreboardPanel extends JPanel {

    public JButton btnNewGame; // Botón para iniciar un juego nuevo sin cerrar la aplicación
    public JTextField txtTeamSize; // Campo de texto para que el usuario ingrese la cantidad de mutantes (3 a 11)

    public ScoreboardPanel() {
    }

    private void updateCount(Scoreboard currentScoreboard) {
        // Actualiza las etiquetas de vivos y muertos por equipo
    }

    private void drawPlayersEnergy(List<Mutant> mutants) {
        // Lista visual con la energía restante de cada mutante
    }
}
