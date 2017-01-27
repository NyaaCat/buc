package cat.nyaa.bungeecordusercontrol;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserList {
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    public File userCacheFile;
    public File whitelistFile;
    public File bannedPlayersFile;
    private BUC plugin;
    private HashMap<UUID, User> userList = new HashMap<>();

    public UserList(BUC pl) {
        plugin = pl;
    }

    public boolean isEnableWhitelist() {
        return plugin.config.whitelist_enable;
    }

    public void setEnableWhitelist(boolean enableWhitelist) {
        plugin.config.whitelist_enable = enableWhitelist;
    }

    public File getFile(String filename) {
        File file = new File(plugin.getDataFolder(), filename);
        if (!file.isFile()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public void load() {
        userList.clear();
        userCacheFile = getFile("usercache.json");
        bannedPlayersFile = getFile("banned-players.json");
        whitelistFile = getFile("whitelist.json");
        loadUserCache();
        loadWhitelist();
        loadBannedlist();
    }

    public void save() {
        userCacheFile = getFile("usercache.json");
        bannedPlayersFile = getFile("banned-players.json");
        whitelistFile = getFile("whitelist.json");
        saveUserCache();
        saveWhitelist();
        saveBannedlist();
    }

    public void loadUserCache() {
        try {
            String jsonString = new String(Files.readAllBytes(userCacheFile.toPath()), StandardCharsets.UTF_8);
            JsonArray json = new Gson().fromJson(jsonString, JsonArray.class);
            if (json != null && json.size() > 0) {
                Iterator<JsonElement> iterator = json.iterator();
                while (iterator.hasNext()) {
                    JsonObject data = iterator.next().getAsJsonObject();
                    if (data == null) {
                        continue;
                    }
                    UUID uuid = data.has("uuid") ? UUID.fromString(data.get("uuid").getAsString()) : null;
                    String name = data.has("name") ? data.get("name").getAsString() : "";
                    if (uuid != null) {
                        addUser(uuid, name).setCacheExpires(data.has("expiresOn") ? data.get("expiresOn").getAsString() : "");
                    }
                }
            }
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void saveUserCache() {
        try {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(userCacheFile), "UTF-8"));
            writer.setIndent("  ");
            writer.beginArray();
            for (User user : userList.values()) {
                if (user.getPlayerUUID() != null) {
                    writer.beginObject();
                    writer.name("uuid").value(user.getPlayerUUID().toString());
                    writer.name("name").value(user.getPlayerName());
                    writer.name("expiresOn").value(user.getCacheExpires());
                    writer.endObject();
                }
            }
            writer.endArray();
            writer.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadWhitelist() {
        try {
            ArrayList<UUID> tmp = new ArrayList<>();
            String jsonString = new String(Files.readAllBytes(whitelistFile.toPath()), StandardCharsets.UTF_8);
            JsonArray json = new Gson().fromJson(jsonString, JsonArray.class);
            if (json != null && json.size() > 0) {
                for (JsonElement aJson : json) {
                    JsonObject jsonObject = aJson.getAsJsonObject();
                    if (jsonObject == null) {
                        continue;
                    }
                    UUID uuid = jsonObject.has("uuid") ? UUID.fromString(jsonObject.get("uuid").getAsString()) : null;
                    String name = jsonObject.has("name") ? jsonObject.get("name").getAsString() : "";
                    if (uuid != null) {
                        tmp.add(uuid);
                        addWhitelist(uuid, name);
                    }
                }
            }
            for (User user : userList.values()) {
                if (user.isWhitelisted() && !tmp.contains(user.getPlayerUUID())) {
                    removeWhitelist(user.getPlayerUUID());
                }
            }
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void saveWhitelist() {
        try {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(whitelistFile), "UTF-8"));
            writer.setIndent("  ");
            writer.beginArray();
            for (User user : userList.values()) {
                if (user.getPlayerUUID() != null && user.isWhitelisted()) {
                    writer.beginObject();
                    writer.name("uuid").value(user.getPlayerUUID().toString());
                    writer.name("name").value(user.getPlayerName());
                    writer.endObject();
                }
            }
            writer.endArray();
            writer.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadWhitelist() {
        loadWhitelist();
        saveWhitelist();
    }

    public void loadBannedlist() {
        try {
            ArrayList<UUID> tmp = new ArrayList<>();
            String jsonString = new String(Files.readAllBytes(this.bannedPlayersFile.toPath()), StandardCharsets.UTF_8);
            JsonArray json = new Gson().fromJson(jsonString, JsonArray.class);
            if (json != null && json.size() > 0) {
                for (JsonElement aJson : json) {
                    JsonObject jsonObject = aJson.getAsJsonObject();
                    if (jsonObject == null) {
                        continue;
                    }
                    UUID uuid = jsonObject.has("uuid") ? UUID.fromString(jsonObject.get("uuid").getAsString()) : null;
                    String name = jsonObject.has("name") ? jsonObject.get("name").getAsString() : "";
                    String created = jsonObject.has("created") ? jsonObject.get("created").getAsString() : "";
                    String source = jsonObject.has("source") ? jsonObject.get("source").getAsString() : "";
                    String expires = jsonObject.has("expires") ? jsonObject.get("expires").getAsString() : "forever";
                    if (expires.length() == 0) {
                        expires = "forever";
                    }
                    String reason = jsonObject.has("reason") ? jsonObject.get("reason").getAsString() : "";
                    if (uuid != null) {
                        tmp.add(uuid);
                        banUser(uuid, name, created, source, expires, reason);
                    }
                }
            }
            for (User user : userList.values()) {
                if (user.isBanned() && !tmp.contains(user.getPlayerUUID())) {
                    unbanUser(user.getPlayerUUID());
                }
            }
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void saveBannedlist() {
        try {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(bannedPlayersFile), "UTF-8"));
            writer.setIndent("  ");
            writer.beginArray();
            for (User user : userList.values()) {
                if (user.getPlayerUUID() != null && user.isBanned()) {
                    writer.beginObject();
                    writer.name("uuid").value(user.getPlayerUUID().toString());
                    writer.name("name").value(user.getPlayerName());
                    writer.name("created").value(user.getBanCreated());
                    writer.name("source").value(user.getBanSource());
                    writer.name("expires").value(user.getBanExpires());
                    writer.name("reason").value(user.getBanReason());
                    writer.endObject();
                }
            }
            writer.endArray();
            writer.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getUserByUUID(UUID uuid) {
        if (userList.containsKey(uuid)) {
            return userList.get(uuid);
        }
        return null;
    }

    public User getUserByName(String name) {
        for (User user : userList.values()) {
            if (name.equalsIgnoreCase(user.getPlayerName())) {
                return user;
            }
        }
        return null;
    }

    public User addUser(UUID uuid, String name) {
        if (userList.containsKey(uuid)) {
            updateUserName(uuid, name);
        } else {
            userList.put(uuid, new User(uuid, name));
            updateUserCache(uuid, name);
        }
        return userList.get(uuid);
    }

    public void updateUserName(UUID uuid, String name) {
        if (userList.containsKey(uuid)) {
            User user = getUserByUUID(uuid);
            if (!user.getPlayerName().equals(name)) {
                user.setPlayerName(name);
            }
        }
    }

    public void updateUserCache(UUID uuid, String name) {
        if (!userList.containsKey(uuid)) {
            addUser(uuid, name);
        }
        User user = getUserByUUID(uuid);
        if (!user.getPlayerName().equals(name)) {
            user.setPlayerName(name);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        user.setCacheExpires(FORMAT.format(calendar.getTime()));
    }

    public boolean isWhitelisted(UUID uuid) {
        if (userList.containsKey(uuid)) {
            return getUserByUUID(uuid).isWhitelisted();
        }
        return false;
    }

    public void addWhitelist(UUID uuid, String PlayerName) {
        User user = getUserByUUID(uuid);
        if (user == null) {
            user = addUser(uuid, PlayerName);
        }
        user.setWhitelisted(true);
    }

    public boolean removeWhitelist(UUID uuid) {
        User user = getUserByUUID(uuid);
        if (user != null && user.isWhitelisted()) {
            user.setWhitelisted(false);
            return true;
        }
        return false;
    }

    public boolean isBanned(UUID uuid) {
        if (userList.containsKey(uuid)) {
            return getUserByUUID(uuid).isBanned();
        }
        return false;
    }

    public boolean banExpires(UUID uuid) {
        User user = getUserByUUID(uuid);
        if (user == null) {
            return true;
        }
        try {
            if (user.getBanExpires().length() == 0 || user.getBanExpires().equalsIgnoreCase("forever")) {
                return false;
            }
            return FORMAT.parse(user.getBanExpires()).getTime() < new Date().getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unbanUser(UUID uuid) {
        User user = getUserByUUID(uuid);
        if (user != null && user.isBanned()) {
            user.setBanned(false);
            user.setBanCreated("");
            user.setBanSource("");
            user.setBanExpires("");
            user.setBanReason("");
            return true;
        }
        return false;
    }

    public boolean banUser(UUID uuid, String name, String created, String source, String expires, String reason) {
        User user = getUserByUUID(uuid);
        if (user == null) {
            user = addUser(uuid, name);
        }
        if (user != null) {
            user.setBanned(true);
            user.setPlayerName(name);
            user.setBanCreated(created);
            user.setBanSource(source);
            user.setBanExpires(expires);
            user.setBanReason(reason);
            return true;
        }
        return false;
    }
}
