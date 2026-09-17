package Control;

import Game.Battlefield;
import Model.Mutant;

public class MutantController {

    public Battlefield battlefield; // Se inicia un objeto de campoDeBatalla

    public MutantController() {
    }

    private void moveRandomly() {
        // se aplica en el método de cada mutante
    }

    private Mutant detectEnemyInRadius() {
        // se aplica el método de cada mutante
        return null;
    }

    private void decideAction(Mutant enemy) {
        // Decide atacar o defender y aplica las matemáticas
    }

    private boolean checkGameOver() {
        // Verifica la cantidad de mutantes en cada equipo para saber si alguien gano
        return false;
    }

    private void run() {
        // Bucle del hilo mientras el mutante esté vivo y el juego activo
    }
}
