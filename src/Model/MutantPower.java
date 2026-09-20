package Model;

import java.util.Random;

public class MutantPower implements IConstants {

    private static final Random RANDOM = new Random();

    public double powerId; // nombre de cada poder
    public int attackDamage; // aleatorio entre 1 y 3 con un máximo de 7

    public MutantPower() {
        generateId();
        this.attackDamage = assignDamage();
    }

    public int assignDamage() {
        // se asigna un daño aleatorio entre 1 y 3
        return MIN_DAMAGE + RANDOM.nextInt(3); // 1, 2 o 3
    }

    public void increaseDamage() {
        // Se aumenta el daño en 1 cuando gana una batalla, sin pasar el máximo
        if (attackDamage < MAX_DAMAGE) {
            attackDamage++;
        }
    }

    public void generateId() {
        // genera un Id aleatorio para cada poder
        this.powerId = RANDOM.nextInt(1_000_000);
    }

    // Getters públicos para que otras clases (como Mutant) puedan usarlos
    public int getAttackDamage() {
        return attackDamage;
    }

    public double getPowerId() {
        return powerId;
    }

}
