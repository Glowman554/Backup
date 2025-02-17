package de.glowman554.backup.tui.states;

import com.googlecode.lanterna.gui2.*;
import de.glowman554.backup.tui.ProgressingState;

import java.util.List;

public class CopyingWindow extends ProgressingState {

    private final BasicWindow window = new BasicWindow("Copying");
    private final ProgressBar progressBar = new ProgressBar();

    public CopyingWindow() {
        super();
        Panel panel = new Panel();
        Label label = new Label("Copying files...");
        panel.addComponent(label);

        progressBar.setPreferredWidth(40);
        panel.addComponent(progressBar);

        window.setComponent(panel);
        window.setHints(List.of(Window.Hint.CENTERED));
    }

    @Override
    public void entry(WindowBasedTextGUI gui) {
        gui.addWindow(window);
    }

    @Override
    public void update(WindowBasedTextGUI gui) {
    }

    @Override
    public void exit(WindowBasedTextGUI gui) {
        gui.removeWindow(window);
    }

    @Override
    public void setProgress(int progress) {
        progressBar.setValue(progress);
    }
}
