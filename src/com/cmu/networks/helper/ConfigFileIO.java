package com.cmu.networks.helper;

import com.cmu.networks.config.SharedResources;
import com.cmu.networks.models.ServerConfig;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class ConfigFileIO {

    public static ServerConfig parseFileToConfig(){
        FileReader reader = null;
        Map<String,String> mapFromFile = new HashMap<>();
        List<String> node_list = new LinkedList<>();
        boolean isPeerStarted = false;
        try {
            reader = new FileReader(SharedResources.getConfigFile());
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splits = line.split(" = ");
                if(!isPeerStarted)
                    mapFromFile.put(splits[0],splits[1]);
                else
                    node_list.add(line);
                if(splits[0].startsWith(Constants.PEER_COUNT))
                    isPeerStarted = true;
            }
        } catch (IOException e) { e.printStackTrace(); }
        finally {
            try {
                assert reader != null; reader.close();
            } catch (IOException e) {e.printStackTrace();}
        }

        ServerConfig config = new ServerConfig();
        if(mapFromFile.containsKey(Constants.UUID))
            config.setUuid(mapFromFile.get(Constants.UUID));
        else
            config.setUuid(UUID.randomUUID().toString());

        if(mapFromFile.containsKey(Constants.NAME))
            config.setHostName(mapFromFile.get(Constants.NAME));

        if(mapFromFile.containsKey(Constants.PORT))
            config.setBackendPort(Integer.parseInt(mapFromFile.get(Constants.PORT)));

        if(mapFromFile.containsKey(Constants.PEER_COUNT)){
            config.setPeer_count(Integer.parseInt(mapFromFile.get(Constants.PEER_COUNT)));
            for (String s : node_list) {
                config.addPeerFromString(s);
            }
        }

        System.out.println(config);
        return config;
    }

    public static void writeToFileConfig(ServerConfig serverConfig){
        BufferedWriter bufferedWriter = null;
        FileWriter writer = null;
        try {
            writer = new FileWriter(SharedResources.getConfigFile());
            bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(serverConfig.toString());
        } catch (IOException e) { e.printStackTrace(); }
        finally {
            try {
                assert bufferedWriter != null;
                bufferedWriter.close();
                writer.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}
