package UI;

public class MainWindow implements BattleObserver {

    public BattlePanel battlePanel; // Objeto tipo canvas o JPanel donde se dibujan y mueven los mutantes
    public ScoreboardPanel scoreboardPanel; // Objeto para mostrar estadísticas y controles
    public int refreshRate; // Tasa de refresco configurable para el renderizado en tiempo real

    public MainWindow() {
    }

    private void configureWindow() {
        // Configura el tamaño del JFrame y acomoda los paneles
    }

    private void showWinnerMessage() {
        // Muestra un aviso cuando el juego detecta un equipo ganador
    }

    @Override
    public void updateScreen() {
        // Sobrescribe el método de la interfaz para invocar el repintado de los gráficos
    }
}
