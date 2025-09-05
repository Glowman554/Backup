package de.toxicfox.backup.core.util;

import de.toxicfox.backup.core.IUserInterface;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.SecureRandom;

public class FileModificationEncryption implements FileUtil.FileModification {
    private final SecretKeySpec secretKey;
    private final SecureRandom random = new SecureRandom();

    public FileModificationEncryption(File keyFile, IUserInterface userInterface) throws Exception {
        if (keyFile.exists()) {
            byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
            secretKey = new SecretKeySpec(keyBytes, "AES");
        } else {
            userInterface.log("Created encryption key at " + keyFile.getAbsolutePath());

            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey key = keyGen.generateKey();

            try (FileOutputStream fos = new FileOutputStream(keyFile)) {
                fos.write(key.getEncoded());
            }

            secretKey = new SecretKeySpec(key.getEncoded(), "AES");
        }
    }

    @Override
    public OutputStream apply(OutputStream stream) throws IOException {
        try {
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            stream.write(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            return new CipherOutputStream(stream, cipher);
        } catch (Exception e) {
            throw new IOException("Failed to initialize AES encryption", e);
        }
    }

    @Override
    public InputStream unapply(InputStream stream) throws IOException {
        try {
            byte[] iv = new byte[16];
            if (stream.read(iv) != 16) {
                throw new IOException("Invalid encrypted file: missing IV");
            }
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            return new CipherInputStream(stream, cipher);
        } catch (Exception e) {
            throw new IOException("Failed to initialize AES decryption", e);
        }
    }
}
