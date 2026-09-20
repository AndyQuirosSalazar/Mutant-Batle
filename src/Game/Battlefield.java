package Game;

import Control.BattleManager;
import Control.MutantController;
import Model.Mutant;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Battlefield {

    public Team team1; // objeto de la clase equipo
    public Team team2; // objeto de la clase equipo
    public Scoreboard scoreboard; // Guarda la cantidad de mutantes de cada equipo
    public BattleManager battleManager; // Administra los hilos de los mutantes
    public AtomicBoolean gameActive; // Compartida por todos los hilos, indica si la partida sigue en curso

    public Battlefield(Team team1, Team team2) {
        this.team1 = team1;
        this.team2 = team2;
        startBattle();
    }

    private void startBattle() {
        // Arma el scoreboard con los equipos ya generados (mutantes, posiciones y energia
        // se crean en el constructor de Team/Mutant), crea el estado compartido de la
        // partida (bandera de juego activo y barrera de sincronizacion) y arranca un
        // hilo MutantController por cada mutante, dejando el campo listo y en marcha.
        scoreboard = new Scoreboard(team1, team2);
        gameActive = new AtomicBoolean(true);

        List<Mutant> allMutants = getAllMutants();
        CyclicBarrier barrier = new CyclicBarrier(allMutants.size());

        battleManager = new BattleManager();
        for (Mutant mutant : allMutants) {
            battleManager.mutantThreads.add(new Thread(new MutantController(mutant, this, barrier, gameActive)));
        }

        battleManager.startThreads();
    }

    // Lista combinada de mutantes de ambos equipos, para que Control la use en scanRadar/takeTurn
    public List<Mutant> getAllMutants() {
        return Stream.concat(Arrays.stream(team1.mutants), Arrays.stream(team2.mutants))
                .collect(Collectors.toList());
    }
}
