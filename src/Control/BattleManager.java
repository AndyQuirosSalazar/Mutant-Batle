package Control;

import java.util.ArrayList;
import java.util.List;

public class BattleManager {

    public List<Thread> mutantThreads;

    public BattleManager() {
        this.mutantThreads = new ArrayList<>();
    }

    public void startThreads() {
        // Inicia un hilo por cada mutante
        for (Thread thread : mutantThreads) {
            thread.start();
        }
    }
}
