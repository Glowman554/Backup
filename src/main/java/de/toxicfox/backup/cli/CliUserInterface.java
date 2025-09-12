package de.toxicfox.backup.cli;

import de.toxicfox.backup.core.IUserInterface;

import java.io.File;
import java.util.Scanner;

public class CliUserInterface implements IUserInterface {
    private State previousState = null;

    @Override
    public void log(String message) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        System.out.printf("[%s::%s at %s:%s] %s\n", stackTraceElements[2].getClassName(), stackTraceElements[2].getMethodName(), stackTraceElements[2].getFileName(), stackTraceElements[2].getLineNumber(), message);
    }

    @Override
    public void setProgress(int progress) {
        log(progress + "%");
    }

    @Override
    public void setState(State state) {
        if (previousState != state) {
            log("State: " + state);
            previousState = state;
        }
    }

    @Override
    public void error(Exception e) {
        throw new RuntimeException(e);
    }

    @Override
    public File pickSessionToRestore() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter the path of the session file to restore: ");
            return new File(scanner.nextLine());
        }
    }
}
