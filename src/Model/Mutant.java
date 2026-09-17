package Model;

public class Mutant implements IConstants {

    public double id; // Nombre de cada mutante
    public double team; // Se le asigna un equipo
    public int energy = INITIAL_ENERGY; // Energia principal del mutante inicialmente con 100
    public int x; // Coordenada horizontal del mutante
    public int y; // Coordenada vertical del mutante
    public boolean isAlive; // variable para saber si esta vivo inicia en true
    public int defense; // defensa aleatoria entre 1 y 3

    public Mutant() {
    }

    private MutantPower getPower() {
        // Se le asigna un poder aleatorio de podermutante
        return null;
    }

    private int assignDefense() {
        // inicializa la defensa aleatoriamente entre 1 y 3
        return 0;
    }

    private void move() {
        // Se mueve aleatoriamente
    }

    private void decide() {
        // Decide aleatoriamente entre atacar y defender
    }

    private void scanRadar() {
        // Detecta si hay un mutante de otro equipo en su radar
    }

    private void generateId() {
        // genera un id aleatorio para el mutante
    }
}
