package cat.nyaa.bungeecordusercontrol;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class Messages {
    private static BUC plugin;
    private static Configuration local;
    private static Configuration defaultMessage;

    public Messages(BUC pl) {
        plugin = pl;
    }

    public static String get(String path, Object... args) {
        if ("".equals(local.getString(path))) {
            if (!"".equals(defaultMessage.getString(path))) {
                return ChatColor.translateAlternateColorCodes('&', String.format(defaultMessage.getString(path), args));
            } else {
                return "unknown: " + path;
            }
        } else {
            return ChatColor.translateAlternateColorCodes('&',
                    String.format(local.getString(path).length() == 0 ? path : local.getString(path), args));
        }
    }

    public static BaseComponent[] getTextComponent(String path, Object... args) {
        return TextComponent.fromLegacyText(get(path, args));
    }

    public static void load() {
        try {
            String s = CharStreams.toString(new InputStreamReader(BUC.class.getResourceAsStream("/messages.yml"), Charsets.UTF_8));
            defaultMessage = ConfigurationProvider.getProvider(YamlConfiguration.class).load(s);
            File file = new File(plugin.getDataFolder(), "messages.yml");
            if (!file.isFile()) {
                Files.copy(BUC.class.getResourceAsStream("/messages.yml"), file.toPath());
            }
            local = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            File file = new File(plugin.getDataFolder(), "messages.yml");
            if (!file.isFile()) {
                file.createNewFile();
            }
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(local, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
