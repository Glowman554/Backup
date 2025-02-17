package de.glowman554.backup.tui;

import de.glowman554.backup.tui.states.CopyingWindow;
import de.glowman554.backup.tui.states.DefaultState;
import de.glowman554.backup.tui.states.IndexingWindow;

public enum StateInstance {
    DEFAULT(new DefaultState()),
    INDEXING(new IndexingWindow()),
    COPYING(new CopyingWindow()),
    ;
    private final AbstractState state;

    StateInstance(AbstractState state) {
        this.state = state;
    }

    public AbstractState getState() {
        return state;
    }
}
