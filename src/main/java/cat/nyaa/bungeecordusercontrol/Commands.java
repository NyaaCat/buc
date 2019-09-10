package cat.nyaa.bungeecordusercontrol;


import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Commands implements Command {
    private BUC plugin;


    public Commands(BUC pl) {
        super();
        plugin = pl;
    }

    /**
     * Execute this command with the specified sender and arguments.
     *
     * @param sender the executor of this command
     * @param args   arguments used to invoke this command
     */
    @Override
    public void execute(@NonNull CommandSource sender, String[] args) {
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
                plugin.getLogger().info(Messages.get("log.whitelist.toggle", getSenderName(sender), "on"));
                sender.sendMessage(Messages.getTextComponent("messages.whitelist.enable"));
                plugin.save();
            } else if (args[1].equalsIgnoreCase("off")) {
                if (!sender.hasPermission("buc.whitelist.toggle")) {
                    sender.sendMessage(Messages.getTextComponent("messages.no_permission"));
                    return;
                }
                plugin.userList.setEnableWhitelist(false);
                plugin.getLogger().info(Messages.get("log.whitelist.toggle", getSenderName(sender), "off"));
                sender.sendMessage(Messages.getTextComponent("messages.whitelist.disable"));
                plugin.save();
            } else if (args[1].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("buc.whitelist.reload")) {
                    sender.sendMessage(Messages.getTextComponent("messages.no_permission"));
                    return;
                }
                plugin.getLogger().info(Messages.get("log.whitelist.reload", getSenderName(sender)));
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
                    plugin.getLogger().info(Messages.get("log.whitelist.remove", user.getPlayerName(), user.getPlayerUUID(), getSenderName(sender)));
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
        } else if (act.equals("reload")) {
            if (!sender.hasPermission("buc.reload")) {
                sender.sendMessage(Messages.getTextComponent("messages.no_permission"));
                return;
            }
            plugin.reload();
            plugin.getLogger().info(Messages.get("log.reload", getSenderName(sender)));
            sender.sendMessage(Messages.getTextComponent("messages.reload"));
        } else {
            printHelp(sender, args);
        }
    }

    public void printHelp(CommandSource sender, String[] args) {
        Optional<PluginContainer> buc = plugin.server.getPluginManager().getPlugin("buc");
        if (buc.isPresent()) {
            sender.sendMessage(TextComponent.of("--------- buc " + buc.get().getDescription().getVersion().get() + " ---------").color(TextColor.AQUA));
        }
        sender.sendMessage(Messages.getTextComponent("command.help.whitelist_toggle", plugin.config.buc_command));
        sender.sendMessage(Messages.getTextComponent("command.help.whitelist_add", plugin.config.buc_command));
        sender.sendMessage(Messages.getTextComponent("command.help.whitelist_remove", plugin.config.buc_command));
        sender.sendMessage(Messages.getTextComponent("command.help.whitelist_reload", plugin.config.buc_command));
        sender.sendMessage(Messages.getTextComponent("command.help.ban", plugin.config.buc_command));
        sender.sendMessage(Messages.getTextComponent("command.help.tempban", plugin.config.buc_command));
        sender.sendMessage(Messages.getTextComponent("command.help.unban", plugin.config.buc_command));
        sender.sendMessage(Messages.getTextComponent("command.help.reload", plugin.config.buc_command));
    }

    private void banPlayer(CommandSource sender, UUID uuid, String name, String reason) {
        if (!plugin.userList.isBanned(uuid)) {
            plugin.userList.banUser(uuid, name, UserList.FORMAT.format(new Date()),
                    getSenderName(sender), "forever", reason);
            plugin.getLogger().info(plugin.userList.getUserByUUID(uuid).toString());
            plugin.kickPlayer(uuid, Messages.getTextComponent("messages.login.banned", reason));
            sender.sendMessage(Messages.getTextComponent("messages.ban.success", name, uuid.toString()));
            plugin.userList.save();
        } else {
            sender.sendMessage(Messages.getTextComponent("messages.ban.already_banned", name));
        }
    }

    private void tempbanPlayer(CommandSource sender, UUID uuid, String name, String reason, String banTime) {
        if (!plugin.userList.isBanned(uuid)) {
            Date date = stringToDate(banTime);
            if (date == null || date.before(new Date())) {
                sender.sendMessage(Messages.getTextComponent("messages.tempban.time_invalid", name));
                return;
            }
            plugin.userList.banUser(uuid, name, UserList.FORMAT.format(new Date()),
                    getSenderName(sender), UserList.FORMAT.format(date), reason);
            plugin.getLogger().info(plugin.userList.getUserByUUID(uuid).toString());
            plugin.kickPlayer(uuid, Messages.getTextComponent("messages.login.tempban", reason, UserList.FORMAT.format(date)));
            sender.sendMessage(Messages.getTextComponent("messages.ban.success", name, uuid.toString()));
            plugin.userList.save();
        } else {
            sender.sendMessage(Messages.getTextComponent("messages.ban.already_banned", name));
        }
    }

    private void unbanPlayer(CommandSource sender, UUID uuid, String name) {
        if (plugin.userList.isBanned(uuid)) {
            User user = plugin.userList.getUserByUUID(uuid);
            plugin.getLogger().info(user.toString());
            plugin.getLogger().info(Messages.get("log.unban", name, uuid, getSenderName(sender)));
            plugin.userList.unbanUser(user.getPlayerUUID());
            sender.sendMessage(Messages.getTextComponent("messages.unban.success", name));
            plugin.userList.save();
        } else {
            sender.sendMessage(Messages.getTextComponent("messages.unban.not_banned", name));
        }
    }

    private boolean addWhitelist(CommandSource sender, UUID uuid, String name) {
        if (!plugin.userList.isWhitelisted(uuid)) {
            plugin.userList.addWhitelist(uuid, name);
            plugin.getLogger().info(Messages.get("log.whitelist.add", name, uuid, getSenderName(sender)));
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

    public String getSenderName(CommandSource sender) {
        return (sender instanceof Player) ? ((Player) sender).getUsername() : "[CONSOLE]";
    }

    @Override
    public List<String> suggest(@NonNull CommandSource source, String[] currentArgs) {
        List<String> subCommands = Arrays.asList("help", "reload", "whitelist", "ban", "tempban", "unban");
        if (currentArgs.length == 0) {
            return subCommands;
        } else if (currentArgs.length == 1) {
            return subCommands.stream()
                    .filter(name -> name.regionMatches(true, 0, currentArgs[0], 0, currentArgs[0].length()))
                    .collect(Collectors.toList());
        } else {
            return ImmutableList.of();
        }
    }
}
