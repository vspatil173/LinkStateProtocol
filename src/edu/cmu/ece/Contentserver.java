package edu.cmu.ece;

import edu.cmu.ece.config.SharedResources;
import edu.cmu.ece.helper.ConfigFileIO;
import edu.cmu.ece.helper.Constants;
import edu.cmu.ece.helper.JsonParser;
import edu.cmu.ece.models.LinkStateMessage;
import edu.cmu.ece.services.CommandLineService;
import edu.cmu.ece.services.HeartBeatService;
import edu.cmu.ece.services.LinkStateBroadcasterService;
import edu.cmu.ece.services.ReceiverService;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;

public class Contentserver {

    public static void main(String[] args) {
        try {
            String filename = Constants.CONF_FILE;
            if(args.length >= 1){
                System.out.println(args[1]);
                filename = args[1];
            }
            File configFile = new File(filename);
            if(!configFile.exists()) {
                filename = Constants.CONF_FILE;
                configFile = new File(filename);
                if(!configFile.exists()) throw new IOException("Config file does not exists");
            }

            LinkedBlockingDeque<String> liveMsgs = new LinkedBlockingDeque<>();
            LinkedBlockingDeque<LinkStateMessage> linkStateMessages = new LinkedBlockingDeque<>();

            SharedResources.setConfigFile(configFile);
            SharedResources.setServerConfig(ConfigFileIO.parseFileToConfig());
            ConfigFileIO.writeToFileConfig(SharedResources.getServerConfig());

            new Thread(new ReceiverService(liveMsgs,linkStateMessages)).start();

            SharedResources.setCommandLineService(new CommandLineService());
            new Thread(SharedResources.getCommandLineService()).start();

            SharedResources.setHeartBeatService(new HeartBeatService(liveMsgs));
            new Thread(SharedResources.getHeartBeatService()).start();

            SharedResources.setLinkStateBroadcasterService(new LinkStateBroadcasterService(linkStateMessages));
            new Thread(SharedResources.getLinkStateBroadcasterService()).start();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
