package de.toxicfox.backup.core;

import java.util.ArrayList;

public class SessionResult {
    private final long timestamp;
    private int changedFiles;
    private String changedFilesSizeGB;
    private int deletions;
    private ArrayList<String> failedFiles = new ArrayList<>();
            
    public SessionResult(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setChangedFiles(int changedFiles) {
        this.changedFiles = changedFiles;
    }

    public void setChangedFilesSizeGB(String changedFilesSizeGB) {
        this.changedFilesSizeGB = changedFilesSizeGB;
    }

    public void setDeletions(int deletions) {
        this.deletions = deletions;
    }

    public void addFailedFile(String error) {
        failedFiles.add(error);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getChangedFiles() {
        return changedFiles;
    }

    public String getChangedFilesSizeGB() {
        return changedFilesSizeGB;
    }

    public int getDeletions() {
        return deletions;
    }

    public ArrayList<String> getFailedFiles() {
        return failedFiles;
    }
}