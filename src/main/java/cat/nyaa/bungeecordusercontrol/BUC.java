package cat.nyaa.bungeecordusercontrol;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BUC extends net.md_5.bungee.api.plugin.Plugin {
    public UserList userList;
    public Config config;
    public Messages msg;
    public BungeeProxy bungeeProxy;
    public boolean reloading = false;
    public Long lastReload = 0L;
    public FileWatcher fileWatcher;

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
        bungeeProxy = new BungeeProxy(this);
        lastReload = System.currentTimeMillis();
        fileWatcher = new FileWatcher(this);
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

    public synchronized void save() {
        lastReload = System.currentTimeMillis();
        config.save();
        userList.save();
    }

    public synchronized void reload() {
        reloading = true;
        Messages.load();
        config.load();
        userList.load();
        getProxy().getPluginManager().registerCommand(this, new Commands(this));
        reloading = false;
        getLogger().info(Messages.get("messages.reload"));
    }

    public synchronized boolean isReloading() {
        return reloading;
    }
}