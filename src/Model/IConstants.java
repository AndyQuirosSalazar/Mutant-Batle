package Model;

public interface IConstants {

    int WAIT_TIME = 3000; // constante de tiempo de espera
    String DIE_MESSAGE = "Ha muerto"; // constante del mensaje de muerte
    String VICTORY_MESSAGE = "Ha ganado"; // constante del mensaje de victoria
    int INITIAL_ENERGY = 100; // constante de la energía inicial
    int MAX_DAMAGE = 7; // constante del daño maximo
    int MIN_DAMAGE = 1; // constante del daño minimo
    int ATTACK_RADIUS = 0; // TODO: por definir - constante de ataque de los mutantes
    double VELOCITY_XY = 0.0; // TODO: por definir - velocidad a la que se moverán los mutantes
    int WIDTH = 0; // constante de ancho del campo de batalla
    int HEIGHT = 0; // constante de altura del campo de batalla
    int MAX_MUTANTS = 11; // cantidad maxima de mutantes por equipo
    int MIN_MUTANTS = 3; // cantidad minima de mutantes por equipo
}
