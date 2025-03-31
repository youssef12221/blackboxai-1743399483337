package com.thunder;

import netscape.javascript.JSObject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class JSBridge {
    private static final Logger LOGGER = Logger.getLogger(JSBridge.class.getName());
    private final ThunderMainApp app;

    public JSBridge(ThunderMainApp app) {
        this.app = app;
    }

    public void openSettings() {
        app.loadSettingsPage();
    }

    public void startGame(String username) {
        if (username != null && !username.trim().isEmpty()) {
            try {
                List<String> command = app.getLaunchCommand(username);
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.directory(new File(".")); // Set working directory
                Process process = pb.start();
                
                // Log launch success
                LOGGER.info("Launched Minecraft " + app.getVersion() + " for " + username);
            } catch (IOException e) {
                LOGGER.severe("Failed to launch game: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void loadMainPage() {
        app.loadMainPage();
    }

    public void refreshAuthToken() {
        AuthManager.clearToken();
        LOGGER.info("Authentication token reset");
        // Show confirmation to user
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Authentication");
            alert.setHeaderText("Token Reset");
            alert.setContentText("A new authentication token will be generated on next launch.");
            alert.show();
        });
    }
}