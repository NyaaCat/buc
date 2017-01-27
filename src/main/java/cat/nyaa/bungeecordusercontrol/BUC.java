package cat.nyaa.bungeecordusercontrol;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BUC extends net.md_5.bungee.api.plugin.Plugin {
    public UserList userList;
    public Config config;
    public Messages msg;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        userList = new UserList(this);
        config = new Config(this);
        msg = new Messages(this);
        Messages.load();
        config.load();
        userList.load();
        this.getProxy().getPluginManager().registerListener(this, new PlayerListener(this));
        this.getProxy().getPluginManager().registerCommand(this, new Commands(this));
        userList.save();
        config.save();
    }

    @Override
    public void onDisable() {
        save();
    }

    public boolean kickPlayer(UUID uuid, BaseComponent... reason) {
        ProxiedPlayer player = getProxy().getPlayer(uuid);
        if (player != null) {
            player.disconnect(reason);
            return true;
        } else {
            return false;
        }
    }

    public void save() {
        config.save();
        userList.save();
    }
}