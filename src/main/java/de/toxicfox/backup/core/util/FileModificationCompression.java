package de.toxicfox.backup.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileModificationCompression implements FileUtil.FileModification {
    @Override
    public OutputStream apply(OutputStream stream) throws IOException {
        return new GZIPOutputStream(stream);
    }

    @Override
    public InputStream unapply(InputStream stream) throws IOException {
        return new GZIPInputStream(stream);
    }
}
