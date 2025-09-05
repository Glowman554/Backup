package de.toxicfox.backup.core;

import java.io.File;

public interface IUserInterface {
    void log(String message);

    void setProgress(int progress);

    void setState(State state);

    void error(Exception e);

    File pickSessionToRestore();

    public enum State {
        INDEXING(false), DIFFING(false), COPYING(true), DONE_BACKUP(false), DONE_RESTORE(false);

        private final boolean progressIndicator;

        private State(boolean progressIndicator) {
            this.progressIndicator = progressIndicator;
        }

        public boolean hasProgressIndicator() {
            return progressIndicator;
        }
    }
}
