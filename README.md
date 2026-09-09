# Mutant Battle — Design Specification

**Course project:** Case #1 – Mutant Battle (25%)
**Status:** Draft for professor review — no code has been written yet.
**Repository location:** `/docs/SPEC.md` (root-level `docs` folder, versioned alongside the source once approved)

---

## 1. Overview

Mutant Battle is a self-running simulation. The user only supplies the team size (3–11, equal for both teams). From that point on the system generates two teams of random mutants and runs the battle to completion without user input, animating it live in a Swing UI.

The system is split into four layers with strict separation of concerns:

| Layer | Responsibility |
|---|---|
| Model | Pure data + behavior of game entities (Mutant, MutantPower, Team) |
| Game | The battlefield: team lifecycle, scoreboard, win condition, dimensions |
| Control | Movement, encounter detection, attack/defend resolution, threading |
| UI | Swing rendering only, via Observer + MVC, no game logic |

### 1.1 Design Patterns Used

- **Observer** — `Battlefield` (subject) notifies `BattleObserver`s (the UI) on every state change (movement, attack, death, match end).
- **MVC** — Model = `Model` package (Mutant/Team/Battlefield state); View = `BattlefieldPanel`/`ScoreboardPanel`; Controller = `GameController` (owns start/reset, wires Model to View, never draws or computes damage).
- **Strategy** — `MovementStrategy` (per mutant-archetype movement pattern) and `CombatDecisionStrategy` (attack-or-defend decision).
- **Factory Method** — `MutantFactory` / `PowerFactory` build random mutants and powers so team-generation code has no `new SomeConcreteMutant()` scattered around.
- **Template Method** — `Mutant.act()` defines the fixed skeleton (move → detect radius → decide → resolve), subclasses only override the pieces that vary.
- **Singleton** — `GameConstants` (or a constants holder loaded once) and `ExecutorServiceProvider`.

No literal numbers/strings are hard-coded in logic classes; everything routes through `GameConstants`.

---

## 2. Constants (`GameConstants`)

| Constant | Description | Example default |
|---|---|---|
| `MIN_TEAM_SIZE` | Minimum mutants per team | 3 |
| `MAX_TEAM_SIZE` | Maximum mutants per team | 11 |
| `INITIAL_ENERGY` | Starting energy for every mutant | 100 |
| `MIN_DEFENSE`, `MAX_DEFENSE` | Defense capacity range | 1, 3 |
| `MIN_DAMAGE`, `MAX_DAMAGE` | Initial power damage range | 1, 3 |
| `MAX_POWER_LEVEL` | Cap on power growth | 7 |
| `POWER_GROWTH_STEP` | Increment per successful hit | 1 |
| `ENCOUNTER_RADIUS` | Distance that triggers a decision | configurable, e.g. 40px |
| `MIN_SPEED`, `MAX_SPEED` | Mutant movement speed bounds | 2, 6 |
| `BATTLEFIELD_WIDTH`, `BATTLEFIELD_HEIGHT` | Arena dimensions | 1000, 700 |
| `REFRESH_RATE_MS` | UI redraw interval | 33 (~30 FPS) |
| `THREAD_POOL_SIZE` | Worker threads for mutant agents | `Runtime.getRuntime().availableProcessors()` |

All are `public static final` in a non-instantiable class (or an `enum` singleton), loaded from a single source so no other class embeds numbers/strings directly.

---

## 3. Model Layer

### 3.1 `Position` / `Vector2D`
Immutable-ish value object: `x`, `y`, plus `heading` (angle) used by movement. Utility methods: `distanceTo(Position)`, `translate(dx, dy)`, `clamp(width, height)`.

### 3.2 `MutantPower` (abstract)
```
abstract class MutantPower {
    protected int damageCapacity;      // 1..3 initially, grows to MAX_POWER_LEVEL
    String name;

    abstract int computeDamage();      // polymorphic: some powers may add effects
    void grow() { damageCapacity = min(damageCapacity + POWER_GROWTH_STEP, MAX_POWER_LEVEL); }
}
```
Concrete subclasses (polymorphism, each overrides `computeDamage()` / adds flavor, e.g. a chance of a secondary effect): `KineticBlastPower`, `PyrokinesisPower`, `TelepathicStrikePower`, `RegenerationAssistPower` (support-type modifier), etc. `PowerFactory` picks one at random per mutant and assigns a random `damageCapacity` in `[MIN_DAMAGE, MAX_DAMAGE]`.

A mutant may carry **at most one** power (nullable-safe: a `NullPower`/`NoPower` object, Null Object pattern, avoids null checks scattered in Control layer).

### 3.3 `Mutant` (abstract — extends the Week #5 model)
```
abstract class Mutant {
    String id, name;
    Team team;
    Position position;
    int speed;
    int defenseCapacity;      // 1..3
    AtomicInteger energy;     // starts at INITIAL_ENERGY, thread-safe mutation
    MutantPower power;
    volatile boolean alive;

    // Template method – fixed skeleton, steps overridden by subclasses
    final void act(Battlefield field) {
        move(field);
        // encounter detection delegated to Control layer's MutantAgent
    }

    abstract void move(Battlefield field);        // movement pattern hook
    abstract CombatAction decide(Mutant opponent); // ATTACK or DEFEND hook

    void applyDamage(double amount) { ... }        // decrements energy, may kill
    void rewardSuccessfulHit() { power.grow(); }
}
```
Concrete subclasses demonstrate inheritance/polymorphism by archetype, each supplying its own `move()` pattern and `decide()` bias, e.g.:
- `BruteMutant` — slow, biased to attack, moves in straight bursts.
- `SpeedsterMutant` — fast, erratic zig-zag movement, biased to dodge/defend.
- `TelepathMutant` — moderate speed, circular/orbit movement pattern, balanced decision logic.

`MutantFactory` (Factory Method) builds a random archetype + random stats + one random power for each team slot.

### 3.4 `Team`
```
class Team {
    String name;
    Color color;
    Shield symbol;                 // enum or small class: shape + icon
    List<Mutant> roster;           // fixed size (3..11)
    int aliveCount(); int deadCount();
}
```

---

## 4. Game Layer

### 4.1 `Battlefield` (the Subject)
- Holds `Team teamA`, `Team teamB`, dimensions (`width`, `height`), the running `Scoreboard`.
- `getDimensions()` — exposed so any `Mutant`/`MutantAgent` can bound its movement.
- Tracks alive/dead counts per team continuously (delegates to `Team`, aggregates for the scoreboard).
- `isMatchOver()` → true when one team's `aliveCount() == 0`.
- `getWinner()` → the surviving `Team` (or `null`/draw handling if simultaneous).
- Implements the **Observer subject** side: `registerObserver(BattleObserver)`, `notifyObservers(BattleEvent)`. Every mutation (move tick, attack resolved, death, match end) fires an event; it never talks to Swing directly.

### 4.2 `Scoreboard`
Simple aggregator: alive/dead per team, elapsed ticks, event log (optional), consumed by UI through the observer callback, never computed by the UI.

### 4.3 `BattleEvent`
Immutable event object (`MOVED`, `ATTACK_RESOLVED`, `MUTANT_DIED`, `MATCH_OVER`, …) carrying just enough data for the UI to redraw without recomputation.

---

## 5. Control Layer

### 5.1 Movement
`MovementStrategy` interface, one implementation per archetype (Strategy pattern), called from `Mutant.move()`. Movement is randomized but pattern-based (e.g., bounded random-walk with momentum/heading persistence rather than a fresh random direction every tick), and always clamped to the `Battlefield` dimensions.

### 5.2 Encounter detection & resolution
- `MutantAgent implements Runnable` — one agent wraps one `Mutant`. Each tick it: moves the mutant, then scans the battlefield for opponents within `ENCOUNTER_RADIUS`.
- For every opponent found within radius, exactly **one** attack-or-defend exchange is generated per unordered pair per tick (a pair is de-duplicated using a canonical key, e.g. `min(idA,idB)+"-"+max(idA,idB)`, tracked in a `ConcurrentHashMap<String, Boolean>` cleared each tick) — this satisfies "several opponents in radius → one action per pair."
- `CombatDecisionStrategy` (Strategy) decides ATTACK vs DEFEND per mutant per encounter, based on its archetype bias + current energy (e.g., low energy → more likely to defend).
- `CombatResolver` applies the rules:
  - Attacker attacks, defender does **not** defend → `damage = power.computeDamage()`.
  - Attacker attacks, defender **does** defend → `damage = power.computeDamage() / defender.defenseCapacity`.
  - On any successful damage, the dealer's `power.grow()` fires (capped at `MAX_POWER_LEVEL`).
  - Damage application decrements `energy` via `AtomicInteger`; if `energy <= 0` → `alive = false`, `Team` count updated, `Battlefield` notified (`MUTANT_DIED`).

### 5.3 Threading scheme
- A single `ExecutorService` (fixed thread pool sized via `GameConstants.THREAD_POOL_SIZE`) is shared by both teams — **not** one raw `Thread` per mutant, to keep resource usage bounded regardless of team size (3–11 per side).
- Each simulation tick, all `MutantAgent` tasks are submitted to the pool and the tick advances once all futures for that tick complete (barrier via `CompletableFuture.allOf` or a `CyclicBarrier`), so movement/combat stay logically synchronized per frame while executing in parallel.
- Shared mutable state (`energy`, alive flags, pair-dedup map, team rosters) uses thread-safe types (`AtomicInteger`, `volatile boolean`, `ConcurrentHashMap`, `CopyOnWriteArrayList` for rosters read by the UI thread) to avoid explicit broad locks; where an exchange must be atomic (e.g., resolving one pair's attack/defend and applying damage), a lock is scoped to that pair only (`synchronized` on the canonical pair key or a `ReentrantLock` obtained from a per-pair lock registry) — never a single global lock, so unrelated encounters across the battlefield still run in parallel.
- `Battlefield.notifyObservers(...)` calls are marshalled onto the Swing Event Dispatch Thread via `SwingUtilities.invokeLater` so the Control layer never touches UI components directly and Swing thread-safety is respected.

---

## 6. UI Layer (Observer + MVC)

### 6.1 Roles
- **Model** — `Battlefield`, `Team`, `Mutant` (already defined above); the UI never mutates these, only reads.
- **View** — `BattleFrame` (JFrame), `BattlefieldPanel` (JPanel/Canvas that paints mutants at their current `Position`, colored/symboled by `Team`), `ScoreboardPanel` (alive/dead counts, energy bars per mutant, winner banner).
- **Controller** — `GameController`: reads the user's team-size input, invokes `MutantFactory`/`Battlefield` to build the match, starts the `ExecutorService` loop, implements `BattleObserver` and forwards received `BattleEvent`s to the View for repaint, and exposes `startNewMatch()` to reset everything after a win.

### 6.2 Observer wiring
```
interface BattleObserver { void onBattleEvent(BattleEvent e); }
```
`GameController` (or a thin `BattlefieldViewModel`) implements `BattleObserver`, registers itself on `Battlefield`, and on each event triggers `BattlefieldPanel.repaint()` / `ScoreboardPanel.refresh()`. A `javax.swing.Timer` at `GameConstants.REFRESH_RATE_MS` drives the actual repaint cadence so the canvas redraws smoothly even between discrete events.

### 6.3 Required displays
- All living mutants, positioned and moving simultaneously, colored/symboled by team.
- Per-mutant energy (e.g., a small bar or numeric label).
- Alive/dead counts per team (scoreboard).
- End-of-match winner announcement (from `BattleEvent.MATCH_OVER`).
- A "New Match" control that calls `GameController.startNewMatch()`, which tears down the executor, rebuilds two fresh teams at the same size, and re-registers observers.

---

## 7. Sequence — one simulation tick

1. `GameController` submits all `MutantAgent`s to the `ExecutorService`.
2. Each `MutantAgent`: `mutant.move(battlefield)` → position updated.
3. Each `MutantAgent` scans battlefield for opponents within `ENCOUNTER_RADIUS`.
4. For each new (undeduplicated) pair this tick: both mutants call `decide()`; `CombatResolver` resolves attack/defend, applies damage, grows power, updates energy.
5. Any death triggers `Team` count update + `Battlefield.notifyObservers(MUTANT_DIED)`.
6. Once all agents finish the tick (barrier), `Battlefield` checks `isMatchOver()`; if true, notifies `MATCH_OVER` with the winning `Team` and stops submitting further ticks.
7. UI (EDT) repaints from the latest Model state on every notified event and on the `Timer` cadence.

---

## 8. Class Diagram (high level)

```mermaid
classDiagram
    class Mutant {
        <<abstract>>
        +Position position
        +AtomicInteger energy
        +int defenseCapacity
        +MutantPower power
        +move(Battlefield)*
        +decide(Mutant)*
    }
    Mutant <|-- BruteMutant
    Mutant <|-- SpeedsterMutant
    Mutant <|-- TelepathMutant

    class MutantPower {
        <<abstract>>
        +int damageCapacity
        +computeDamage()*
        +grow()
    }
    MutantPower <|-- KineticBlastPower
    MutantPower <|-- PyrokinesisPower
    MutantPower <|-- NullPower

    Mutant "1" --> "0..1" MutantPower

    class Team {
        +Color color
        +Shield symbol
        +List~Mutant~ roster
    }
    Team "1" o-- "3..11" Mutant

    class Battlefield {
        +Team teamA
        +Team teamB
        +getDimensions()
        +isMatchOver()
        +notifyObservers(BattleEvent)
    }
    Battlefield "1" o-- "2" Team
    Battlefield ..> BattleObserver : notifies

    class MutantAgent {
        +run()
    }
    MutantAgent --> Mutant
    MutantAgent --> CombatResolver

    class GameController {
        +startNewMatch()
        +onBattleEvent(BattleEvent)
    }
    GameController ..|> BattleObserver
    GameController --> Battlefield
    GameController --> BattlefieldPanel
```

---

## 9. Repository Layout (proposed)

```
mutant-battle/
├── docs/
│   └── SPEC.md              <- this document
├── src/main/java/mutantbattle/
│   ├── model/                (Mutant, MutantPower, Team, Position, ...)
│   ├── game/                 (Battlefield, Scoreboard, BattleEvent)
│   ├── control/              (MutantAgent, MovementStrategy, CombatDecisionStrategy, CombatResolver)
│   ├── ui/                   (BattleFrame, BattlefieldPanel, ScoreboardPanel, GameController, BattleObserver)
│   └── util/                 (GameConstants, MutantFactory, PowerFactory)
└── README.md
```

---

## 10. Open Points for Review

- Exact list of concrete mutant archetypes and power types to implement (kept extensible via the abstract base + factory).
- Whether pair de-duplication window is per-tick only or debounced over N ticks to avoid attack spam at close range.
- Tie-break behavior if both teams reach zero alive mutants on the same tick.
