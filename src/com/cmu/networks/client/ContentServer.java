package com.cmu.networks.client;

import com.cmu.networks.config.SharedResources;
import com.cmu.networks.helper.ConfigFileIO;
import com.cmu.networks.models.ServerConfig;
import com.cmu.networks.services.CommandLineService;

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

            Thread commandLine = new Thread( new CommandLineService());
            commandLine.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
