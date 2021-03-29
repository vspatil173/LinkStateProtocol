package edu.cmu.ece.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cmu.ece.config.SharedResources;
import edu.cmu.ece.helper.ConfigFileIO;
import edu.cmu.ece.helper.Constants;
import edu.cmu.ece.helper.Optimizer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CommandLineService implements Runnable{
    boolean isRunning = true;
    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        String inputLine;
        while(isRunning){
            inputLine = sc.nextLine();
            if(inputLine.startsWith(Constants.CMD_UUID)){
                System.out.println(SharedResources.getServerConfig().getUuid());
            }else if(inputLine.startsWith(Constants.CMD_NEIGHBORS)){
                System.out.println(SharedResources.getServerConfig().printListOfNeighbor());
            }else if(inputLine.startsWith(Constants.CMD_ADD_NEIGHBOR)){
                boolean isNeighborAdded = ConfigFileIO.addNeighbor(inputLine);
                if(isNeighborAdded)
                    System.out.println("neighbor added");
                else
                    System.out.println("Failure: adding neighbor - invalid syntax or duplicate entry found");
            }else if(inputLine.startsWith(Constants.CMD_NETSTAT)){
                System.out.println(SharedResources.getServerConfig());
            }
            else if(inputLine.startsWith(Constants.CMD_KILL)){
                System.out.println("Shutting down node");
                ((HeartBeatService)SharedResources.getHeartBeatService()).killService();
                ((LinkStateBroadcasterService)SharedResources.getLinkStateBroadcasterService()).killService();
                System.out.println("node state saved and shutdown completed");
                killService();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) { e.printStackTrace(); }
                System.exit(0);
            }else if(inputLine.startsWith(Constants.ENABLE_METRIC)){
                SharedResources.getServerConfig().enableActiveMetric();
                ConfigFileIO.writeToFileConfig(SharedResources.getServerConfig());
            }else if(inputLine.startsWith(Constants.DISABLE_METRIC)){
                SharedResources.getServerConfig().disableActiveMetric();
                ConfigFileIO.writeToFileConfig(SharedResources.getServerConfig());
            }else if(inputLine.startsWith(Constants.MAP)){
                maphandler();
            }else if(inputLine.startsWith(Constants.RANK)){
                rankhandler();
            }else if(inputLine.startsWith(Constants.ENABLE_DEBUG)){
                Optimizer.setDebugMode(true);
                ((HeartBeatService)SharedResources.getHeartBeatService()).setDEBUG_MODE(true);
                ((LinkStateBroadcasterService)SharedResources.getLinkStateBroadcasterService()).setDEBUG_MODE(true);
            }else if(inputLine.startsWith(Constants.DISABLE_DEBUG)){
                Optimizer.setDebugMode(false);
                ((HeartBeatService)SharedResources.getHeartBeatService()).setDEBUG_MODE(false);
                ((LinkStateBroadcasterService)SharedResources.getLinkStateBroadcasterService()).setDEBUG_MODE(false);
            }else if(inputLine.isEmpty()){
                //empty
            }else{
                System.out.println("invalid command :"+inputLine);
            }
        }
    }

    private void rankhandler() {
        Map<String,Double> hostmap = SharedResources.getServerConfig().getLinkStateMessage()
                .getDistance_vector().get(SharedResources.serverConfig.getHostName());
        Object[] a = hostmap.entrySet().toArray();
        Arrays.sort(a,new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Map.Entry<String, Double>) o1).getValue()
                        .compareTo(((Map.Entry<Double, Double>) o2).getValue());
            }
        });
        List<Map.Entry<String, Double>> list =
                new LinkedList<>();
        for(Object e :a){
            list.add((Map.Entry<String,Double>)e);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void maphandler(){
        HashMap<String, HashMap<String,Double>> map = new HashMap<>();
        HashMap<String,Double> cur;
        Set<String> liveList = new HashSet<>();
        for( Map.Entry<String,List<String>> cur1: SharedResources.getServerConfig().getDirect_peer_map().entrySet()){
            cur1.getValue().forEach(s -> {
                liveList.add(cur1.getKey());
            });
        }

        for(Map.Entry<String, List<String>> entry : SharedResources.getServerConfig().getDirect_peer_map().entrySet()){
            cur = new HashMap<>();
            for(String node:entry.getValue()){
                double dist = SharedResources.getServerConfig()
                        .getLinkStateMessage().getDistance_vector()
                        .get(entry.getKey()).get(node);
//                if(SharedResources.getServerConfig().getDirect_peer_map().get(entry.getKey()).contains(entry.getKey()))
                if(liveList.contains(node))
                    cur.put(node,dist);
            }
            map.put(entry.getKey(),cur);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void killService(){
        isRunning = false;
    }
}
