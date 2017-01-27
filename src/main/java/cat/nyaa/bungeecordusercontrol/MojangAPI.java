package cat.nyaa.bungeecordusercontrol;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.UUID;

public class MojangAPI {
    public static User getUserByName(String name) {
        try {
            URL url = new URL("https://api.mojang.com/profiles/minecraft");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
            Gson gson = new Gson();
            String[] strings = {name};
            outputStream.writeBytes(gson.toJson(strings));
            outputStream.flush();
            outputStream.close();
            if (conn.getResponseCode() != 200) {
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder builder = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) {
                builder.append(inputLine);
            }
            reader.close();
            JsonArray json = gson.fromJson(builder.toString(), JsonArray.class);
            UUID uuid = null;
            String playername = "";
            boolean demo = false;
            if (conn.getContentLength() > 4 && json != null && json.size() != 0) {
                JsonObject object = json.get(0).getAsJsonObject();
                uuid = object.has("id") ? (stringToUUID(object.get("id").getAsString())) : null;
                playername = object.has("name") ? object.get("name").getAsString() : "";
                demo = object.has("demo") && object.get("demo").getAsBoolean();
            }
            if (uuid == null || !playername.equalsIgnoreCase(name) || demo) {
                return null;
            }
            return new User(uuid, playername);
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static UUID stringToUUID(String uuid) {
        return UUID.fromString(uuid.substring(0, 8) + "-" +
                uuid.substring(8, 12) + "-" +
                uuid.substring(12, 16) + "-" +
                uuid.substring(16, 20) + "-" +
                uuid.substring(20, 32));
    }
}

abstract class Task extends Thread {

    public Task() {

    }

    @Override
    public abstract void run();
}