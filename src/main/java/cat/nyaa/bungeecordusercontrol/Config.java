package cat.nyaa.bungeecordusercontrol;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public Toml conf;
    public boolean whitelist_enable = true;
    public String buc_command = "buc";
    private BUC plugin;

    public Config(BUC pl) {
        plugin = pl;
    }

    public void load() {
        try {
            File file = new File(plugin.getDataFolder(), "config.toml");
            if (!file.isFile()) {
                file.createNewFile();
            }
            conf = new Toml().read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        whitelist_enable = conf.getBoolean("whitelist_enable", true);
        buc_command = conf.getString("buc_command", "buc");
    }

    public void save() {
        try {
            File file = new File(plugin.getDataFolder(), "config.toml");
            if (!file.isFile()) {
                file.createNewFile();
            }
            TomlWriter tomlWriter = new TomlWriter();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("whitelist_enable", whitelist_enable);
            map.put("buc_command", buc_command);
            tomlWriter.write(map, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
