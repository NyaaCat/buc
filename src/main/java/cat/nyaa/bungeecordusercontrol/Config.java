package cat.nyaa.bungeecordusercontrol;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {
    public Configuration conf;
    public boolean whitelist_enable = true;
    public String buc_command = "buc";
    private BUC plugin;

    public Config(BUC pl) {
        plugin = pl;
    }

    public void load() {
        try {
            File file = new File(plugin.getDataFolder(), "config.yml");
            if (!file.isFile()) {
                file.createNewFile();
            }
            conf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        whitelist_enable = conf.getBoolean("whitelist.enable", true);
        buc_command = conf.getString("buc_command", "buc");
    }

    public void save() {
        conf.set("whitelist.enable", whitelist_enable);
        conf.set("buc_command", buc_command);
        try {
            File file = new File(plugin.getDataFolder(), "config.yml");
            if (!file.isFile()) {
                file.createNewFile();
            }
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(conf, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
