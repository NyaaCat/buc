package cat.nyaa.bungeecordusercontrol;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.text.Component;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

public class BUC {
    public final ProxyServer server;
    private final Logger logger;
    public UserList userList;
    public Config config;
    public Messages msg;
    public boolean reloading = false;
    public Long lastReload = 0L;
    public FileWatcher fileWatcher;
    public Path dataDir;

    @Inject
    public BUC(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDir = dataDirectory;
        if (!dataDir.toFile().exists()) {
            dataDir.toFile().mkdirs();
        }
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        onEnable();
    }

    public void onEnable() {
        userList = new UserList(this);
        config = new Config(this);
        msg = new Messages(this);
        Messages.load();
        config.load();
        userList.load();
        server.getEventManager().register(this, new PlayerListener(this));
        server.getCommandManager().register(new Commands(this), config.buc_command);
        userList.save();
        config.save();
        lastReload = System.currentTimeMillis();
        fileWatcher = new FileWatcher(this);
    }

    public void onDisable() {
        save();
    }

    public boolean kickPlayer(UUID uuid, Component reason) {
        Optional<Player> player = server.getPlayer(uuid);
        if (player.isPresent()) {
            player.get().disconnect(reason);
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
        server.getCommandManager().register(new Commands(this), config.buc_command);
        reloading = false;
        getLogger().info(Messages.get("messages.reload"));
    }

    public synchronized boolean isReloading() {
        return reloading;
    }

    public Logger getLogger() {
        return logger;
    }

    public File getDataFolder() {
        return dataDir.toFile();
    }
}