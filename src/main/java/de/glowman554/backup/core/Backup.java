package de.glowman554.backup.core;

import de.glowman554.backup.tui.TuiUserInterface;

import java.io.File;
import java.io.IOException;

public class Backup {
    private static Config loadConfig() {
        Config config = new Config();
        config.load();

        if (config.sourcePath.equals("<change me>") || config.targetPath.equals("<change me>")) {
            throw new RuntimeException("Please configure the backup.json file");
        }

        return config;
    }

    public static void backup(IUserInterface user, boolean compression) throws IOException {
        try {
            Config config = loadConfig();


            File directory = new File(config.sourcePath);
            File target = new File(config.targetPath);

            SessionStore store = new SessionStore(new File(target, "sessions"));
            Session session = store.loadLatestSession();

            session.startNewSession(user, directory, config.excludes);
            session.copyChanges(user, directory, target, compression);

            store.persistSession(session);

            user.setState(IUserInterface.State.DONE_BACKUP);
        } catch (Exception e) {
            user.error(e);

            System.exit(1);
        }
    }

    public static void restore(IUserInterface user, boolean compression) throws IOException {
        try {
            Config config = loadConfig();

            File directory = new File(config.sourcePath);
            File target = new File(config.targetPath);

            File sessionFile = user.pickSessionToRestore();

            Session session = SessionStore.loadFromSessionFile(sessionFile);

            session.restoreTo(user, directory, target, compression);

            user.setState(IUserInterface.State.DONE_RESTORE);
        } catch (Exception e) {
            user.error(e);

            System.exit(1);
        }
    }
}
