package de.glowman554.backup.core;

import de.glowman554.backup.core.util.FileUtil;
import net.shadew.json.JsonNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class Session {
    private final HashSet<String> changedFiles = new HashSet<>();
    private final HashMap<String, BackupFile> files = new HashMap<>(); // needs to be persisted
    private long timestamp;

    public Session() {
        timestamp = System.currentTimeMillis();
    }

    public static Session fromJSON(JsonNode node) {
        Session session = new Session();
        for (String path : node.keySet()) {
            session.files.put(path, BackupFile.fromJson(node.get(path)));
        }

        return session;
    }

    public void startNewSession(IUserInterface user, File directory) {
        timestamp = System.currentTimeMillis();
        user.log("Starting new session: " + timestamp);

        HashMap<String, BackupFile> newFiles = new HashMap<>();

        indexDirectory(user, directory, directory, newFiles);

        changedFiles.clear();
        diffPreviousSession(user, newFiles);
    }

    private void threadedCopy(IUserInterface user, String[] paths, File base, File target, boolean restore, boolean compression) {
        user.setState(IUserInterface.State.COPYING);

        ThreadPool pool = new ThreadPool();

        int progress = -1;

        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            int newProgress = (int) ((double) (i + 1) / paths.length * 100);
            if (newProgress != progress) {
                progress = newProgress;
                user.setProgress(progress);
                // user.log(i + " / " + paths.length);
            }

            BackupFile file = files.get(path);
            File sourceFile = new File(base, path);
            File targetFile = new File(new File(target, String.valueOf(file.timestamp())), path);

            // user.log("Copying changed file: " + path);

            pool.submit(() -> {
                if (restore) {
                    sourceFile.getParentFile().mkdirs();
                } else {
                    targetFile.getParentFile().mkdirs();
                }
                try {
                    if (restore) {
                        if (compression) {
                            FileUtil.copyAndDecompressFile(targetFile, sourceFile);
                        } else {
                            FileUtil.copyFile(targetFile, sourceFile);
                        }
                    } else {
                        if (compression) {
                            FileUtil.copyAndCompressFile(sourceFile, targetFile);
                        } else {
                            FileUtil.copyFile(sourceFile, targetFile);
                        }
                    }
                } catch (IOException e) {
                    user.log("Failed to copy file: " + path);
                    files.remove(path);
                }
            });
        }

        pool.stop();
    }

    public void copyChanges(IUserInterface user, File base, File target, boolean compression) {
        String[] paths = changedFiles.toArray(String[]::new);

        long size = 0;
        for (String path : paths) {
            size += new File(base, path).length();
        }
        user.log("Size of changed files: " + FileUtil.formatSize(size));

        long free = target.getFreeSpace();
        if (size > free) {
            throw new RuntimeException("Not enough space on target drive");
        }

        threadedCopy(user, paths, base, target, false, compression);
    }

    public void restoreTo(IUserInterface user, File base, File target, boolean compression) {
        String[] paths = files.keySet().toArray(String[]::new);
        user.log("Restoring " + paths.length + " files");
        threadedCopy(user, paths, base, target, true, compression);
    }


    private void indexDirectory(IUserInterface user, File base, File directory, HashMap<String, BackupFile> output) {
        user.setState(IUserInterface.State.INDEXING);


        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                // user.log("Indexing directory: " + file.getAbsolutePath());

                indexDirectory(user, base, file, output);
            } else {
                long lastModified = file.lastModified();

                String relative = base.toURI().relativize(file.toURI()).getPath();
                output.put(relative, new BackupFile(lastModified, timestamp));
            }
        }
    }

    private void diffPreviousSession(IUserInterface user, HashMap<String, BackupFile> newFiles) {
        user.setState(IUserInterface.State.DIFFING);


        int deletions = 0;
        for (String path : files.keySet().toArray(String[]::new)) {
            BackupFile newFile = newFiles.get(path);

            if (newFile == null) {
                // user.log("File " + path + " was deleted");
                files.remove(path);
                deletions++;
            }
        }
        
        for (String path : newFiles.keySet()) {
            BackupFile newFile = newFiles.get(path);
            BackupFile oldFile = files.get(path);

            if (oldFile == null || oldFile.lastModified() != newFile.lastModified()) {
                files.put(path, newFile);
                changedFiles.add(path);
            }
        }



        user.log("Changed files: " + changedFiles.size());
        user.log("Deleted files: " + deletions);
    }

    public JsonNode toJSON() {
        JsonNode node = JsonNode.object();
        for (String path : files.keySet()) {
            BackupFile file = files.get(path);
            node.set(path, file.toJson());
        }

        return node;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
