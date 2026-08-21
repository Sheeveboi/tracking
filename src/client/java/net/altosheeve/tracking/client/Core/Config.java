package net.altosheeve.tracking.client.Core;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Config {

    private static final File configFile = new File("trackingConfig.json");
    private static JSONObject configJson = new JSONObject();

    public static void createDefaultConfig() throws IOException {

        String defaultConfigJson = "{" +
                "   'waypoint' : {" +
                "       'allowedEntities' : {" +
                "           'players' : true," +
                "           'mobs'    : true," +
                "           'blocks'  : true," +
                "           'items'   : true" +
                "       }," +
                "       'packetRate' : 0" +
                "   }" +
                "}";

        JSONObject jsonFixer = new JSONObject(defaultConfigJson);

        FileWriter writer = new FileWriter("trackingConfig.json");
        writer.write(jsonFixer.toString());
        writer.close();

    }

    public static void updateFile() throws IOException {

        FileWriter writer = new FileWriter("trackingConfig.json");
        writer.write(configJson.toString());
        writer.close();

    }

    public static void loadJson() throws IOException {

        if (!configFile.exists()) createDefaultConfig();

        Scanner scanner = new Scanner(configFile);
        StringBuilder jsonString = new StringBuilder();
        while (scanner.hasNextLine()) jsonString.append(scanner.nextLine());

        configJson = new JSONObject(jsonString.toString());

    }


    public static void updateWaypointAllowedEntities(String entity, boolean enable) throws Exception {

        JSONObject waypointJson = configJson.getJSONObject("waypoint");
        JSONObject allowedEntities = waypointJson.getJSONObject("allowedEntities");

        if (!allowedEntities.has(entity)) throw new Exception("Entity type '" + entity + "' does not exist");

        allowedEntities.put(entity, enable);
        System.out.println(configJson);

        updateFile();

    }

    public static boolean getWaypointAllowedEntity(String entity) throws Exception {

        JSONObject allowedEntites = configJson.getJSONObject("waypoint")
                .getJSONObject("allowedEntities");

        if (!allowedEntites.has(entity)) throw new Exception("Entity type '" + entity + "' does not exist");

        return allowedEntites.getBoolean(entity);

    }

    public static float getPacketRate() throws IOException {

        if (!configJson.getJSONObject("waypoint").has("packetRate")) setPacketRate(0);

        return configJson.getJSONObject("waypoint").getFloat("packetRate");

    }

    public static void setPacketRate(float rate) throws IOException {

        configJson.getJSONObject("waypoint").put("packetRate", rate);

        updateFile();

    }

}
