package cat.nyaa.bungeecordusercontrol;


import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class PlayerListener implements Listener {
    private BUC plugin;

    public PlayerListener(BUC pl) {
        plugin = pl;
    }

    @EventHandler
    public void onPlayerLogin(LoginEvent event) {
        if (plugin.isReloading()) {
            event.setCancelled(true);
            event.setCancelReason(Messages.getTextComponent("messages.login.reload"));
            return;
        }
        UUID uuid = event.getConnection().getUniqueId();
        String name = event.getConnection().getName();
        if (plugin.userList.isEnableWhitelist() && !plugin.userList.isWhitelisted(uuid)) {
            event.setCancelReason(Messages.getTextComponent("messages.login.whitelist"));
            event.setCancelled(true);
            return;
        }
        if (plugin.userList.isShadowBaned(uuid)) {
            event.setCancelReason(Messages.getTextComponent("messages.mojang_fail"));
            event.setCancelled(true);
        }
        if (plugin.userList.isBanned(uuid)) {
            User user = plugin.userList.getUserByUUID(uuid);
            if (plugin.userList.banExpires(uuid)) {
                plugin.getLogger().info(user.toString());
                plugin.getLogger().info(Messages.get("log.unban", name, uuid, "[CONSOLE]"));
                plugin.userList.unbanUser(user.getPlayerUUID());
            } else if ("".equals(user.getBanExpires()) || user.getBanExpires().equalsIgnoreCase("forever")) {
                event.setCancelReason(Messages.getTextComponent("messages.login.banned", user.getBanReason()));
                event.setCancelled(true);
            } else {
                event.setCancelReason(Messages.getTextComponent("messages.login.tempban", user.getBanReason(), user.getBanExpires()));
                event.setCancelled(true);
            }
            return;
        }
        plugin.userList.updateUserCache(uuid, name);
    }
}