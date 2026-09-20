package Model;

import java.util.List;
import java.util.Random;

public class Mutant implements IConstants {

    private static final Random RANDOM = new Random();

    public double id; // Nombre de cada mutante
    public double team; // Se le asigna un equipo
    public int energy = INITIAL_ENERGY; // Energia principal del mutante inicialmente con 100
    public int x; // Coordenada horizontal del mutante
    public int y; // Coordenada vertical del mutante
    public boolean isAlive; // variable para saber si esta vivo inicia en true
    public int defense; // defensa aleatoria entre 1 y 3
    public boolean decide; // variables para saber si se esta defendiendo o atacando
    private boolean hasDecidedThisTurn; // controla que decideNow() solo se ejecute una vez por ronda
    private MutantPower power; // poder de cada mutante

    public Mutant(double team) {
        this.team = team;
        this.isAlive = true;
        this.energy = INITIAL_ENERGY;
        generateId();
        this.defense = assignDefense();
        this.power = getPower();
        this.x = RANDOM.nextInt(Math.max(WIDTH, 1));
        this.y = RANDOM.nextInt(Math.max(HEIGHT, 1));
    }

    public MutantPower getPower() {
        // Se le asigna un poder aleatorio de podermutante
        return new MutantPower();
    }

    public int assignDefense() {
        // inicializa la defensa aleatoriamente entre 1 y 3
        return MIN_DAMAGE + RANDOM.nextInt(3);
    }

    public void move() {
        // Se mueve aleatoriamente dentro de los límites del campo de batalla,
        // usando VELOCITY_XY como el paso máximo por eje.
        int newX = (int) Math.round((RANDOM.nextDouble() * 2 - 1) * VELOCITY_XY);
        int newY = (int) Math.round((RANDOM.nextDouble() * 2 - 1) * VELOCITY_XY);

        x = clamp(x + newX, 0, WIDTH);
        y = clamp(y + newY, 0, HEIGHT);
    }

    public boolean decideNow() {
        // Decide aleatoriamente entre atacar (true) y defender (false)
        boolean desicion = RANDOM.nextBoolean();
        this.decide = desicion;
        return desicion;
    
    }

    // Se llama en la fase de decidir de cada ronda. Si el mutante ya decidió

    public boolean ensureDecision() {
        if (!hasDecidedThisTurn) {
            decideNow();
            hasDecidedThisTurn = true;
        }
        return this.decide;
    }

    // Se llama al terminar cada ronda (fase ATTACK), para que en la
    // siguiente ronda el mutante pueda volver a decidir.
    public void resetTurn() {
        hasDecidedThisTurn = false;
    }

    public Mutant scanRadar(List<Mutant> allMutants) {
        // Detecta si hay un mutante vivo de otro equipo dentro de ATTACK_RADIUS
        for (Mutant other : allMutants) {
            if (other == this || !other.isAlive || other.team == this.team) {
                continue;
            }
            double distance = Math.hypot(this.x - other.x, this.y - other.y);
            if (distance <= ATTACK_RADIUS) {
                return other;
            }
        }
        return null;
    }

    public void generateId() {
        // genera un id aleatorio para el mutante
        this.id = RANDOM.nextInt(1_000_000);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    // ---- Métodos públicos, necesarios para que otras clases operen sobre el mutante ----

    public int getAttackDamage() {
        return power.getAttackDamage();
    }

    public void receiveDamage(int incomingDamage) {
        
        if (this.decide){
            int effectiveDamage = Math.max(0, incomingDamage);
            energy -= effectiveDamage;
            if (energy <= 0) {
                energy = 0;
                isAlive = false;
            }
        }
        
        else {
            int effectiveDamage = Math.max(0, incomingDamage - defense);
            energy -= effectiveDamage;
            if (energy <= 0) {
                energy = 0;
                isAlive = false;
            }
        }
    }

    public void onBattleWon() {
        power.increaseDamage();
    }

    // Orquesta un turno completo: moverse, escanear y decidir si ataca.

    public void takeTurn(List<Mutant> allMutants) {
        if (!isAlive) {
            return;
        }
        decideNow();
        move();
        Mutant enemy = scanRadar(allMutants);
        if (enemy != null && this.decide) {
            enemy.receiveDamage(this.getAttackDamage());
        }
    }

    @Override
    public String toString() {
        return "Mutant{" +
                "id=" + id +
                ", team=" + team +
                ", energy=" + energy +
                ", x=" + x +
                ", y=" + y +
                ", isAlive=" + isAlive +
                ", defense=" + defense +
                '}';
    }
}
