package com.thunder;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import java.util.List;
import netscape.javascript.JSObject;
import javafx.concurrent.Worker;

public class ThunderMainApp extends Application {
    private WebView webView;
    private ThunderLauncher launcher;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialize components
        launcher = new ThunderLauncher();

        // Set up WebView for HTML UI
        webView = new WebView();
        loadMainPage();

        // Enable JavaScript bridge
        JSBridge bridge = new JSBridge(this);
        webView.getEngine().getLoadWorker().stateProperty().addListener(
            (obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) webView.getEngine().executeScript("window");
                    window.setMember("java", bridge);
                }
            });

        Scene scene = new Scene(webView, 900, 600);
        primaryStage.setTitle("Thunder Client - Minecraft 1.8");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void loadMainPage() {
        webView.getEngine().load(getClass().getResource("/static/index.html").toExternalForm());
    }

    public void loadSettingsPage() {
        webView.getEngine().load(getClass().getResource("/static/settings.html").toExternalForm());
    }

    public List<String> getLaunchCommand(String username) {
        return launcher.getLaunchCommand(username);
    }

    public String getVersion() {
        return "1.8.0"; // Should match minecraft_1.8.json version
    }

    public static void main(String[] args) {
        launch(args);
    }
}