package Game;

import Model.IConstants;
import Model.Mutant;

public class Team implements IConstants {

    public double name; // se asignan dos equipos cada uno con 1 o 2
    public double mutantCount; // Cantidad asiganda por el usuario máximo 11 y minimo 3
    public double aliveMutants; // cantidad de mutantes vivos en el equipo
    public Mutant[] mutants; // los mutantes que hay en el equipo

    public Team(double name, double mutantCount) {
        this.name = name;
        this.mutantCount = mutantCount;
        this.aliveMutants = mutantCount;

        this.mutants = new Mutant[(int) mutantCount];
        for (int i = 0; i < mutants.length; i++) {
            mutants[i] = new Mutant();
            mutants[i].team = name;
        }
    }
}
