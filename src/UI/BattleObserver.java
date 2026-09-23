package UI;

import Game.Battlefield;

public interface BattleObserver {

    // Se llama cada vez que el sujeto observado (UIController) notifica un cambio en el campo de batalla.
    // El observador solo consulta el Battlefield para dibujarlo, nunca modifica su estado.
    void updateScreen(Battlefield battlefield);
}
