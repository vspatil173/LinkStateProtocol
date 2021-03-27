package edu.cmu.ece.services;

import edu.cmu.ece.config.SharedResources;
import edu.cmu.ece.helper.ConfigFileIO;
import edu.cmu.ece.helper.Constants;

import java.util.Scanner;

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
                System.out.println("node state saved and shutdown completed");
                killService();
            }else if(inputLine.startsWith(Constants.ENABLE_METRIC)){
                SharedResources.getServerConfig().enableActiveMetric();
            }else if(inputLine.startsWith(Constants.DISABLE_METRIC)){
                SharedResources.getServerConfig().disableActiveMetric();
            }else if(inputLine.isEmpty()){
                //empty
            }else{
                System.out.println("invalid command :"+inputLine);
            }
        }
    }

    public void killService(){
        isRunning = false;
    }
}
