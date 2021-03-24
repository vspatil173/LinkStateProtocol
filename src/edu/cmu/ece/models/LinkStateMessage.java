package edu.cmu.ece.models;

import edu.cmu.ece.helper.JsonParser;

import java.util.HashMap;
import java.util.Map;

public class LinkStateMessage {
    String received_from_uuid;
    String intermediate_from_uuid;
    long sequence_number;
    Map<String,Map<String,Integer>> distance_vector;

    public LinkStateMessage() {
        sequence_number = 0;
        distance_vector = new HashMap<>();
    }

    public LinkStateMessage(String received_from_uuid) {
        this();
        this.received_from_uuid = received_from_uuid;
    }

    public String getReceived_from_uuid() { return received_from_uuid; }
    public void setReceived_from_uuid(String received_from_uuid) { this.received_from_uuid = received_from_uuid; }

    public long getSequence_number() { return sequence_number;}
    public long increment_Sequence_Number(){ return ++sequence_number; }
    public Map<String, Map<String, Integer>> getDistance_vector() { return distance_vector; }
    public void setDistance_vector(Map<String, Map<String, Integer>> distance_vector) { this.distance_vector = distance_vector; }

    public void addPeerToCurrentNode(String node){ this.distance_vector.putIfAbsent(node,new HashMap<>()); }

    public void addDistanceVectorDataForNode(String node, String subNodeName,int distance){
        this.addPeerToCurrentNode(node);
        this.distance_vector.get(node).put(subNodeName,distance);
    }

    public void updateMapForPeer(String node, Map<String,Integer> peerMap){
        //discarding older info
        addPeerToCurrentNode(node);
        this.distance_vector.get(node).putAll(peerMap);
    }

    @Override
    public String toString() {
        return JsonParser.generateStringFromJson(this);
    }

    public void setSequence_number(long sequence_number) {
        this.sequence_number = sequence_number;
    }

    public String getIntermediate_from_uuid() { return intermediate_from_uuid; }
    public void setIntermediate_from_uuid(String intermediate_from_uuid) { this.intermediate_from_uuid = intermediate_from_uuid; }
}
