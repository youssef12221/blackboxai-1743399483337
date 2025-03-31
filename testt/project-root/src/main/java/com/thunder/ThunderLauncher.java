package com.thunder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ThunderLauncher {
    private static final String CONFIG_FILE = "/config/minecraft_1.8.json";
    private JSONObject config;

    public ThunderLauncher() {
        loadConfig();
    }

    private void loadConfig() {
        try (InputStream is = getClass().getResourceAsStream(CONFIG_FILE)) {
            config = new JSONObject(new JSONTokener(is));
        } catch (Exception e) {
            Logger.getLogger(ThunderLauncher.class.getName()).severe("Failed to load Minecraft 1.8 config: " + e.getMessage());
        }
    }

    public List<String> getLaunchCommand(String username) {
        List<String> command = new ArrayList<>();
        command.add("java");

        // Add JVM arguments from config
        JSONArray jvmArgs = config.getJSONArray("jvmArgs");
        for (int i = 0; i < jvmArgs.length(); i++) {
            command.add(jvmArgs.getString(i));
        }

        // Add libraries
        command.add("-cp");
        StringBuilder classpath = new StringBuilder();
        JSONArray libraries = config.getJSONArray("libraries");
        for (int i = 0; i < libraries.length(); i++) {
            JSONObject lib = libraries.getJSONObject(i);
            String path = getLibraryPath(lib.getString("name"));
            if (!path.isEmpty()) {
                classpath.append(path).append(":");
            }
        }
        classpath.append("minecraft.jar");
        command.add(classpath.toString());

        // Add main class and game arguments
        command.add(config.getString("mainClass"));
        command.add("--version");
        command.add(config.getString("version"));
        command.add("--username");
        command.add(username);
        command.add("--assetsDir");
        command.add("assets");
        command.add("--assetIndex");
        command.add(config.getString("assets"));
        command.add("--accessToken");
        command.add(AuthManager.getAccessToken()); // Get stored or generated token
        command.add("--userProperties");
        command.add("{}");
        command.add("--gameDir");
        command.add(".");

        return command;
    }

    private String getLibraryPath(String name) {
        // Convert library name to path (e.g., "net.minecraft:launchwrapper:1.12" -> "libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar")
        String[] parts = name.split(":");
        return String.format("libraries/%s/%s/%s/%s-%s.jar",
            parts[0].replace('.', '/'),
            parts[1],
            parts[2],
            parts[1],
            parts[2]);
    }

    public String getVersion() {
        return config.getString("version");
    }
}