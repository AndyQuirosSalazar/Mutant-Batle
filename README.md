#  Mutant Battle - Caso 1

**Integrantes:**
* Andy Quiros Salazar
* Jordan Seas Alvarez

---

##  Specs

###  Package: `Model`

#### `public Interface IConstants`
* `+ final int WAIT_TIME = 3000` // constante de tiempo de espera
* `+ final int INITIAL_ENERGY = 100` // constante de la energía inicial
* `+ final int MAX_DAMAGE = 7` // constante del daño maximo
* `+ final int MIN_DAMAGE = 1` // constante del daño minimo
* `+ final int ATTACK_RADIUS = por definir` // constante de ataque de los mutantes
* `+ final double VELOCITY_XY = por definir` // constante velocidad a la que se moverán los mutantes
* `+ final int WIDTH` // constante de ancho del campo de batalla
* `+ final int HEIGHT` // constante de altura del campo de batalla

#### `class Mutant`
* `+ double id` // Nombre de cada mutante
* `+ double team` // Se le asigna un equipo
* `+ int energy = INITIAL_ENERGY` // Energia principal del mutante inicialmente con 100
* `+ int x` // Coordenada horizontal del mutante
* `+ int y` // Coordenada vertical del mutante
* `+ bool isAlive` // variable para saber si esta vivo inicia en true
* `+ int defense` // defensa aleatoria entre 1 y 3
* `- MutantPower getPower()` // Se le asigna un poder aleatorio de podermutante
* `- int assignDefense()` // inicializa la defensa aleatoriamente entre 1 y 3
* `- void move()` // Se mueve aleatoriamente
* `- void decide()` // Decide aleatoriamente entre atacar y defender
* `- void scanRadar()` // Detecta si hay un mutante de otro equipo en su radar
* `- void generateId()` // genera un id aleatorio para el mutante

#### `class MutantPower`
* `+ int attackDamage` // aleatorio entre 1 y 3 con un máximo de 7
* `- int assignDamage()` // se asigna un daño aleatorio entre 1 y 3
* `- void increaseDamage()` // Se aumenta el daño en 1 cuando gana una batalla

---

###  Package: `Game`

#### `class Battlefield`
* `+ Team team1` // objeto de la clase equipo
* `+ Team team2` // objeto de la clase equipo
* `+ Scoreboard scoreboard` // Guarda la cantidad de mutantes de cada equipo
* `- void startBattle()` // para inicializar la batalla

#### `class Team`
* `+ double name` // se asignan dos equipos cada uno con 1 o 2
* `+ double mutantCount` // Cantidad asiganda por el usuario máximo 11 y minimo 3
* `+ array mutants` // los mutantes que hay en el equipo

#### `class Scoreboard`
* `+ int aliveTeam1` // guarda los mutantes vivos del equipo 1
* `+ int deadTeam1` // guarda los mutantes muertos del equipo 1
* `+ int aliveTeam2` // guarda los mutantes vivos del equipo 2
* `+ int deadTeam2` // guarda los mutantes muertos del equipo 2
* `- void registerCasualty()` // Registra la baja de un equipo

#### `class Generator`
* `- void generateMutant()` // genera un mutante con todas sus características
* `- void generateTeam()` // genera un equipo con todas sus caracteristicas

---

###  Package: `Control`

#### `class MutantController`
* `+ Battlefield battlefield` // Se inicia un objeto de campoDeBatalla
* `- void moveRandomly()` // se aplica en el método de cada mutante
* `- Mutant detectEnemyInRadius()` // se aplica el método de cada mutante
* `- void decideAction(Mutant enemy)` // Decide atacar o defender y aplica las matemáticas
* `- boolean checkGameOver()` // Verifica la cantidad de mutantes en cada equipo para saber si alguien gano
* `- void run()` // Bucle del hilo mientras el mutante esté vivo y el juego activo

#### `class BattleManager`
* `+ List<Thread> mutantThreads`
* `+ void startThreads()` // Inicia un hilo por cada mutante

---

###  Package: `UI`

#### `interface BattleObserver`
* `+ void updateScreen()` // Se llama cada vez que el Observable (CampoDeBatalla) notifica un cambio

#### `class MainWindow`
* `+ BattlePanel battlePanel` // Objeto tipo canvas o JPanel donde se dibujan y mueven los mutantes
* `+ ScoreboardPanel scoreboardPanel` // Objeto para mostrar estadísticas y controles
* `+ int refreshRate` // Tasa de refresco configurable para el renderizado en tiempo real
* `- void configureWindow()` // Configura el tamaño del JFrame y acomoda los paneles
* `- void showWinnerMessage()` // Muestra un aviso cuando el juego detecta un equipo ganador
* `- void updateScreen()` // Sobrescribe el método de la interfaz para invocar el repintado de los gráficos

#### `class BattlePanel`
* `- void paintComponent()` // Método nativo de Java donde se programa el renderizado gráfico
* `- void drawBattlefield()` // Renderiza el fondo usando las dimensiones del CampoDeBatalla
* `- void drawMutant()` // Dibuja la forma, el color del equipo, el símbolo, la barra de energía y el daño/defensa (DMG/DEF) de cada mutante en las coordenadas x, y actuales

#### `class ScoreboardPanel`
* `+ JButton btnNewGame` // Botón para iniciar un juego nuevo sin cerrar la aplicación
* `+ JTextField txtTeamSize` // Campo de texto para que el usuario ingrese la cantidad de mutantes (3 a 11)
* `- void updateCount(Scoreboard currentScoreboard)` // Actualiza las etiquetas de vivos y muertos por equipo
* `- void drawPlayersEnergy(List<Mutant> mutants)` // Lista visual con la energía, el daño y la defensa restantes de cada mutante

#### `class UIController`
* `+ Battlefield gameModel` // Referencia de solo lectura a la capa Game
* `+ MainWindow view` // Referencia a la interfaz gráfica
* `- void startGame()` // Captura el clic del botón, valida el número y delega el inicio a la capa Game
* `- void connectObserver()` // Suscribe la VentanaPrincipal a la lista de observadores del CampoDeBatalla para mantenerlos sincronizados

@startuml
package Model {
    interface IConstants {
        + WAIT_TIME: int = 3000
        + INITIAL_ENERGY: int = 100
        + MAX_DAMAGE: int = 7
        + MIN_DAMAGE: int = 1
        + ATTACK_RADIUS: int
        + VELOCITY_XY: double
        + WIDTH: int
        + HEIGHT: int
    }

    class Mutant {
        + id: double
        + team: double
        + energy: int = INITIAL_ENERGY
        + x: int
        + y: int
        + isAlive: bool
        + defense: int
        - getPower(): MutantPower
        - assignDefense(): int
        - move(): void
        - decide(): void
        - scanRadar(): void
        - generateId(): void
    }

    class MutantPower {
        + attackDamage: int
        - assignDamage(): int
        - increaseDamage(): void
    }
}

package Game {
    class Battlefield {
        + team1: Team
        + team2: Team
        + scoreboard: Scoreboard
        - startBattle(): void
    }

    class Team {
        + name: double
        + mutantCount: double
        + mutants: array
    }

    class Scoreboard {
        + aliveTeam1: int
        + deadTeam1: int
        + aliveTeam2: int
        + deadTeam2: int
        - registerCasualty(): void
    }

    class Generator {
        - generateMutant(): void
        - generateTeam(): void
    }
}

package Control {
    class MutantController {
        + battlefield: Battlefield
        - moveRandomly(): void
        - detectEnemyInRadius(): Mutant
        - decideAction(enemy: Mutant): void
        - checkGameOver(): boolean
        - run(): void
    }

    class BattleManager {
        + mutantThreads: array<Thread>
        + startThreads(): void
    }
}

package UI {
    interface BattleObserver {
        + updateScreen(): void
    }

    class MainWindow {
        + battlePanel: BattlePanel
        + scoreboardPanel: ScoreboardPanel
        + refreshRate: int
        - configureWindow(): void
        - showWinnerMessage(): void
        - updateScreen(): void
    }

    class BattlePanel {
        - paintComponent(): void
        - drawBattlefield(): void
        - drawMutant(): void
    }

    class ScoreboardPanel {
        + btnNewGame: JButton
        + txtTeamSize: JTextField
        - updateCount(currentScoreboard: Scoreboard): void
        - drawPlayersEnergy(mutants: array<Mutant>): void
    }

    class UIController {
        + gameModel: Battlefield
        + view: MainWindow
        - startGame(): void
        - connectObserver(): void
    }

    note right of BattlePanel
        drawMutant() ahora también dibuja
        el daño (DMG) y la defensa (DEF)
        de cada mutante bajo su ícono.
    end note

    note right of ScoreboardPanel
        drawPlayersEnergy() ahora también
        muestra el daño y la defensa de
        cada mutante junto a su energía.
    end note
}

' Relaciones estructurales basadas en los atributos
Mutant ..> MutantPower : usa
Battlefield --> Team : contiene
Battlefield --> Scoreboard : gestiona
MutantController --> Battlefield : observa
MainWindow ..|> BattleObserver : implementa
MainWindow --> BattlePanel : contiene
MainWindow --> ScoreboardPanel : contiene
UIController --> Battlefield : gameModel
UIController --> MainWindow : view
@enduml
