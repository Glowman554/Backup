package de.toxicfox.backup.core;

import de.toxicfox.config.ConfigFile;
import de.toxicfox.config.Savable;
import de.toxicfox.config.auto.AutoSavable;
import de.toxicfox.config.auto.Saved;

import java.io.File;

public class Config extends ConfigFile {
    @Saved
    public String sourcePath = "<change me>";
    @Saved
    public String targetPath = "<change me>";

    @Saved
    public String[] excludes = new String[]{};

    @Saved(remap = Savable.class, optional = true)
    public Hooks hooks = new Hooks();


    public static class Hooks extends AutoSavable {
        @Saved(optional = true)
        public String postBackup = null;

        @Saved(optional = true)
        public String preBackup = null;
        
        @Saved(optional = true)
        public String postRestore = null;

        @Saved(optional = true)
        public String preRestore = null;
    }

    public Config() {
        super(new File("backup.json"));
        setSaveAfterLoad(true);
    }
}
