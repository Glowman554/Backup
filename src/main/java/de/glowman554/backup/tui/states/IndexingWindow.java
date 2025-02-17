package de.glowman554.backup.tui.states;

import com.googlecode.lanterna.gui2.*;
import de.glowman554.backup.tui.AbstractState;

import java.util.List;

public class IndexingWindow extends AbstractState {

    private final BasicWindow window = new BasicWindow("Indexing");
    private final Label label = new Label("Indexing files...");

    private long begin;

    public IndexingWindow() {
        super();

        Panel panel = new Panel();
        panel.addComponent(label);

        window.setComponent(panel);
        window.setHints(List.of(Window.Hint.CENTERED));
    }

    @Override
    public void entry(WindowBasedTextGUI gui) {
        begin = System.currentTimeMillis();
        gui.addWindow(window);
    }

    @Override
    public void update(WindowBasedTextGUI gui) {
        long elapsed = System.currentTimeMillis() - begin;
        elapsed /= 1000;
        label.setText("Indexing files... (" + elapsed + "s)");
    }

    @Override
    public void exit(WindowBasedTextGUI gui) {
        gui.removeWindow(window);
    }
}
