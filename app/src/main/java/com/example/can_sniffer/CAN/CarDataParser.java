package com.example.can_sniffer.CAN;

public interface CarDataParser {
    public void parseWheelSpeed(CANPacket canPacket, CANWheelSpeed canWheelSpeed);
}
