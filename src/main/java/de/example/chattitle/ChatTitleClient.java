package de.example.chattitle;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatTitleClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("chattitle");
    private static ChatTitleConfig config;

    @Override
    public void onInitializeClient() {
        config = ChatTitleConfig.load();

        ClientReceiveMessageEvents.CHAT.register(
                (message, signedMessage, sender, params, receptionTimestamp) -> check(message));

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) check(message);
        });
    }

    private static void check(Text message) {
        if (!config.matches(message.getString())) return;

        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            InGameHud hud = client.inGameHud;
            hud.setTitleTicks(config.fadeIn, config.stay, config.fadeOut);
            hud.setSubtitle(Text.empty());
            hud.setTitle(Text.literal(config.title));
        });
    }
}
