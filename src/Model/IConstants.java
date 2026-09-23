package Model;

public interface IConstants {

    int WAIT_TIME = 60; // constante de tiempo de espera entre rondas (ms)
    String DIE_MESSAGE = "Ha muerto"; // constante del mensaje de muerte
    String VICTORY_MESSAGE = "Ha ganado"; // constante del mensaje de victoria
    int INITIAL_ENERGY = 100; // constante de la energía inicial
    int MAX_DAMAGE = 7; // constante del daño maximo
    int MIN_DAMAGE = 1; // constante del daño minimo
    int ATTACK_RADIUS = 80; // constante de ataque de los mutantes (radio del radar en pixeles)
    double VELOCITY_XY = 6.0; // velocidad a la que se moverán los mutantes (paso máximo por eje)
    int WIDTH = 800; // constante de ancho del campo de batalla
    int HEIGHT = 600; // constante de altura del campo de batalla
    int MAX_MUTANTS = 11; // cantidad maxima de mutantes por equipo
    int MIN_MUTANTS = 3; // cantidad minima de mutantes por equipo
}
