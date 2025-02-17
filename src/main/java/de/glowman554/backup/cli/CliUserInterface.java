package de.glowman554.backup.cli;

import de.glowman554.backup.core.IUserInterface;

import java.io.File;
import java.util.Scanner;

public class CliUserInterface implements IUserInterface {
    private State previousState = null;

    @Override
    public void log(String message) {
        System.out.println(message);
    }

    @Override
    public void setProgress(int progress) {
        System.out.println(progress + "%");
    }

    @Override
    public void setState(State state) {
        if (previousState != state) {
            System.out.println("State: " + state);
            previousState = state;
        }
    }

    @Override
    public void error(Exception e) {
        e.printStackTrace();
    }

    @Override
    public File pickSessionToRestore() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter the path of the session file to restore: ");
            return new File(scanner.nextLine());
        }
    }
}
