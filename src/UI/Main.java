package UI;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // Toda la interfaz de Swing se construye en el hilo de eventos (EDT)
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            new UIController(window);
            window.setVisible(true);
        });
    }
}
