package de.glowman554.backup.core.util;

import java.io.*;
import java.util.ArrayList;

public class FileUtil {
    private final ArrayList<FileModification> modifications = new ArrayList<>();

    public void copyBackup(File source, File target) throws IOException {
        try (InputStream fis = new FileInputStream(source);
             OutputStream fos = new FileOutputStream(target)) {

            OutputStream modifiedOut = fos;
            for (FileModification mod : modifications) {
                modifiedOut = mod.apply(modifiedOut);
            }

            fis.transferTo(modifiedOut);

            modifiedOut.close();
        }
    }

    public void copyRestore(File source, File target) throws IOException {
        try (InputStream fis = new FileInputStream(source);
             OutputStream fos = new FileOutputStream(target)) {

            InputStream modifiedIn = fis;
            for (FileModification mod : modifications) {
                modifiedIn = mod.unapply(modifiedIn);
            }

            modifiedIn.transferTo(fos);

            modifiedIn.close();
        }
    }

    public void addModification(FileModification modification) {
        modifications.add(modification);
    }

    public static String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return size / 1024 + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            return size / 1024 / 1024 + " MB";
        } else {
            return size / 1024 / 1024 / 1024 + " GB";
        }
    }


    public static interface FileModification {
        OutputStream apply(OutputStream stream) throws IOException;

        InputStream unapply(InputStream stream) throws IOException;
    }
}
