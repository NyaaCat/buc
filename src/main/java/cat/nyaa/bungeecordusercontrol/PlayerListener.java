package cat.nyaa.bungeecordusercontrol;


import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.kyori.text.TextComponent;

import java.util.UUID;

public class PlayerListener {
    private BUC plugin;

    public PlayerListener(BUC pl) {
        plugin = pl;
    }

    @Subscribe
    public void onPlayerLogin(LoginEvent event) {
        if (plugin.isReloading()) {
            event.setResult(ResultedEvent.ComponentResult.denied(TextComponent.of(Messages.get("messages.login.reload"))));
            return;
        }
        UUID uuid = event.getPlayer().getUniqueId();
        String name = event.getPlayer().getUsername();
        if (plugin.userList.isEnableWhitelist() && !plugin.userList.isWhitelisted(uuid)) {
            event.setResult(ResultedEvent.ComponentResult.denied(TextComponent.of(Messages.get("messages.login.whitelist"))));
            return;
        }
        if (plugin.userList.isBanned(uuid)) {
            User user = plugin.userList.getUserByUUID(uuid);
            if (plugin.userList.banExpires(uuid)) {
                plugin.getLogger().info(user.toString());
                plugin.getLogger().info(Messages.get("log.unban", name, uuid, "[CONSOLE]"));
                plugin.userList.unbanUser(user.getPlayerUUID());
            } else if ("".equals(user.getBanExpires()) || user.getBanExpires().equalsIgnoreCase("forever")) {
                event.setResult(ResultedEvent.ComponentResult.denied(TextComponent.of(Messages.get("messages.login.banned", user.getBanReason()))));
            } else {
                event.setResult(ResultedEvent.ComponentResult.denied(TextComponent.of(Messages.get("messages.login.tempban", user.getBanReason(), user.getBanExpires()))));
            }
            return;
        }
        plugin.userList.updateUserCache(uuid, name);
    }
}