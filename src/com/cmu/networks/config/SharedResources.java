package com.cmu.networks.config;

import com.cmu.networks.models.ServerConfig;

import java.io.File;

public class SharedResources {
    public static ServerConfig serverConfig;
    public static File configFile;
    public static Runnable commandLineService;
    public static Runnable heartBeatService;

    public static ServerConfig getServerConfig() { return serverConfig; }
    public static void setServerConfig(ServerConfig serverConfig) { SharedResources.serverConfig = serverConfig; }

    public static File getConfigFile() { return configFile; }
    public static void setConfigFile(File configFile) { SharedResources.configFile = configFile; }

    public static Runnable getCommandLineService() { return commandLineService; }
    public static void setCommandLineService(Runnable commandLineService) { SharedResources.commandLineService = commandLineService; }

    public static Runnable getHeartBeatService() { return heartBeatService; }
    public static void setHeartBeatService(Runnable heartBeatService) { SharedResources.heartBeatService = heartBeatService; }
}
