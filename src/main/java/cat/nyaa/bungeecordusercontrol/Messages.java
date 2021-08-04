package cat.nyaa.bungeecordusercontrol;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.moandjiezana.toml.Toml;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class Messages {
    private static BUC plugin;
    private static Toml local;
    private static Toml defaultMessage;

    public Messages(BUC pl) {
        plugin = pl;
    }

    public static String get(String path, Object... args) {
        if (Strings.isNullOrEmpty(local.getString(path))) {
            if (!Strings.isNullOrEmpty(defaultMessage.getString(path))) {
                return getTextComponent(String.format(defaultMessage.getString(path), args)).content().replaceAll("&","§");
            } else {
                return "unknown: " + path;
            }
        } else {
            return getTextComponent(
                    String.format(local.getString(path).length() == 0 ? path : local.getString(path), args)).content().replaceAll("&","§");
        }
    }

    public static TextComponent getTextComponent(String path, Object... args) {
        return textComponentOf(get(path, args));
    }
    public static TextComponent textComponentOf(String string) {
        final TextComponent.Builder textComponentBuilder = Component.text();
        return textComponentBuilder.content(string).build();
    }
    public static void load() {
        try {
            String s = CharStreams.toString(new InputStreamReader(BUC.class.getResourceAsStream("/messages.toml"), Charsets.UTF_8));
            defaultMessage = new Toml().read(s);
            File file = new File(plugin.getDataFolder(), "messages.toml");
            if (!file.isFile()) {
                Files.copy(BUC.class.getResourceAsStream("/messages.toml"), file.toPath());
            }
            local = new Toml().read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {

    }
}
