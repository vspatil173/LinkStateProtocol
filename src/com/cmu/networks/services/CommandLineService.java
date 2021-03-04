package com.cmu.networks.services;

import com.cmu.networks.config.SharedResources;
import com.cmu.networks.helper.ConfigFileIO;
import com.cmu.networks.helper.Constants;

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
                System.out.println(ConfigFileIO.addNeighbor(inputLine));
            }else if(inputLine.startsWith(Constants.CMD_NETSTAT)){
                System.out.println(SharedResources.getServerConfig());
            }
            else if(inputLine.startsWith(Constants.CMD_KILL)){
                System.out.println("Shutting down node");
                ((HeartBeatService)SharedResources.getHeartBeatService()).killService();
                System.out.println("node state saved and shutdown completed");
                killService();
            }
            else{
                System.out.println("invalid command :"+inputLine);
            }
        }
    }

    public void killService(){
        isRunning = false;
    }
}
