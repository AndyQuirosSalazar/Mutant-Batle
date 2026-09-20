package Game;

import Model.Mutant;

public class Generator {

    public Generator() {
    }

    private Mutant generateMutant(double team) {
        // genera un mutante con todas sus características
        // (id, defensa, poder y posición se generan dentro del constructor de Mutant)
        return new Mutant(team);
    }

    private Team generateTeam(double name, double mutantCount) {
        // genera un equipo con todas sus caracteristicas
        // (el array de mutantes se genera dentro del constructor de Team)
        return new Team(name, mutantCount);
    }
}
