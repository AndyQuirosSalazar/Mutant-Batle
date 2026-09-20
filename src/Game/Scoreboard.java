package Game;

import Model.Mutant;

public class Scoreboard {

    public Team team1; // equipo 1, contiene sus propios datos (name, mutantCount, mutants, etc.)
    public Team team2; // equipo 2, contiene sus propios datos (name, mutantCount, mutants, etc.)

    public int aliveTeam1; // guarda los mutantes vivos del equipo 1
    public int deadTeam1; // guarda los mutantes muertos del equipo 1
    public int aliveTeam2; // guarda los mutantes vivos del equipo 2
    public int deadTeam2; // guarda los mutantes muertos del equipo 2

    public Scoreboard(Team team1, Team team2) {
        this.team1 = team1;
        this.team2 = team2;
        // Al iniciar la partida todos los mutantes de cada equipo están vivos
        this.aliveTeam1 = (int) team1.mutantCount;
        this.deadTeam1 = 0;
        this.aliveTeam2 = (int) team2.mutantCount;
        this.deadTeam2 = 0;
    }

    // Se expone públicamente para que MutantController la llame cuando un mutante muere en batalla.
    // synchronized porque varios hilos de MutantController pueden llamarla al mismo tiempo
    // durante la fase de ataque (antes del barrier.await()), y aliveTeam1--/aliveTeam2-- no es atómico.
    public synchronized void registerDeadMutant(Mutant mutant) {
        // Registra la baja de un mutante en el equipo al que pertenece
        mutant.isAlive = false;

        if (mutant.team == team1.name) {
            aliveTeam1--;
            deadTeam1++;
            team1.aliveMutants = aliveTeam1;
        } else if (mutant.team == team2.name) {
            aliveTeam2--;
            deadTeam2++;
            team2.aliveMutants = aliveTeam2;
        }
    }

    // Recalcula los contadores desde cero revisando el estado real de cada mutante
    public void refreshCounts() {
        aliveTeam1 = countAlive(team1);
        deadTeam1 = team1.mutants.length - aliveTeam1;
        team1.aliveMutants = aliveTeam1;

        aliveTeam2 = countAlive(team2);
        deadTeam2 = team2.mutants.length - aliveTeam2;
        team2.aliveMutants = aliveTeam2;
    }

    private int countAlive(Team team) {
        int alive = 0;
        for (Mutant mutant : team.mutants) {
            if (mutant != null && mutant.isAlive) {
                alive++;
            }
        }
        return alive;
    }
}
