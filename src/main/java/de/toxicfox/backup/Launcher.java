package de.toxicfox.backup;

import de.toxicfox.backup.cli.CliUserInterface;
import de.toxicfox.backup.core.Backup;
import de.toxicfox.backup.core.IUserInterface;
import de.toxicfox.backup.core.util.FileModificationCompression;
import de.toxicfox.backup.core.util.FileModificationEncryption;
import de.toxicfox.backup.core.util.FileUtil;
import de.toxicfox.backup.tui.TuiUserInterface;

import java.io.File;
import java.io.IOException;

public class Launcher {

    public static void main(String[] args) throws Exception {
        ArgumentParser parser = new ArgumentParser(args, new String[]{"--interface", "--scheduled", "--restore", "--compression", "--encryption"});

        String interfaceType = parser.consumeOption("--interface", "tui");
        IUserInterface user = switch (interfaceType) {
            case "tui" -> new TuiUserInterface();
            case "cli" -> new CliUserInterface();
            default -> throw new IllegalArgumentException("Invalid interface type");
        };

        FileUtil fileUtil = new FileUtil();

        if (parser.isOption("--compression")) {
            fileUtil.addModification(new FileModificationCompression());
        }

        if (parser.isOption("--encryption")) {
            String keyFile = parser.consumeOption("--encryption", null);
            fileUtil.addModification(new FileModificationEncryption(new File(keyFile), user));
        }

        if (parser.isOption("--restore")) {
            Backup.restore(user, fileUtil);
        } else {
            if (parser.isOption("--scheduled")) {
                System.out.println("Scheduled backup started");
                Scheduler.every(24, () -> {
                    try {
                        Backup.backup(user, fileUtil);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                Backup.backup(user, fileUtil);
            }
        }
    }

}
