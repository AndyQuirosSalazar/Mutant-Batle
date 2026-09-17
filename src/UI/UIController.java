package UI;

import Game.Battlefield;

public class UIController {

    public Battlefield gameModel; // Referencia de solo lectura a la capa Game
    public MainWindow view; // Referencia a la interfaz gráfica

    public UIController() {
    }

    private void startGame() {
        // Captura el clic del botón, valida el número y delega el inicio a la capa Game
    }

    private void connectObserver() {
        // Suscribe la VentanaPrincipal a la lista de observadores del CampoDeBatalla para mantenerlos sincronizados
    }
}
