package com.cmu.networks.services;

public class HeartBeatService implements Runnable{

    boolean isRunning = true;
    @Override
    public void run() {
        new Thread(this::sendHeartBeats).start();
        new Thread(this::receiveHeartBeats).start();
    }

    public void sendHeartBeats(){
        while (isRunning){
            System.out.println("send heart beats...");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void receiveHeartBeats(){
        while (isRunning){
            System.out.println("receive heart beats...");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void killService(){
        isRunning = false;
    }
}
