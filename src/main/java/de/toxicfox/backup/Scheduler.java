package de.toxicfox.backup;

import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    private static final Timer timer = new Timer();

    public static void every(long hours, Runnable runnable) {
        final var running = new Object() {
            boolean value = false;
        };

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (running.value) {
                    System.out.println("Already running...");
                    return;
                }

                running.value = true;
                runnable.run();
                running.value = false;
            }
        };

        timer.scheduleAtFixedRate(task, 0, hours * 60 * 60 * 1000);
    }
}
