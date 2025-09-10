package de.toxicfox.backup;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    private static final Timer timer = new Timer();

    public static void every(int beginHour, int beginMinute, long hours, Runnable runnable) {
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

        timer.scheduleAtFixedRate(task, calculateStart(beginHour, beginMinute), hours * 60 * 60 * 1000);
    }

    private static Date calculateStart(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        
        Date firstRun = cal.getTime();
        if (firstRun.before(new Date())) {
            cal.add(Calendar.DATE, 1);
            firstRun = cal.getTime();
        }

        return firstRun;
    }
}
