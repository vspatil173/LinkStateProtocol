package edu.cmu.ece.services;

import edu.cmu.ece.config.SharedResources;
import edu.cmu.ece.helper.JsonParser;
import edu.cmu.ece.models.LinkStateMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Date;
import java.util.concurrent.BlockingDeque;

public class ReceiverService implements Runnable{

    private static final boolean DEBUG_MODE = true ;
    BlockingDeque<String> liveMsgs;
    BlockingDeque<LinkStateMessage> linkstateMsgs;
    boolean isRunning = true;
    public ReceiverService() {
    }

    public ReceiverService(BlockingDeque<String> liveMsgs, BlockingDeque<LinkStateMessage> linkstateMsgs) {
        this.liveMsgs = liveMsgs;
        this.linkstateMsgs = linkstateMsgs;
    }

    @Override
    public void run() {
        DatagramSocket dsock = null;
        try {
            dsock = new DatagramSocket(SharedResources.getServerConfig().getBackendPort());
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (isRunning) {
            try {
                byte[] arr1 = new byte[65527];
                DatagramPacket dpack = new DatagramPacket(arr1, arr1.length);
                dsock.receive(dpack);
                int packSize = dpack.getLength();
                String s2 = new String(dpack.getData(), 0, packSize);

                if(DEBUG_MODE)  System.out.println(new Date() + "  " + dpack.getAddress() + " @ " + dpack.getPort() + " = "+dpack.getLength()+":"+ s2);
                if(s2.startsWith("Live#")){
                    liveMsgs.push(s2);
                }else{
                    LinkStateMessage obj = JsonParser.generateMapFromString(s2);
                    linkstateMsgs.push(obj);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
