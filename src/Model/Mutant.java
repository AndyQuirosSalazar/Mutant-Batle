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
    private double heading; // dirección actual del movimiento, en radianes (persiste entre pasos)

    public Mutant(double team) {
        this.team = team;
        this.isAlive = true;
        this.energy = INITIAL_ENERGY;
        generateId();
        this.defense = assignDefense();
        this.power = getPower();
        this.x = RANDOM.nextInt(Math.max(WIDTH, 1));
        this.y = RANDOM.nextInt(Math.max(HEIGHT, 1));
        this.heading = RANDOM.nextDouble() * 2 * Math.PI; // rumbo inicial aleatorio
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
        // Gira un poco la dirección actual en vez de elegir una nueva de cero -
        // esto hace que el camino se vea como una curva suave en vez de zigzag.
        heading += (RANDOM.nextDouble() * 2 - 1) * 0.4; // giro máximo ~0.4 radianes por paso

        int proposedX = x + (int) Math.round(Math.cos(heading) * VELOCITY_XY);
        int proposedY = y + (int) Math.round(Math.sin(heading) * VELOCITY_XY);

        // Si se sale del campo, "rebota": invierte la componente del rumbo que corresponda
        if (proposedX < 0 || proposedX > WIDTH) {
            heading = Math.PI - heading; // rebote horizontal
        }
        if (proposedY < 0 || proposedY > HEIGHT) {
            heading = -heading; // rebote vertical
        }

        int newX = (int) Math.round(Math.cos(heading) * VELOCITY_XY);
        int newY = (int) Math.round(Math.sin(heading) * VELOCITY_XY);

        x = limit(x + newX, 0, WIDTH);
        y = limit(y + newY, 0, HEIGHT);
    }

    public boolean decideNow() {
        // Decide aleatoriamente entre atacar (true) y defender (false)
        boolean desicion = RANDOM.nextBoolean();
        this.decide = desicion;
        return desicion;
    
    }

    // Se llama en la fase DECIDE de cada ronda. Si el mutante ya decidió
    // en esta ronda, no vuelve a tirar el random - solo devuelve lo que ya tenía.
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

    private int limit(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    // ---- Métodos públicos, necesarios para que otras clases operen sobre el mutante ----

    public int getAttackDamage() {
        return power.getAttackDamage();
    }

    // MutantController registre la baja una única vez.
    public synchronized boolean receiveDamage(int Damage) {
        if (!isAlive) {
            return false;
        }

        if (this.decide) {
            int effectiveDamage = Math.max(0, Damage);
            energy -= effectiveDamage;
        } else {
            int effectiveDamage = Math.max(0, Damage - defense);
            energy -= effectiveDamage;
        }

        if (energy <= 0) {
            energy = 0;
            isAlive = false;
            return true;
        }
        return false;
    }

    public void onBattleWon() {
        power.increaseDamage();
    }

}
