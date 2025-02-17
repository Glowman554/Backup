package de.glowman554.backup.tui;

import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import de.glowman554.backup.core.IUserInterface;

import java.io.File;
import java.io.IOException;

public class TuiUserInterface implements IUserInterface {
    private final Terminal terminal;
    private final WindowBasedTextGUI textGUI;

    private final DebugWindow debugWindow;

    private StateInstance current = StateInstance.DEFAULT;

    public TuiUserInterface() throws IOException {
        terminal = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();

        textGUI = new MultiWindowTextGUI(screen);

        debugWindow = new DebugWindow();
        textGUI.addWindow(debugWindow);
    }

    public File pickSessionToRestore() {
        return new FileDialogBuilder()
                .setTitle("Open File")
                .setDescription("Choose a session to restore")
                .setActionLabel("Restore")
                .build()
                .showDialog(textGUI);
    }


    private void transferState(StateInstance newState) {
        if (newState != current) {
            current.getState().exit(textGUI);
            current = newState;
            current.getState().entry(textGUI);
        }
    }


    @Override
    public void log(String message) {
        // System.out.println(message);
        debugWindow.log(message);

        safeUpdate();
    }

    @Override
    public void setProgress(int progress) {
        if (current.getState() instanceof ProgressingState progressingState) {
            progressingState.setProgress(progress);
        }
        safeUpdate();
    }

    @Override
    public void setState(State state) {
        // System.out.println("State: " + state);
        try {
            switch (state) {
                case INDEXING: {
                    transferState(StateInstance.INDEXING);
                }
                break;
                case DIFFING: {
                    transferState(StateInstance.DEFAULT);
                }
                break;
                case COPYING: {
                    transferState(StateInstance.COPYING);
                }
                break;
                case DONE_BACKUP:
                    new MessageDialogBuilder()
                            .setTitle("Done")
                            .setText("Backup completed successfully.")
                            .addButton(MessageDialogButton.Close)
                            .build().showDialog(textGUI);
                    terminal.close();
                    return;

                case DONE_RESTORE:
                    new MessageDialogBuilder()
                            .setTitle("Done")
                            .setText("Restore completed successfully.")
                            .addButton(MessageDialogButton.Close)
                            .build().showDialog(textGUI);
                    terminal.close();
                    return;
            }

            safeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void error(Exception e) {
        StringBuilder fullStackTrace = new StringBuilder();
        fullStackTrace.append(e.getClass().getName())
                .append(": ")
                .append(e.getMessage())
                .append("\n");

        for (StackTraceElement element : e.getStackTrace()) {
            fullStackTrace.append(element.toString()).append("\n");
        }

        new MessageDialogBuilder()
                .setTitle("Error")
                .setText(fullStackTrace.toString())
                .addButton(MessageDialogButton.Close)
                .build().showDialog(textGUI);
    }

    private void safeUpdate() {
        current.getState().update(textGUI);
        try {
            textGUI.updateScreen();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
