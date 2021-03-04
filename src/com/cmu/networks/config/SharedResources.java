package com.cmu.networks.config;

import com.cmu.networks.models.ServerConfig;

import java.io.File;

public class SharedResources {
    public static ServerConfig serverConfig;
    public static File configFile;

    public static ServerConfig getServerConfig() { return serverConfig; }
    public static void setServerConfig(ServerConfig serverConfig) { SharedResources.serverConfig = serverConfig; }

    public static File getConfigFile() { return configFile; }
    public static void setConfigFile(File configFile) { SharedResources.configFile = configFile; }
}
