package de.example.chattitle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ChatTitleConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("chattitle.json");

    public String keyword = "hallo";
    public String title = "received message";
    public boolean ignoreCase = true;
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

    public boolean matches(String message) {
        if (keyword == null || keyword.isEmpty()) return false;
        return ignoreCase
                ? message.toLowerCase().contains(keyword.toLowerCase())
                : message.contains(keyword);
    }
}
