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

    boolean DEBUG_MODE = false ;
    BlockingDeque<String> liveMsgs;
    BlockingDeque<DatagramPacket> linkstateMsgs;
    boolean isRunning = true;
    public ReceiverService() {
    }

    public ReceiverService(BlockingDeque<String> liveMsgs, BlockingDeque<DatagramPacket> linkstateMsgs) {
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
                }else if(s2.startsWith("OUT_OF_SYNC")){
                    if(DEBUG_MODE)  System.out.println(new Date() + "  " + dpack.getAddress() + " @ " + dpack.getPort() + " = "+dpack.getLength()+":"+ s2);
                    long suggested_seq_no = Long.parseLong(s2.split(":")[1]);
                    long current_seq_no = SharedResources.getServerConfig().getLinkStateMessage().getSequence_number();
                    if(suggested_seq_no >= current_seq_no){
                        SharedResources.getServerConfig().getLinkStateMessage().setSequence_number(suggested_seq_no+1);
                    }
                }else{
                    linkstateMsgs.push(dpack);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setDEBUG_MODE(boolean DEBUG_MODE) {
        this.DEBUG_MODE = DEBUG_MODE;
    }
}
