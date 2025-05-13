package de.glowman554.backup;

import de.glowman554.backup.cli.CliUserInterface;
import de.glowman554.backup.core.Backup;
import de.glowman554.backup.core.IUserInterface;
import de.glowman554.backup.tui.TuiUserInterface;

import java.io.IOException;

public class Launcher {


    public static void main(String[] args) throws IOException {
        ArgumentParser parser = new ArgumentParser(args, new String[]{"--interface", "--scheduled", "--restore", "--compression"});

        String interfaceType = parser.consumeOption("--interface", "tui");
        IUserInterface user = switch (interfaceType) {
            case "tui" -> new TuiUserInterface();
            case "cli" -> new CliUserInterface();
            default -> throw new IllegalArgumentException("Invalid interface type");
        };

        boolean compression = parser.isOption("--compression");

        if (parser.isOption("--restore")) {
            Backup.restore(user, compression);
        } else {
            if (parser.isOption("--scheduled")) {
                System.out.println("Scheduled backup started");
                Scheduler.every(24, () -> {
                    try {
                        Backup.backup(user, compression);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                Backup.backup(user, compression);
            }
        }
    }

}
