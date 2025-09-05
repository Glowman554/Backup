package de.toxicfox.backup.tui;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;

public class DebugWindow extends BasicWindow {
    private final Label[] labels;

    public DebugWindow() {
        super("Debug");
        Panel panel = new Panel();

        labels = new Label[20];
        for (int i = 0; i < labels.length; i++) {
            Label label = new Label("");
            labels[i] = label;
            panel.addComponent(label);
        }


        setComponent(panel);
    }

    public void log(String message) {
        synchronized (labels) {
            for (int i = labels.length - 1; i > 0; i--) {
                labels[i].setText(labels[i - 1].getText());
            }
            labels[0].setText(message);
        }
    }
}
