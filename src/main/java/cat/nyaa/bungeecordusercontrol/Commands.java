package cat.nyaa.bungeecordusercontrol;


import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Commands extends Command {
    private BUC plugin;


    public Commands(BUC pl) {
        super(pl.config.buc_command);
        plugin = pl;
    }

    /**
     * Execute this command with the specified sender and arguments.
     *
     * @param sender the executor of this command
     * @param args   arguments used to invoke this command
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        String act = (args.length == 0 ? "help" : args[0].toLowerCase());
        if (act.equals("help")) {
            printHelp(sender, args);
        } else if (act.equals("whitelist") && args.length >= 2) {
            if (args[1].equalsIgnoreCase("on")) {
                if (!sender.hasPermission("buc.whitelist.toggle")) {
                    sender.sendMessage(Messages.getTextComponent("messages.no_permission"));
                    return;
                }
                plugin.userList.setEnableWhitelist(true);
                plugin.getLogger().info(Messages.get("log.whitelist.toggle", sender.getName(), "on"));
                sender.sendMessage(Messages.getTextComponent("messages.whitelist.enable"));
                plugin.save();
            } else if (args[1].equalsIgnoreCase("off")) {
                if (!sender.hasPermission("buc.whitelist.toggle")) {
                    sender.sendMessage(Messages.getTextComponent("messages.no_permission"));
                    return;
                }
                plugin.userList.setEnableWhitelist(false);
                plugin.getLogger().info(Messages.get("log.whitelist.toggle", sender.getName(), "off"));
                sender.sendMessage(Messages.getTextComponent("messages.whitelist.disable"));
                plugin.save();
            } else if (args[1].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("buc.whitelist.reload")) {
                    sender.sendMessage(Messages.getTextComponent("messages.no_permission"));
                    return;
                }
                plugin.getLogger().info(Messages.get("log.whitelist.reload", sender.getName()));
                sender.sendMessage(Messages.getTextComponent("messages.whitelist.reload"));
                plugin.userList.reloadWhitelist();
            } else if (args[1].equalsIgnoreCase("add") && args.length == 3) {
                if (!sender.hasPermission("buc.whitelist.add")) {
                    sender.sendMessage(Messages.getTextComponent("messages.no_permission"));
                    return;
                }
                String name = args[2];
                User user = plugin.userList.getUserByName(name);
                if (user == null) {
                    new Task() {
                        @Override
                        public void run() {
                            User user = MojangAPI.getUserByName(name);
                            if (user != null) {
                                addWhitelist(sender, user.getPlayerUUID(), user.getPlayerName());
                            } else {
                                sender.sendMessage(Messages.getTextComponent("messages.player_not_found", name));
                            }
                        }
                    }.start();
                } else {
                    addWhitelist(sender, user.getPlayerUUID(), user.getPlayerName());
                }
                return;
            } else if (args[1].equalsIgnoreCase("remove") && args.length == 3) {
                if (!sender.hasPermission("buc.whitelist.remove")) {
                    sender.sendMessage(Messages.getTextComponent("messages.no_permission"));
                    return;
                }
                String name = args[2];
                User user = plugin.userList.getUserByName(name);
                if (user != null && user.isWhitelisted()) {
                    plugin.userList.removeWhitelist(user.getPlayerUUID());
                    plugin.getLogger().info(Messages.get("log.whitelist.remove", user.getPlayerName(), user.getPlayerUUID(), sender.getName()));
                    sender.sendMessage(Messages.getTextComponent("messages.whitelist.remove", user.getPlayerName(), user.getPlayerUUID()));
                    plugin.userList.save();
                } else {
                    sender.sendMessage(Messages.getTextComponent("messages.player_not_found", name));
                }
            } else {
                printHelp(sender, args);
            }
        } else if (act.equals("ban") && args.length >= 2) {
            if (!sender.hasPermission("buc.ban")) {
                sender.sendMessage(Messages.getTextComponent("messages.no_permission"));
                return;
            }
            String name = args[1];
            String reason = args.length == 3 ? args[2] : "";
            User user = plugin.userList.getUserByName(name);
            if (user == null) {
                new Task() {
                    @Override
                    public void run() {
                        User user = MojangAPI.getUserByName(name);
                        if (user != null) {
                            banPlayer(sender, user.getPlayerUUID(), user.getPlayerName(), reason);
                        } else {
                            sender.sendMessage(Messages.getTextComponent("messages.player_not_found", name));
                        }
                    }
                }.start();
            } else {
                banPlayer(sender, user.getPlayerUUID(), user.getPlayerName(), reason);
            }

        } else if (act.equals("tempban") && args.length >= 2) {
            if (!sender.hasPermission("buc.tempban")) {
                sender.sendMessage(Messages.getTextComponent("messages.no_permission"));
                return;
            }
            String name = args[1];
            String time = args.length >= 3 ? args[2] : "";
            String reason = args.length == 4 ? args[3] : "";
            User user = plugin.userList.getUserByName(name);
            if (user == null) {
                new Task() {
                    @Override
                    public void run() {
                        User user = MojangAPI.getUserByName(name);
                        if (user != null) {
                            tempbanPlayer(sender, user.getPlayerUUID(), user.getPlayerName(), reason, time);
                        } else {
                            sender.sendMessage(Messages.getTextComponent("messages.player_not_found", name));
                        }
                    }
                }.start();
            } else {
                tempbanPlayer(sender, user.getPlayerUUID(), user.getPlayerName(), reason, time);
            }

        } else if (act.equals("unban") && args.length >= 2) {
            if (!sender.hasPermission("buc.unban")) {
                sender.sendMessage(Messages.getTextComponent("messages.no_permission"));
                return;
            }
            String name = args[1];
            User user = plugin.userList.getUserByName(name);
            if (user == null) {
                new Task() {
                    @Override
                    public void run() {
                        User user = MojangAPI.getUserByName(name);
                        if (user != null) {
                            unbanPlayer(sender, user.getPlayerUUID(), user.getPlayerName());
                        } else {
                            sender.sendMessage(Messages.getTextComponent("messages.player.not_found", name));
                        }
                    }
                }.start();
            } else {
                unbanPlayer(sender, user.getPlayerUUID(), user.getPlayerName());
            }
        } else if (act.equals("haproxy") && args.length >= 2) {
            if (args[1].equalsIgnoreCase("on")) {
                if (!sender.hasPermission("buc.haproxy.toggle")) {
                    sender.sendMessage(Messages.getTextComponent("messages.no_permission"));
                    return;
                }
                plugin.config.haproxy_enable = true;
                plugin.getLogger().info(Messages.get("log.haproxy.toggle", sender.getName(), "on"));
                sender.sendMessage(Messages.getTextComponent("messages.haproxy.enable"));
                plugin.save();
            } else if (args[1].equalsIgnoreCase("off")) {
                if (!sender.hasPermission("buc.haproxy.toggle")) {
                    sender.sendMessage(Messages.getTextComponent("messages.no_permission"));
                    return;
                }
                plugin.config.haproxy_enable = false;
                plugin.getLogger().info(Messages.get("log.haproxy.toggle", sender.getName(), "off"));
                sender.sendMessage(Messages.getTextComponent("messages.haproxy.disable"));
                plugin.save();
            } else {
                printHelp(sender, args);
            }
        } else if (act.equals("reload")) {
            if (!sender.hasPermission("buc.reload")) {
                sender.sendMessage(Messages.getTextComponent("messages.no_permission"));
                return;
            }
            plugin.config.load();
            plugin.config.save();
            plugin.getLogger().info(Messages.get("log.reload", sender.getName()));
            sender.sendMessage(Messages.getTextComponent("messages.reload"));
        } else {
            printHelp(sender, args);
        }
    }

    public void printHelp(CommandSender sender, String[] args) {
        TextComponent msg = new TextComponent();
        msg.addExtra("--------- ");
        msg.addExtra(plugin.getDescription().getName());
        msg.addExtra(" ");
        msg.addExtra(plugin.getDescription().getVersion());
        msg.addExtra(" ---------");
        msg.setColor(ChatColor.AQUA);
        sender.sendMessage(msg);
        sender.sendMessage(Messages.getTextComponent("command.help.whitelist_toggle", plugin.config.buc_command));
        sender.sendMessage(Messages.getTextComponent("command.help.whitelist_add", plugin.config.buc_command));
        sender.sendMessage(Messages.getTextComponent("command.help.whitelist_remove", plugin.config.buc_command));
        sender.sendMessage(Messages.getTextComponent("command.help.whitelist_reload", plugin.config.buc_command));
        sender.sendMessage(Messages.getTextComponent("command.help.ban", plugin.config.buc_command));
        sender.sendMessage(Messages.getTextComponent("command.help.tempban", plugin.config.buc_command));
        sender.sendMessage(Messages.getTextComponent("command.help.unban", plugin.config.buc_command));
        sender.sendMessage(Messages.getTextComponent("command.help.haproxy_toggle", plugin.config.buc_command));
        sender.sendMessage(Messages.getTextComponent("command.help.reload", plugin.config.buc_command));
    }

    private void banPlayer(CommandSender sender, UUID uuid, String name, String reason) {
        if (!plugin.userList.isBanned(uuid)) {
            plugin.userList.banUser(uuid, name, UserList.FORMAT.format(new Date()),
                    sender.getName(), "forever", reason);
            plugin.getLogger().info(plugin.userList.getUserByUUID(uuid).toString());
            plugin.kickPlayer(uuid, Messages.getTextComponent("messages.login.banned", reason));
            sender.sendMessage(Messages.getTextComponent("messages.ban.success", name, uuid.toString()));
            plugin.userList.save();
        } else {
            sender.sendMessage(Messages.getTextComponent("messages.ban.already_banned", name));
        }
    }

    private void tempbanPlayer(CommandSender sender, UUID uuid, String name, String reason, String banTime) {
        if (!plugin.userList.isBanned(uuid)) {
            Date date = stringToDate(banTime);
            if (date == null || date.before(new Date())) {
                sender.sendMessage(Messages.getTextComponent("messages.tempban.time_invalid", name));
                return;
            }
            plugin.userList.banUser(uuid, name, UserList.FORMAT.format(new Date()),
                    sender.getName(), UserList.FORMAT.format(date), reason);
            plugin.getLogger().info(plugin.userList.getUserByUUID(uuid).toString());
            plugin.kickPlayer(uuid, Messages.getTextComponent("messages.login.tempban", reason, UserList.FORMAT.format(date)));
            sender.sendMessage(Messages.getTextComponent("messages.ban.success", name, uuid.toString()));
            plugin.userList.save();
        } else {
            sender.sendMessage(Messages.getTextComponent("messages.ban.already_banned", name));
        }
    }

    private void unbanPlayer(CommandSender sender, UUID uuid, String name) {
        if (plugin.userList.isBanned(uuid)) {
            User user = plugin.userList.getUserByUUID(uuid);
            plugin.getLogger().info(user.toString());
            plugin.getLogger().info(Messages.get("log.unban", name, uuid, sender.getName()));
            plugin.userList.unbanUser(user.getPlayerUUID());
            sender.sendMessage(Messages.getTextComponent("messages.unban.success", name));
            plugin.userList.save();
        } else {
            sender.sendMessage(Messages.getTextComponent("messages.unban.not_banned", name));
        }
    }

    private boolean addWhitelist(CommandSender sender, UUID uuid, String name) {
        if (!plugin.userList.isWhitelisted(uuid)) {
            plugin.userList.addWhitelist(uuid, name);
            plugin.getLogger().info(Messages.get("log.whitelist.add", name, uuid, sender.getName()));
            sender.sendMessage(Messages.getTextComponent("messages.whitelist.add", name, uuid));
            plugin.userList.save();
            return true;
        } else {
            sender.sendMessage(Messages.getTextComponent("messages.whitelist.already_exists", name));
            return false;
        }
    }

    private Date stringToDate(String str) {
        try {
            long days = 0;
            long hours = 0;
            long minutes = 0;
            long seconds = 0;
            Pattern compile = Pattern.compile("(\\d+)d");
            Matcher matcher = compile.matcher(str);
            if (matcher.find()) {
                days = Long.valueOf(str.substring(matcher.start(), matcher.end() - 1));
            }
            compile = Pattern.compile("(\\d+)h");
            matcher = compile.matcher(str);
            if (matcher.find()) {
                hours = Long.valueOf(str.substring(matcher.start(), matcher.end() - 1));
            }
            compile = Pattern.compile("(\\d+)m");
            matcher = compile.matcher(str);
            if (matcher.find()) {
                minutes = Long.valueOf(str.substring(matcher.start(), matcher.end() - 1));
            }
            compile = Pattern.compile("(\\d+)s");
            matcher = compile.matcher(str);
            if (matcher.find()) {
                seconds = Long.valueOf(str.substring(matcher.start(), matcher.end() - 1));
            }
            long time = 0;
            time += days * 86400000;
            time += hours * 3600000;
            time += minutes * 60000;
            time += seconds * 1000;
            if (time < 1000) {
                return null;
            }
            return new Date(System.currentTimeMillis() + time);
        } catch (NumberFormatException e) {
            //e.printStackTrace();
            return null;
        }
    }
}
