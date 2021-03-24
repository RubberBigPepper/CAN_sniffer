package com.example.can_sniffer.CAN;

//пакет данных с шины CAN
public class CANPacket {
    private int ID;
    private int[] data;
    private long timeStampMs;

    private CANPacket (int ID, int[]data){
        timeStampMs=System.currentTimeMillis();
        this.ID=ID;
        this.data=data.clone();
    }

    public static CANPacket parsePacket(int ID, int []data){
        if (data.length>8)//пакеты не более 8 байт длиной
            return null;
        return new CANPacket(ID, data);
    }

    public int getID() {
        return ID;
    }

    public int[] getData() {
        return data;
    }

    public long getTimeStampMs() {
        return timeStampMs;
    }
}
