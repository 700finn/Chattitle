package de.example.chattitle;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ChatTitleConfigScreen extends Screen {
    private static final String[][] SOUNDS = {
            {"", "Kein Sound"},
            {"entity.experience_orb.pickup", "XP-Kugel"},
            {"entity.player.levelup", "Level-Up"},
            {"block.note_block.pling", "Pling"},
            {"block.note_block.bell", "Glocke (Notenblock)"},
            {"block.note_block.chime", "Chime"},
            {"block.bell.use", "Dorfglocke"},
            {"ui.toast.challenge_complete", "Erfolg"},
            {"entity.villager.yes", "Dorfbewohner Ja"},
            {"entity.arrow.hit_player", "Pfeil-Treffer"},
            {"entity.firework_rocket.blast", "Feuerwerk"},
            {"entity.enderman.teleport", "Enderman Teleport"},
            {"block.anvil.land", "Amboss"},
            {"entity.lightning_bolt.thunder", "Donner"}
    };

    private final Screen parent;
    private final ChatTitleConfig config = ChatTitleClient.getConfig();
    private final List<String[]> sounds = new ArrayList<>();
    private int soundIndex;
    private ButtonWidget soundButton;

    public ChatTitleConfigScreen(Screen parent) {
        super(Text.literal("Chat Title"));
        this.parent = parent;
        for (String[] s : SOUNDS) sounds.add(s);
        if (config.keywords == null) config.keywords = new ArrayList<>();
        soundIndex = indexOfSound(config.sound == null ? "" : config.sound);
    }

    private int indexOfSound(String id) {
        for (int i = 0; i < sounds.size(); i++) {
            if (sounds.get(i)[0].equals(id)) return i;
        }
        sounds.add(new String[]{id, id}); // eigener Sound aus der Config
        return sounds.size() - 1;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 150;
        int fx = left + 95;
        int fw = 205;
        int y = Math.max(8, this.height / 2 - 110);

        // Wörter
        label(left, y, "Wörter (Komma):");
        TextFieldWidget kw = new TextFieldWidget(textRenderer, fx, y, fw, 20, Text.literal("Wörter"));
        kw.setMaxLength(512);
        kw.setText(String.join(", ", config.keywords));
        kw.setChangedListener(this::updateKeywords);
        addDrawableChild(kw);
        y += 24;

        // Title
        label(left, y, "Title:");
        TextFieldWidget title = new TextFieldWidget(textRenderer, fx, y, fw, 20, Text.literal("Title"));
        title.setMaxLength(128);
        title.setText(config.title == null ? "" : config.title);
        title.setChangedListener(s -> config.title = s);
        addDrawableChild(title);
        y += 24;

        // Untertitel
        label(left, y, "Untertitel:");
        TextFieldWidget sub = new TextFieldWidget(textRenderer, fx, y, fw, 20, Text.literal("Untertitel"));
        sub.setMaxLength(128);
        sub.setText(config.subtitle == null ? "" : config.subtitle);
        sub.setChangedListener(s -> config.subtitle = s);
        addDrawableChild(sub);
        y += 24;

        // Sound
        label(left, y, "Sound:");
        addDrawableChild(ButtonWidget.builder(Text.literal("<"), b -> changeSound(-1))
                .dimensions(fx, y, 20, 20).build());
        soundButton = ButtonWidget.builder(Text.empty(), b -> changeSound(1))
                .dimensions(fx + 22, y, 161, 20).build();
        addDrawableChild(soundButton);
        addDrawableChild(ButtonWidget.builder(Text.literal(">"), b -> changeSound(1))
                .dimensions(fx + 185, y, 20, 20).build());
        updateSoundButton();
        y += 24;

        // Lautstärke
        label(left, y, "Lautstärke:");
        addDrawableChild(new ValueSlider(fx, y, fw, 20, "Lautstärke", 0.0, 2.0, config.volume,
                v -> config.volume = v.floatValue()));
        y += 24;

        // Tonhöhe
        label(left, y, "Tonhöhe:");
        addDrawableChild(new ValueSlider(fx, y, fw, 20, "Tonhöhe", 0.5, 2.0, config.pitch,
                v -> config.pitch = v.floatValue()));
        y += 24;

        // Groß-/Kleinschreibung
        label(left, y, "Groß/Klein:");
        ButtonWidget caseButton = ButtonWidget.builder(caseText(), b -> {
            config.ignoreCase = !config.ignoreCase;
            b.setMessage(caseText());
        }).dimensions(fx, y, fw, 20).build();
        addDrawableChild(caseButton);
        y += 28;

        // Test + Fertig
        addDrawableChild(ButtonWidget.builder(Text.literal("Testen"), b -> {
            String word = config.keywords.isEmpty() ? "test" : config.keywords.get(0);
            ChatTitleClient.show(client, config, word);
        }).dimensions(left, y, 147, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Fertig"), b -> close())
                .dimensions(left + 153, y, 147, 20).build());
    }

    private void label(int x, int y, String text) {
        addDrawableChild(new TextWidget(x, y, 90, 20, Text.literal(text), textRenderer));
    }

    private Text caseText() {
        return Text.literal(config.ignoreCase ? "Ignorieren: Ja" : "Ignorieren: Nein");
    }

    private void updateKeywords(String text) {
        List<String> list = new ArrayList<>();
        for (String part : text.split(",")) {
            String w = part.trim();
            if (!w.isEmpty()) list.add(w);
        }
        config.keywords = list;
    }

    private void changeSound(int delta) {
        soundIndex = Math.floorMod(soundIndex + delta, sounds.size());
        config.sound = sounds.get(soundIndex)[0];
        updateSoundButton();
        ChatTitleClient.playSound(client, config.sound, config.pitch, config.volume);
    }

    private void updateSoundButton() {
        soundButton.setMessage(Text.literal(sounds.get(soundIndex)[1]));
    }

    @Override
    public void close() {
        config.save();
        client.setScreen(parent);
    }

    /** Einfacher Schieberegler mit Beschriftung und Wert. */
    private static class ValueSlider extends SliderWidget {
        private final String label;
        private final double min;
        private final double max;
        private final Consumer<Double> onChange;

        ValueSlider(int x, int y, int w, int h, String label, double min, double max,
                    double current, Consumer<Double> onChange) {
            super(x, y, w, h, Text.empty(), (Math.max(min, Math.min(max, current)) - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.onChange = onChange;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(label + ": " + String.format(Locale.ROOT, "%.2f", min + (max - min) * value)));
        }

        @Override
        protected void applyValue() {
            onChange.accept(min + (max - min) * value);
        }
    }
}
