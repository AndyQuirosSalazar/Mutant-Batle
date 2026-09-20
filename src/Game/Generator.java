package Game;

import Model.Mutant;

public class Generator {

    public Generator() {
    }

    // static: no depende de ningún dato propio de una instancia de Generator,
    // así que Team puede llamarlo directo como Generator.generateMutant(name)
    // sin necesitar crear un new Generator() primero.
    public static Mutant generateMutant(double team) {
        // genera un mutante con todas sus características
        // (id, defensa, poder y posición se generan dentro del constructor de Mutant)
        return new Mutant(team);
    }

    public Team generateTeam(double name, double mutantCount) {
        // genera un equipo con todas sus caracteristicas
        // usando Generator.generateMutant() para cada uno)
        return new Team(name, mutantCount);
    }
}
