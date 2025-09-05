package de.toxicfox.backup.core;

import net.shadew.json.JsonNode;

public record BackupFile(long lastModified, long timestamp) {
    public static BackupFile fromJson(JsonNode node) {
        return new BackupFile(
                node.get("lastModified").asLong(),
                node.get("timestamp").asLong()
        );
    }

    public JsonNode toJson() {
        return JsonNode.object()
                .set("lastModified", lastModified)
                .set("timestamp", timestamp);
    }
}
