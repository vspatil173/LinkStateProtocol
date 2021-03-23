package edu.cmu.ece.services;

import edu.cmu.ece.config.SharedResources;
import edu.cmu.ece.models.Peer;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;

public class HeartBeatService implements Runnable{

    boolean isRunning = true;
    Map<String,Integer> currState;
    Map<String,Integer> prevState;
    final int TTL=2;
    final boolean DEBUG_MODE=true;

    BlockingDeque<String> liveMsgs;

    public HeartBeatService(BlockingDeque<String> liveMsgs) {
        this.liveMsgs = liveMsgs;
    }

    @Override
    public void run() {
        currState = new ConcurrentHashMap<>();
        prevState = new ConcurrentHashMap<>();
        SharedResources.getServerConfig().getPeers().forEach(peer -> {
            currState.put(peer.getUuid(),0);
            prevState.put(peer.getUuid(),-1);
        });
        new Thread(this::sendHeartBeats).start();
        new Thread(this::receiveHeartBeats).start();
    }

    public void sendHeartBeats() {
        InetAddress add = null;
        DatagramSocket dsock = null;
        try {
            dsock = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        int sync=0;
        while (isRunning) {
            try {
                for (int i = 0; i < SharedResources.getServerConfig().getPeers().size(); i++) {
                    Peer peer = SharedResources.getServerConfig().getPeers().get(i);
                    if (!peer.isActive()) continue;
                    if (currState.containsKey(peer.getUuid()) && prevState.containsKey(peer.getUuid())) {
                        if (currState.get(peer.getUuid()).equals(-1)) {
                            peer.setActive(false);
                            System.out.println(new Date() + "  " + peer.getUuid() + " got disconnected");
                        } else if (prevState.get(peer.getUuid()) == -1 && currState.get(peer.getUuid()) == 0) {
                            //initial condition
                            currState.put(peer.getUuid(), currState.get(peer.getUuid()) - 1);
                        } else {
                            //penalty
                            currState.put(peer.getUuid(), currState.get(peer.getUuid()) - 1);
                            prevState.put(peer.getUuid(), prevState.get(peer.getUuid()) - 1);
                        }
                    } else {
                        currState.put(peer.getUuid(), 0);
                        prevState.put(peer.getUuid(), -1);
                    }

                    add = InetAddress.getByName(peer.getIp());
                    String message1 = "Live#" + sync + ":" + SharedResources.getServerConfig().getUuid()+":"+SharedResources.getServerConfig().getHostName();
                    byte[] arr = message1.getBytes();
                    DatagramPacket dpack = new DatagramPacket(arr, arr.length, add, peer.getPort());
                    try {
//                        System.out.println(sync);
                        dsock.send(dpack);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Thread.sleep(10000);
                sync++;
                sync %= 1024;
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            } catch (UnknownHostException e) { }
        }
    }

    public void receiveHeartBeats(){
        try {
//            DatagramSocket dsock = new DatagramSocket(SharedResources.getServerConfig().getBackendPort());
            while (isRunning) {
                String s2 = liveMsgs.take();
                String uuid = s2.split(":")[1];
                if(DEBUG_MODE) System.out.println("heartbeat received: "+s2);
                if (currState.containsKey(uuid) && prevState.containsKey(uuid)) {
                    Peer per =SharedResources.getServerConfig().getPeers().stream().filter(peer -> peer.getUuid().equals(uuid)).findFirst().orElse(null);
                    if(currState.get(uuid)==-1 && !per.isActive()) System.out.println("Node with "+uuid+" Connected again");
                    if (currState.get(uuid) < TTL) {
                        prevState.put(uuid, currState.get(uuid));
                        currState.put(uuid, currState.get(uuid) + 1);
                        if(per!=null) per.setActive(true);
                    }
                } else {
                    currState.put(uuid, 0);
                    prevState.put(uuid, -1);
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void killService(){
        isRunning = false;
    }
}
