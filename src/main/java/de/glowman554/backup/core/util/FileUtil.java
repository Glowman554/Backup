package de.glowman554.backup.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtil {
    public static void copyFile(File source, File target) throws IOException {
        try (FileInputStream sourceStream = new FileInputStream(source); FileOutputStream targetStream = new FileOutputStream(target)) {
            try (FileChannel sourceChannel = sourceStream.getChannel(); FileChannel targetChannel = targetStream.getChannel()) {
                targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            }
        }
    }

    /*
    public static long readLastModifiedTime (File file) {
        try {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return attr.lastModifiedTime().toMillis();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    */

    public static String calculateHash(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] byteArray = new byte[1024];
                int bytesCount;
                while ((bytesCount = fis.read(byteArray)) != -1) {
                    digest.update(byteArray, 0, bytesCount);
                }
            }

            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
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
}
