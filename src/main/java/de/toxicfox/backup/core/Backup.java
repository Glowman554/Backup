package de.toxicfox.backup.core;

import de.toxicfox.backup.core.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Backup {
    private static Config loadConfig() {
        Config config = new Config();
        config.load();

        if (config.sourcePath.equals("<change me>") || config.targetPath.equals("<change me>")) {
            throw new RuntimeException("Please configure the backup.json file");
        }

        return config;
    }

    public static SessionResult backup(IUserInterface user, FileUtil fileUtil) {
        try {
            Config config = loadConfig();
            executeCommand(user, config.hooks.preBackup);

            File directory = new File(config.sourcePath);
            File target = new File(config.targetPath);

            SessionStore store = new SessionStore(new File(target, "sessions"));
            Session session = store.loadLatestSession();

            session.startNewSession(user, directory, config.excludes);
            session.copyChanges(user, directory, target, fileUtil);

            store.persistSession(session);
            executeCommand(user, config.hooks.postBackup);

            user.setState(IUserInterface.State.DONE_BACKUP);

            return session.getResult();
        } catch (Exception e) {
            user.error(e);
        }

        return null;

    }

    public static void restore(IUserInterface user, FileUtil fileUtil) throws IOException {
        try {
            Config config = loadConfig();
            executeCommand(user, config.hooks.preRestore);

            File directory = new File(config.sourcePath);
            File target = new File(config.targetPath);

            File sessionFile = user.pickSessionToRestore();

            Session session = SessionStore.loadFromSessionFile(sessionFile);

            session.restoreTo(user, directory, target, fileUtil);
            executeCommand(user, config.hooks.preRestore);

            user.setState(IUserInterface.State.DONE_RESTORE);
        } catch (Exception e) {
            user.error(e);
        }
    }

     private static void executeCommand(IUserInterface user, String command) throws IOException {
        if (command == null) {
            return;
        }

        user.log("Executing command '" + command + "'");
        try {
            String[] cmd = {"/bin/bash", "-c", command};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    user.log(line);
                }
            }

            int exitCode = process.waitFor();
            user.log("Exit code: " + exitCode);
            
            if (exitCode != 0) {
                throw new RuntimeException("Command failed with exit code " + exitCode);
            }
        } catch (Exception e) {
            throw new IOException("Error while executing command: " + command);
        }
    }
}
