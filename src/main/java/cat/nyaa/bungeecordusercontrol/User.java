package cat.nyaa.bungeecordusercontrol;


import java.util.UUID;

public class User {
    private UUID playerUUID;
    private String playerName = "";
    private String cacheExpires = "";
    private boolean whitelisted = false;
    private boolean banned = false;
    private String banCreated = "";
    private String banSource = "";
    private String banExpires = "";
    private String banReason = "";
    private boolean shadowBaned = false;

    public User(UUID uuid) {
        playerUUID = uuid;
    }

    public User(UUID uuid, String playerName) {
        this.setPlayerUUID(uuid);
        this.setPlayerName(playerName);
    }

    public User(UUID uuid, String name, boolean whitelisted, boolean banned) {
        playerUUID = uuid;
        playerName = name;
        this.whitelisted = whitelisted;
        this.banned = banned;
    }

    public String getCacheExpires() {
        return cacheExpires;
    }

    public void setCacheExpires(String cacheExpires) {
        this.cacheExpires = cacheExpires;
    }

    public String getBanCreated() {
        return banCreated;
    }

    public void setBanCreated(String banCreated) {
        this.banCreated = banCreated;
    }

    public String getBanSource() {
        return banSource;
    }

    public void setBanSource(String banSource) {
        this.banSource = banSource;
    }

    public String getBanExpires() {
        return banExpires;
    }

    public void setBanExpires(String banExpires) {
        this.banExpires = banExpires;
    }

    public String getBanReason() {
        return banReason;
    }

    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public boolean isWhitelisted() {
        return whitelisted;
    }

    public void setWhitelisted(boolean whitelisted) {
        this.whitelisted = whitelisted;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public boolean isShadowBaned() {
        return shadowBaned;
    }

    public void setShadowBaned(boolean shadowed){
        this.shadowBaned = shadowed;
    }

    @Override
    public String toString() {
        String str = "User{";
        str += "name=" + getPlayerName();
        str += ", uuid=" + getPlayerUUID().toString();
        str += ", cacheExpires=" + getCacheExpires();
        str += ", whitelisted=" + isWhitelisted();
        str += ", banned=" + isBanned();
        str += ", shadowBaned=" + isShadowBaned();
        if (isBanned()) {
            str += ", banCreated=" + getBanCreated();
            str += ", banSource=" + getBanSource();
            str += ", banReason=" + getBanReason();
            str += ", banExpires=" + getBanExpires();
        }
        str += "}";
        return str;
    }

}
