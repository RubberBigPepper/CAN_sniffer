package com.example.can_sniffer.CAN;

import com.example.can_sniffer.misc.BinaryParserData;
import com.example.can_sniffer.usb.UsbUartReader;

import java.nio.ByteOrder;

//класс будет собирать пакеты и отдавать парсеру
public class CANDataManager{

    private final int WHEEL_SPD_ID=0x386;//пока захардкодим, но надо переписать на классы парсинга скорости

    private CANWheelSpeed canWheelSpeed=new CANWheelSpeed();
    private CarDataParser carDataParser=new Solaris2020Parser();

    public interface CANDataListener{

    };

    public CANWheelSpeed getCanWheelSpeed() {
        return canWheelSpeed;
    }

    public void setCANPacket(CANPacket canPacket) {
        switch (canPacket.getID()){
            case WHEEL_SPD_ID:
                carDataParser.parseWheelSpeed(canPacket,canWheelSpeed);
                break;
        }
    }

}
