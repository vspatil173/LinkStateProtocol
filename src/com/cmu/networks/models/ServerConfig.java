package com.cmu.networks.models;

import java.util.LinkedList;
import java.util.List;

public class ServerConfig {
    String uuid;
    String hostName;
    int backendPort;
    int peer_count;
    List<Peer> peers;

    public ServerConfig() { peers = new LinkedList<>(); }

    public ServerConfig(String uuid, String hostName, int backendPort) {
        this.uuid = uuid;
        this.hostName = hostName;
        this.backendPort = backendPort;
        peers = new LinkedList<>();
    }

    public ServerConfig(String uuid, String hostName, int backendPort, int peer_count, List<Peer> peers) {
        this.uuid = uuid;
        this.hostName = hostName;
        this.backendPort = backendPort;
        this.peer_count = peer_count;
        this.peers = peers;
    }

    public String getUuid() { return uuid; }

    public void setUuid(String uuid) { this.uuid = uuid;}

    public String getHostName() { return hostName; }

    public void setHostName(String hostName) { this.hostName = hostName;}

    public int getBackendPort() { return backendPort; }

    public void setBackendPort(int backendPort) { this.backendPort = backendPort;}

    public int getPeer_count() { return peer_count; }

    public void setPeer_count(int peer_count) { this.peer_count = peer_count; }

    public List<Peer> getPeers() { return peers; }

    public void setPeers(List<Peer> peers) { this.peers = peers; }

    public void addPeerFromString(String parameters){
        String[] splits = parameters.split(" = ");
        Peer curPer = new Peer();
        curPer.setPid(splits[0].trim());
        String[] paras = splits[1].split(",");
        curPer.setUuid(paras[0].trim());
        curPer.setIp(paras[1].trim());
        curPer.setPort(Integer.parseInt(paras[2].trim()));
        curPer.setDistance(Integer.parseInt(paras[3].trim()));
        peers.add(curPer);
    }

    @Override
    public String toString() {
        StringBuilder result =  new StringBuilder("uuid = " + uuid + "\n" +
                "name = " + hostName + "\n" +
                "backend_port = " + backendPort + "\n"+
                "peer_count = " + peer_count +"\n");
        peers.forEach(peer -> {
            result.append(peer.toString()).append("\n");
        });
        return result.toString();
    }

    public String printListOfNeighbor(){
        StringBuilder str = new StringBuilder("[");
        for (Peer peer : peers) {
            str.append("{\"uuid\":\"").append(peer.uuid).append("\",").append("\"name\":\"").append(peer.pid).append("\",").append("\"host\":\"").append(peer.ip).append("\",").append("\"backend_port\":\"").append(peer.port).append("\",").append("\"metric\":\"").append(peer.distance).append("\"},");
        }
        return str.substring(0,str.length()-1)+"]";
    }

    public void addPeer(Peer peer){
        peers.add(peer);
        peer_count = peers.size();
    }
}
