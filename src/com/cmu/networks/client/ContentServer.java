package com.cmu.networks.client;

import com.cmu.networks.config.SharedResources;
import com.cmu.networks.helper.ConfigFileIO;
import com.cmu.networks.services.CommandLineService;
import com.cmu.networks.services.HeartBeatService;

import java.io.File;
import java.io.IOException;

public class ContentServer {

    public static void main(String[] args) {
        try {
            File configFile = new File("resources/node.conf");
            if(!configFile.exists()) throw new IOException("Config file does not exists");

            SharedResources.setConfigFile(configFile);
            SharedResources.setServerConfig(ConfigFileIO.parseFileToConfig());
            ConfigFileIO.writeToFileConfig(SharedResources.getServerConfig());

            SharedResources.setCommandLineService(new CommandLineService());
            new Thread(SharedResources.getCommandLineService()).start();

            SharedResources.setHeartBeatService(new HeartBeatService());
            new Thread(SharedResources.getHeartBeatService()).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
