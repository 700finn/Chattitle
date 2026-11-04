package de.example.chattitle;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatTitleClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("chattitle");
    private static ChatTitleConfig config;

    public static ChatTitleConfig getConfig() {
        return config;
    }

    @Override
    public void onInitializeClient() {
        config = ChatTitleConfig.load();

        ClientReceiveMessageEvents.CHAT.register(
                (message, signedMessage, sender, params, receptionTimestamp) -> check(message));

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) check(message);
        });

        // /chattitle öffnet das Einstellungsmenü
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("chattitle").executes(ctx -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    mc.send(() -> mc.setScreen(new ChatTitleConfigScreen(null)));
                    return 1;
                })));
    }

    private static void check(Text message) {
        String word = config.findMatch(message.getString());
        if (word == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> show(client, config, word));
    }

    /** Zeigt Title, Untertitel und spielt den Sound ab. */
    public static void show(MinecraftClient client, ChatTitleConfig cfg, String word) {
        InGameHud hud = client.inGameHud;
        String sub = cfg.subtitle == null ? "" : cfg.subtitle.replace("{word}", word);

        hud.setTitleTicks(cfg.fadeIn, cfg.stay, cfg.fadeOut);
        hud.setSubtitle(Text.literal(sub));
        hud.setTitle(Text.literal(cfg.title));
        playSound(client, cfg.sound, cfg.pitch, cfg.volume);
    }

    public static void playSound(MinecraftClient client, String soundId, float pitch, float volume) {
        if (soundId == null || soundId.isBlank()) return;
        Identifier id = Identifier.tryParse(soundId);
        if (id == null) return;
        client.getSoundManager().play(
                PositionedSoundInstance.ui(SoundEvent.of(id), pitch, volume));
    }
}
