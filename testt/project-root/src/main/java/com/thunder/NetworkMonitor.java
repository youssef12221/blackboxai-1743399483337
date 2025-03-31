package com.thunder;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NetworkMonitor {
    private static final Set<String> ALLOWED_SERVERS = Set.of(
        "mc.hypixel.net",
        "us.mineplex.com",
        "pvp.land"
    );
    
    private static final Set<String> BLOCKED_IPS = Set.of(
        "45.148.121.33", // Example cheat server IP
        "185.239.211.7"  // Another example
    );
    
    private static final int PORT_SCAN_INTERVAL = 30; // Seconds
    private static final Set<Integer> SUSPICIOUS_PORTS = Set.of(
        1337, 31337, 6666, 7777, 9999 // Common cheat client ports
    );
    
    private final Set<InetAddress> suspiciousConnections = ConcurrentHashMap.newKeySet();
    private final Map<Integer, Integer> portConnectionCounts = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    public NetworkMonitor() {
        startPortScanMonitor();
    }
    
    private void startPortScanMonitor() {
        scheduler.scheduleAtFixedRate(() -> {
            portConnectionCounts.entrySet().removeIf(entry -> {
                if (entry.getValue() > 10) { // More than 10 connections to this port
                    Logger.log("Suspicious port activity detected on port: " + entry.getKey());
                    return true;
                }
                return false;
            });
        }, PORT_SCAN_INTERVAL, PORT_SCAN_INTERVAL, TimeUnit.SECONDS);
    }
    
    public boolean checkNetwork() {
        try {
            // Check all network interfaces
            for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (iface.isLoopback() || !iface.isUp()) continue;
                
                // Check TCP connections
                for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
                    if (isSuspicious(addr)) {
                        Logger.log("Suspicious connection to: " + addr.getHostAddress());
                        return true;
                    }
                }
                
                // Check for suspicious port activity
                checkSuspiciousPorts(iface);
            }
            
            // Check 1.8-specific traffic patterns
            if (check1_8SpecificTraffic()) {
                return true;
            }
        } catch (SocketException e) {
            Logger.logError("Network monitoring error: " + e.getMessage());
        }
        return false;
    }
    
    private void checkSuspiciousPorts(NetworkInterface iface) {
        try {
            for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                for (int port : SUSPICIOUS_PORTS) {
                    try (Socket s = new Socket()) {
                        s.connect(new InetSocketAddress(addr.getAddress(), port), 100);
                        portConnectionCounts.merge(port, 1, Integer::sum);
                    } catch (IOException ignored) {
                        // Port not open
                    }
                }
            }
        } catch (Exception e) {
            Logger.logError("Port scan error: " + e.getMessage());
        }
    }
    
    private boolean isSuspicious(InetAddress addr) {
        String hostAddress = addr.getHostAddress();
        String hostName = addr.getHostName();
        
        // Check against blacklisted IPs
