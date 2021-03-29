package edu.cmu.ece.models;

public class Peer {
    String pid;
    String uuid;
    String ip;
    int port;
    int distance;
    boolean isActive;
    public Peer() {
        isActive = true;
    }

    public Peer(String pid, String uuid, String ip, int port, int distance) {
        this.pid = pid;
        this.uuid = uuid;
        this.ip = ip;
        this.port = port;
        this.distance = distance;
        isActive = true;
    }

    @Override
    public String toString() {
        return pid + " = " + uuid + ", " + ip + ", " + port +", "+ distance+", "+ (isActive);
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public boolean isActive() { return isActive; }

    public void setActive(boolean active) { isActive = active; }
}
