package de.toxicfox.backup.tui;

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;

public abstract class AbstractState {
    public abstract void entry(WindowBasedTextGUI gui);

    public abstract void update(WindowBasedTextGUI gui);

    public abstract void exit(WindowBasedTextGUI gui);
}
