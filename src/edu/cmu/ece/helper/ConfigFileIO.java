package edu.cmu.ece.helper;

import edu.cmu.ece.config.SharedResources;
import edu.cmu.ece.models.LinkStateMessage;
import edu.cmu.ece.models.Peer;
import edu.cmu.ece.models.ServerConfig;

import java.io.*;
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
        else
            config.setHostName("node"+(new Random()).nextInt(10000));

        if(mapFromFile.containsKey(Constants.PORT))
            config.setBackendPort(Integer.parseInt(mapFromFile.get(Constants.PORT)));

        if(mapFromFile.containsKey(Constants.ACTIVE_METRIC))
            config.setActive_metric(Integer.parseInt(mapFromFile.get(Constants.ACTIVE_METRIC)));

        if(mapFromFile.containsKey(Constants.PEER_COUNT)){
            config.setPeer_count(Integer.parseInt(mapFromFile.get(Constants.PEER_COUNT)));
            for (String s : node_list) {
                config.addPeerFromString(s);
            }
        }
        config.setLinkStateMessage(generateLinkStateMessage(config));
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


    public static boolean addNeighbor(String input){
        Map<String,String> paraMaps = new HashMap<>();
        for(String paras : input.split(" ")) {
            if(paras.equals("addneighbor")) continue;
            paraMaps.put(paras.split("=")[0],paras.split("=")[1]);
        }

        String pid = Constants.PEER_PREFIX+(SharedResources.getServerConfig().getPeer_count());
        if(paraMaps.containsKey(Constants.UUID) && paraMaps.containsKey(Constants.HOST) &&
                paraMaps.containsKey(Constants.PORT) && paraMaps.containsKey(Constants.METRIC)){

            Peer curPeer = new Peer(pid,paraMaps.get(Constants.UUID),
                    paraMaps.get(Constants.HOST),Integer.parseInt(paraMaps.get(Constants.PORT)),
                    Integer.parseInt(paraMaps.get(Constants.METRIC)));

            if(checkForDuplicatePeer(curPeer)){
                return false;
            }
            SharedResources.getServerConfig().addPeer(curPeer);
            ConfigFileIO.writeToFileConfig(SharedResources.getServerConfig());
        }else{
            return false;
        }
        return true;
    }

    private static boolean checkForDuplicatePeer(Peer curPeer) {

        Peer duplicateRecord = SharedResources.getServerConfig().getPeers().stream()
                .filter(peer -> peer.getUuid().equals(curPeer.getUuid())).findFirst().orElse(null);
        return duplicateRecord != null;
    }

    public static LinkStateMessage generateLinkStateMessage(ServerConfig config){
        String nodeName = config.getHostName();
        LinkStateMessage linkStateMessage = config.getLinkStateMessage()!=null? config.getLinkStateMessage() : new LinkStateMessage(nodeName);
        linkStateMessage.setDirect_peers(new LinkedList<>());
        List<String> peers = new LinkedList<>();
        config.getPeers().forEach(peer -> {
            if(peer.isActive() && config.getUuidToAlias().containsKey(peer.getUuid())){
                linkStateMessage.getDirect_peers().add(config.getUuidToAlias().get(peer.getUuid()));
                peers.add(config.getUuidToAlias().get(peer.getUuid()));
            }
        });
        config.getDirect_peer_map().put(nodeName,peers);
        config.getPeers().forEach(peer -> {
            if(config.getUuidToAlias().containsKey(peer.getUuid())){
                linkStateMessage.addDistanceVectorDataForNode(
                        nodeName,
                        config.getUuidToAlias().get(peer.getUuid()),
                        config.isActiveMetric() ? 0.0: (double) peer.getDistance()
                );
            }
        });
        return  linkStateMessage;
    }
}
