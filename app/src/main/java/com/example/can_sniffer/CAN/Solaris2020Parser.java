package com.example.can_sniffer.CAN;

import com.example.can_sniffer.misc.BinaryParserData;

import java.nio.ByteOrder;

//класс парсинга данных соляриса
public class Solaris2020Parser implements CarDataParser {
    private final static int START_FL_BIT = 0;
    private final static int START_FR_BIT = 16;
    private final static int START_RL_BIT = 32;
    private final static int START_RR_BIT = 48;
    private final static int LENG_SPD_BIT = 14;//сколько бит на скорость
    private final static ByteOrder ORDER = ByteOrder.BIG_ENDIAN;
    private final static double SPD_IN_MPS = 0.03125 / 3.6;//коэффициент перевода скорости из тиков в м/с

    public void parseWheelSpeed(CANPacket canPacket, CANWheelSpeed canWheelSpeed) {
        double speedFL = SPD_IN_MPS *
                BinaryParserData.parse(canPacket.getData(), START_FL_BIT, LENG_SPD_BIT, ORDER);
        double speedFR = SPD_IN_MPS *
                BinaryParserData.parse(canPacket.getData(), START_FR_BIT, LENG_SPD_BIT, ORDER);
        double speedRL = SPD_IN_MPS *
                BinaryParserData.parse(canPacket.getData(), START_RL_BIT, LENG_SPD_BIT, ORDER);
        double speedRR = SPD_IN_MPS *
                BinaryParserData.parse(canPacket.getData(), START_RR_BIT, LENG_SPD_BIT, ORDER);
        canWheelSpeed.calcSpeedXY(canPacket.getTimeStampMs(), speedFL, speedFR, speedRL, speedRR);
    }
}
