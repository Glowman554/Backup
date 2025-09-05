package de.toxicfox.backup.core;

import net.shadew.json.Json;
import net.shadew.json.JsonNode;

import java.io.File;
import java.io.IOException;

public class SessionStore {
    private final File sessionDirectory;
    private final File sessionFile;
    private final Json json = Json.json();
    private JsonNode sessionsStore;

    public SessionStore(File sessionDirectory) throws IOException {
        this.sessionDirectory = sessionDirectory;
        this.sessionFile = new File(sessionDirectory, "sessions.json");
        this.sessionDirectory.mkdirs();
        load();
    }

    public void load() throws IOException {

        if (sessionFile.exists()) {
            sessionsStore = json.parse(sessionFile);
        } else {
            sessionsStore = JsonNode.object().set("sessions", JsonNode.array());
            save();
        }
    }

    public void save() throws IOException {
        json.serialize(sessionsStore, sessionFile);
    }

    public void persistSession(Session session) {
        sessionsStore.get("sessions").add(session.getTimestamp());
        try {
            json.serialize(session.toJSON(), new File(sessionDirectory, session.getTimestamp() + ".json"));
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Session loadLatestSession() {
        JsonNode sessions = sessionsStore.get("sessions");
        if (sessions.size() == 0) {
            return new Session();
        }
        long session = sessions.get(sessions.size() - 1).asLong();
        return loadFromSessionFile(new File(sessionDirectory, session + ".json"));
    }

    public static Session loadFromSessionFile(File file) {
        try {
            return Session.fromJSON(Json.json().parse(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
