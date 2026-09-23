package UI;

import Game.Battlefield;
import Game.Generator;
import Game.Scoreboard;
import Game.Team;

import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

public class UIController {

    private static final int MIN_TEAM_SIZE = 3; // cantidad minima de mutantes por equipo que acepta la UI
    private static final int MAX_TEAM_SIZE = 11; // cantidad maxima de mutantes por equipo que acepta la UI
    private static final double TEAM1_NAME = 1; // identificador del equipo 1
    private static final double TEAM2_NAME = 2; // identificador del equipo 2

    public Battlefield gameModel; // Referencia de solo lectura a la capa Game
    public MainWindow view; // Referencia a la interfaz gráfica

    private final List<BattleObserver> observers = new ArrayList<>(); // Observadores suscritos (patrón Observer)
    private final Timer refreshTimer; // Dispara la notificación a los observadores cada refreshRate ms
    private boolean winnerAnnounced; // Evita anunciar el ganador más de una vez por partida

    public UIController(MainWindow view) {
        this.view = view;
        this.refreshTimer = new Timer(view.refreshRate, e -> onRefreshTick());

        view.scoreboardPanel.btnNewGame.addActionListener(e -> startGame());
        view.scoreboardPanel.spnRefreshRate.addChangeListener(e -> changeRefreshRate());

        connectObserver();
    }

    public void addObserver(BattleObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(BattleObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        // Avisa a cada observador que debe consultar el Battlefield y redibujarse
        for (BattleObserver observer : observers) {
            observer.updateScreen(gameModel);
        }
    }

    private void startGame() {
        // Captura el clic del botón, valida el número y delega el inicio a la capa Game
        Integer teamSize = readTeamSize();
        if (teamSize == null) {
            return;
        }

        stopCurrentGame();

        Generator generator = new Generator();
        Team team1 = generator.generateTeam(TEAM1_NAME, teamSize);
        Team team2 = generator.generateTeam(TEAM2_NAME, teamSize);
        gameModel = new Battlefield(team1, team2); // el Battlefield arranca los hilos de los mutantes

        winnerAnnounced = false;
        view.showNewGame(gameModel);
        view.scoreboardPanel.setGameRunning(true);

        notifyObservers();
        refreshTimer.start();
    }

    private void connectObserver() {
        // Suscribe la VentanaPrincipal a la lista de observadores del CampoDeBatalla para mantenerlos sincronizados
        addObserver(view);
    }

    private void onRefreshTick() {
        // Cada tick del timer: se notifica a la vista y se consulta si la partida terminó
        if (gameModel == null) {
            return;
        }

        notifyObservers();

        if (!gameModel.gameActive.get() && !winnerAnnounced) {
            winnerAnnounced = true;
            refreshTimer.stop();
            view.scoreboardPanel.setGameRunning(false);
            view.showWinnerMessage(buildWinnerText(gameModel.scoreboard));
        }
    }

    private String buildWinnerText(Scoreboard scoreboard) {
        // Solo lee los contadores del Scoreboard para redactar el anuncio
        if (scoreboard.aliveTeam1 > 0 && scoreboard.aliveTeam2 == 0) {
            return "¡Ganó el Equipo " + (int) scoreboard.team1.name + "! (" + scoreboard.aliveTeam1 + " mutantes en pie)";
        }
        if (scoreboard.aliveTeam2 > 0 && scoreboard.aliveTeam1 == 0) {
            return "¡Ganó el Equipo " + (int) scoreboard.team2.name + "! (" + scoreboard.aliveTeam2 + " mutantes en pie)";
        }
        if (scoreboard.aliveTeam1 == 0 && scoreboard.aliveTeam2 == 0) {
            return "¡Empate! No quedó ningún mutante en pie";
        }
        return "La partida se interrumpió sin ganador";
    }

    private Integer readTeamSize() {
        // Valida que la cantidad ingresada sea un entero entre MIN_TEAM_SIZE y MAX_TEAM_SIZE
        String text = view.scoreboardPanel.txtTeamSize.getText().trim();
        try {
            int teamSize = Integer.parseInt(text);
            if (teamSize >= MIN_TEAM_SIZE && teamSize <= MAX_TEAM_SIZE) {
                return teamSize;
            }
        } catch (NumberFormatException ignored) {
            // se reporta abajo con el mismo mensaje
        }
        view.showError("La cantidad de mutantes por equipo debe ser un número entre "
                + MIN_TEAM_SIZE + " y " + MAX_TEAM_SIZE + ".");
        return null;
    }

    private void changeRefreshRate() {
        // Aplica la nueva frecuencia de actualización elegida por el usuario
        int refreshRate = (Integer) view.scoreboardPanel.spnRefreshRate.getValue();
        view.refreshRate = refreshRate;
        refreshTimer.setDelay(refreshRate);
    }

    private void stopCurrentGame() {
        // Si quedaba una partida anterior en curso, le pide a sus hilos que terminen
        refreshTimer.stop();
        if (gameModel != null) {
            gameModel.gameActive.set(false);
        }
    }
}
