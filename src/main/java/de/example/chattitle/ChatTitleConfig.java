package de.example.chattitle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ChatTitleConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("chattitle.json");

    /** Alle Wörter, auf die reagiert wird. */
    public List<String> keywords = new ArrayList<>(List.of("hallo", "hilfe"));
    /** Haupttext des Titles. */
    public String title = "received message";
    /** Untertitel. {word} wird durch das erkannte Wort ersetzt. Leer lassen für keinen Untertitel. */
    public String subtitle = "{word}";
    /** Sound-ID, leer lassen für keinen Sound. */
    public String sound = "entity.experience_orb.pickup";
    public float volume = 1.0f;
    public float pitch = 1.0f;
    public boolean ignoreCase = true;
    /** Anzeigedauer in Ticks (20 Ticks = 1 Sekunde). */
    public int fadeIn = 10;
    public int stay = 60;
    public int fadeOut = 10;

    public static ChatTitleConfig load() {
        try {
            if (Files.exists(FILE)) {
                ChatTitleConfig cfg = GSON.fromJson(Files.readString(FILE), ChatTitleConfig.class);
                if (cfg != null) return cfg;
            }
        } catch (Exception e) {
            ChatTitleClient.LOGGER.error("Konfiguration konnte nicht gelesen werden, nutze Standardwerte", e);
        }
        ChatTitleConfig cfg = new ChatTitleConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try {
            Files.writeString(FILE, GSON.toJson(this));
        } catch (IOException e) {
            ChatTitleClient.LOGGER.error("Konfiguration konnte nicht gespeichert werden", e);
        }
    }

    /** Gibt das erkannte Wort zurück oder null, wenn keines enthalten ist. */
    public String findMatch(String message) {
        if (keywords == null) return null;
        String text = ignoreCase ? message.toLowerCase() : message;
        for (String word : keywords) {
            if (word == null || word.isEmpty()) continue;
            String check = ignoreCase ? word.toLowerCase() : word;
            if (text.contains(check)) return word;
        }
        return null;
    }
}
