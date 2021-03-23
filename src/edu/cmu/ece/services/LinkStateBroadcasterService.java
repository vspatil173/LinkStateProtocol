package edu.cmu.ece.services;

import edu.cmu.ece.config.SharedResources;
import edu.cmu.ece.models.LinkStateMessage;
import edu.cmu.ece.models.Peer;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;

public class LinkStateBroadcasterService implements Runnable {

    boolean isRunning = true;
    final boolean DEBUG_MODE=true;
    final int SLEEP_TIME = 50000;
    BlockingDeque<LinkStateMessage> linkstateMsgs;

    public LinkStateBroadcasterService(BlockingDeque<LinkStateMessage> linkstateMsgs) {
        this.linkstateMsgs = linkstateMsgs;
    }

    @Override
    public void run() {
        new Thread(this::sendLinkStateMessage).start();
        new Thread(this::receiveLinkStateMessage).start();
    }

    private void sendLinkStateMessage() {
        InetAddress add = null;
        DatagramSocket dsock = null;
        try {
            dsock = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println(e.getMessage());
        }

        while (isRunning) {
            try {
                //construct the mressage here and handle the updates as well
                byte[] arr = SharedResources.getServerConfig().getLinkStateMessage().toString().getBytes();
                SharedResources.getServerConfig().getLinkStateMessage().increment_Sequence_Number();

                for (int i = 0; i < SharedResources.getServerConfig().getPeers().size(); i++) {
                    Peer peer = SharedResources.getServerConfig().getPeers().get(i);
                    if (!peer.isActive()) continue;
                    add = InetAddress.getByName(peer.getIp());
                    DatagramPacket dpack = new DatagramPacket(arr, arr.length, add, peer.getPort());
                    try {
                        System.out.println("sending the message to all peers");
                        dsock.send(dpack);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            } catch (UnknownHostException e) { }
        }
    }

    private void receiveLinkStateMessage() {
        try {

            while (isRunning) {
                LinkStateMessage s2 = linkstateMsgs.take();
                if(DEBUG_MODE) System.out.println("Linkstate adv received: "+s2);
//                update the records if the sequence number is greater else ignore the message
//                if the sequence number is correct then then the message to all peers but not the original sender

                Thread.sleep((long)(SLEEP_TIME*.90));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void killService(){
        isRunning = false;
    }
}
