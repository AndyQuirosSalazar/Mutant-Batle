package Game;

import Model.Mutant;

public class Team {

    public double name; // se asignan dos equipos cada uno con 1 o 2
    public double mutantCount; // Cantidad asiganda por el usuario máximo 11 y minimo 3
    public double aliveMutants; // cantidad de mutantes vivos en el equipo
    public Mutant[] mutants; // los mutantes que hay en el equipo

    public Team() {
    }
}
