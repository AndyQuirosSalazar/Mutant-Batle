package Control;

import Game.Battlefield;
import Model.IConstants;
import Model.Mutant;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

public class MutantController implements Runnable, IConstants {

    public Battlefield battlefield; // Se inicia un objeto de campoDeBatalla

    private final Mutant mutant;              // el mutante que controla este hilo
    private final CyclicBarrier barrier;       // compartida por TODOS los MutantController
    private final AtomicBoolean gameActive;    // compartida, indica si la partida sigue activa

    public MutantController(Mutant mutant, Battlefield battlefield,
                             CyclicBarrier barrier, AtomicBoolean gameActive) {
        this.mutant = mutant;
        this.battlefield = battlefield;
        this.barrier = barrier;
        this.gameActive = gameActive;
    }

    private void moveRandomly() {
        // se aplica en el método de cada mutante
        mutant.move();
    }

    private Mutant detectEnemyInRadius() {
        // se aplica el método de cada mutante
        List<Mutant> allMutants = new ArrayList<>();
        allMutants.addAll(List.of(battlefield.team1.mutants));
        allMutants.addAll(List.of(battlefield.team2.mutants));
        return mutant.scanRadar(allMutants);
    }

    private void decideAction(Mutant enemy) {
        // Decide atacar o defender y aplica las matemáticas
        mutant.ensureDecision();

        if (enemy != null && mutant.decide) {
            boolean killedNow = enemy.receiveDamage(mutant.getAttackDamage());

            if (killedNow) {
                battlefield.scoreboard.registerDeadMutant(enemy);
                mutant.onBattleWon();
            }
        }
    }

    private boolean checkGameOver() {
        // Verifica la cantidad de mutantes en cada equipo para saber si alguien gano
        return battlefield.scoreboard.aliveTeam1 == 0 || battlefield.scoreboard.aliveTeam2 == 0;
    }

    @Override
    public void run() {
        // Bucle del hilo mientras el juego siga activo. IMPORTANTE: no se corta
        // por mutant.isAlive, porque el CyclicBarrier tiene un número fijo de
        // hilos esperados en cada await() - si un hilo se sale antes que los
        // demás, el resto se queda esperando para siempre (deadlock).
        try {
            while (gameActive.get()) {

                // --- FASE 1: MOVE ---
                if (mutant.isAlive) {
                    moveRandomly();
                }
                barrier.await();

                // --- FASE 2: DECIDE ---
                if (mutant.isAlive) {
                    mutant.ensureDecision();
                }
                barrier.await();

                // --- FASE 3: ATTACK ---
                if (mutant.isAlive) {
                    Mutant enemy = detectEnemyInRadius();
                    decideAction(enemy);
                }
                barrier.await();

                // Alguien pudo ganar en esta ronda; el primero en notarlo avisa a todos
                if (checkGameOver()) {
                    gameActive.set(false);
                }

                mutant.resetTurn();
                barrier.await();

                Thread.sleep(WAIT_TIME);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (BrokenBarrierException e) {
            // Un hilo murió o fue interrumpido mientras los demás esperaban en la barrera
            gameActive.set(false);
        }
    }
}
