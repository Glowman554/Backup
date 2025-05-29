package de.glowman554.backup.core;

import de.glowman554.config.ConfigFile;
import de.glowman554.config.auto.Saved;

import java.io.File;

public class Config extends ConfigFile {
    @Saved
    public String sourcePath = "<change me>";
    @Saved
    public String targetPath = "<change me>";

    @Saved
    public String[] excludes = new String[]{};

    public Config() {
        super(new File("backup.json"));
    }
}
