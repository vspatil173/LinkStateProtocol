package edu.cmu.ece.services;

import edu.cmu.ece.config.SharedResources;
import edu.cmu.ece.helper.ConfigFileIO;
import edu.cmu.ece.helper.JsonParser;
import edu.cmu.ece.helper.Optimizer;
import edu.cmu.ece.models.LinkStateMessage;
import edu.cmu.ece.models.Peer;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;

public class LinkStateBroadcasterService implements Runnable {

    boolean isRunning = true;
    final boolean DEBUG_MODE = false;
    final boolean _TEMP_DEBUG_MODE = true;
    final int SLEEP_TIME = 50000;
    BlockingDeque<DatagramPacket> linkstateMsgs;

    public LinkStateBroadcasterService(BlockingDeque<DatagramPacket> linkstateMsgs) {
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
                //construct the message here and handle the updates as well
                SharedResources.getServerConfig().setLinkStateMessage(ConfigFileIO.generateLinkStateMessage(SharedResources.getServerConfig()));
                byte[] arr = SharedResources.getServerConfig().getLinkStateMessage().toString().getBytes();
                SharedResources.getServerConfig().getLinkStateMessage().increment_Sequence_Number();
                for (int i = 0; i < SharedResources.getServerConfig().getPeers().size(); i++) {
                    Peer peer = SharedResources.getServerConfig().getPeers().get(i);
                    if (!peer.isActive()) continue;
                    add = InetAddress.getByName(peer.getIp());
                    DatagramPacket dpack = new DatagramPacket(arr, arr.length, add, peer.getPort());
                    try {
                        if (DEBUG_MODE)
                            System.out.println("S@[" + SharedResources.getServerConfig().getHostName() + "] -> ["
                                    + SharedResources.getServerConfig().getUuidToAlias().get(peer.getUuid()) + "]");
                        dsock.send(dpack);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            } catch (UnknownHostException e) {
            }
        }
    }

    private void receiveLinkStateMessage() {
        try {

            while (isRunning) {
                DatagramPacket dp = linkstateMsgs.take();
                int packSize = dp.getLength();
                String str = new String(dp.getData(), 0, packSize);
                LinkStateMessage s2 = JsonParser.generateMapFromString(str);
//                if(DEBUG_MODE) System.out.println("Linkstate adv received: "+s2);
                if (s2.getReceived_from_uuid().equals(SharedResources.getServerConfig().getHostName())) {
                    if (DEBUG_MODE) System.out.println("Node duplicate found in linkstate advertisement");
                    String newalias = "node" + (new Random()).nextInt(10000);
                    SharedResources.getServerConfig().setHostNameOnDuplicate(newalias);
                    ConfigFileIO.writeToFileConfig(SharedResources.getServerConfig());
                }
                if (
                        SharedResources.getServerConfig().getPeerToSeqMap().containsKey(s2.getReceived_from_uuid()) &&
                                SharedResources.getServerConfig().getPeerToSeqMap().get(s2.getReceived_from_uuid()) >= s2.getSequence_number()
                ) {
                    handle_out_of_sync(dp, s2);
                } else {
                    SharedResources.getServerConfig().getPeerToSeqMap().put(s2.getReceived_from_uuid(), s2.getSequence_number());
                    SharedResources.getServerConfig().getPeerToTimeMap().put(s2.getReceived_from_uuid(), s2.getCurrent_time());
                    //update the table and forward
                    //update
                    //forward
                    String uuidFromAliasName = SharedResources.getServerConfig().getUUIDFromAliasName(s2.getReceived_from_uuid());
                    Peer internalPeer = SharedResources.getServerConfig().getPeers().stream().filter(peer -> peer.getUuid().equals(uuidFromAliasName))
                            .findFirst().orElse(null);
                    DatagramSocket dsock = null;
                    try {
                        dsock = new DatagramSocket();
                    } catch (SocketException e) {
                        System.out.println(e.getMessage());
                    }
                    if (DEBUG_MODE)
                        System.out.println("[" + s2.getIntermediate_from_uuid() + "]->R[" + s2.getReceived_from_uuid() + "]");
                    String org_intermediate = s2.getIntermediate_from_uuid();
                    String intermediate_uuid = org_intermediate == null ? null : SharedResources.getServerConfig().getUUIDFromAliasName(org_intermediate);

                    updateLinkStateMessage(s2);

                    s2.setIntermediate_from_uuid(SharedResources.getServerConfig().getHostName());
                    byte[] arr = JsonParser.generateStringFromJson(s2).getBytes();
                    for (Peer p : SharedResources.getServerConfig().getPeers()) {
                        if (p == internalPeer) {
                            continue;
                        }
                        if (p.getUuid().equals(intermediate_uuid))
                            continue;
                        if (!p.isActive()) continue;
                        if (DEBUG_MODE)
                            System.out.println("[" + s2.getReceived_from_uuid() + "] -> [" + s2.getIntermediate_from_uuid() + "] -> ["
                                    + SharedResources.getServerConfig().getUuidToAlias().get(p.getUuid()) + "] / #P[" + linkstateMsgs.size() + "]");
                        DatagramPacket dpack = new DatagramPacket(arr, arr.length, InetAddress.getByName(p.getIp()), p.getPort());
                        try {
                            dsock.send(dpack);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (InterruptedException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void updateLinkStateMessage(LinkStateMessage s2) {
        LinkStateMessage localMsg = SharedResources.getServerConfig().getLinkStateMessage();
        if (_TEMP_DEBUG_MODE) System.out.println(s2);
        //own distance vector
        Map<String, Double> localDistanceVector = localMsg.getDistance_vector().get(SharedResources.getServerConfig().getHostName());
        for (Map.Entry<String, Map<String, Double>> cur : s2.getDistance_vector().entrySet()) {
            if (!localMsg.getDistance_vector().containsKey(cur.getKey())) {
                if (_TEMP_DEBUG_MODE) System.out.println("new node discovered [" + cur + "]");
                if(SharedResources.getServerConfig().isActiveMetric()){
                    localMsg.getDistance_vector().put(cur.getKey(), cur.getValue());
                }else {
                    localMsg.getDistance_vector().put(cur.getKey(), cur.getValue());
                }
                //updating own distance vector with new node
                localDistanceVector.put(cur.getKey(), cur.getValue().get(SharedResources.getServerConfig().getHostName()));
            } else {
                Map<String, Double> curDistance = cur.getValue();
                Map<String, Double> localDistance = localMsg.getDistance_vector().get(cur.getKey());
                for (Map.Entry<String, Double> it : curDistance.entrySet()) {
                    if(SharedResources.getServerConfig().isActiveMetric()){
                        localDistance.put(it.getKey(), (double) (System.currentTimeMillis() - s2.getCurrent_time()));
                    }else{
                        localDistance.put(it.getKey(), it.getValue());
                    }
                }
                if (_TEMP_DEBUG_MODE) System.out.println("[" + cur.getKey() + "]@");
            }
            Optimizer.calculate_min_distances();
//            System.out.println(localMsg);
        }
        if (_TEMP_DEBUG_MODE) System.out.println(localMsg);

    }

    private void handle_out_of_sync(DatagramPacket dp, LinkStateMessage s2) {
        //old seq number used can't forward reply back asking for update
        //handle this if you are peer to this sender
        String uuidFromAliasName = SharedResources.getServerConfig().getUUIDFromAliasName(s2.getReceived_from_uuid());
        if (uuidFromAliasName == null) return;
        int portId = SharedResources.getServerConfig().getPortIdFromPeerUUID(uuidFromAliasName);
        if (portId == -1) return;
        if (DEBUG_MODE)
            System.out.println("out of sync detected [" + s2.getIntermediate_from_uuid() + "]@[" + s2.getReceived_from_uuid() + "]");
        //not coming from immediate peer having same seq number so ignore the message
        if (s2.getIntermediate_from_uuid() != null) return;

        DatagramSocket dsock = null;
        try {
            dsock = new DatagramSocket();
        } catch (SocketException ignored) {
        }
        byte[] arr = ("OUT_OF_SYNC:" + SharedResources.getServerConfig().getPeerToSeqMap().get(s2.getReceived_from_uuid())).getBytes();
        DatagramPacket dpack = new DatagramPacket(arr, arr.length, dp.getAddress(), portId);
        try {
            dsock.send(dpack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void killService() {
        isRunning = false;
    }
}
