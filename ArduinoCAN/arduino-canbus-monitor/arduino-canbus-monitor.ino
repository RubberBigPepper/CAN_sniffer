
#include <SPI.h>
#include "mcp_can.h"
#include "can-232.h"
#include "SoftwareSerial.h"

#define DEBUG_MODE

void setup() {
    while(!Serial);
	  Serial.begin(LW232_DEFAULT_BAUD_RATE); // default COM baud rate is 115200. 
    Can232::init(CAN_500KBPS, MCP_8MHz); // set default rate you need here and clock frequency of CAN shield. Typically it is 16MHz, but on some MCP2515 + TJA1050 it is 8Mhz

    // optional custom packet filter to reduce number of messages comingh through to canhacker
    // Can232::setFilter(customFilter); 
}

INT8U customFilter(INT32U addr) {
    INT8U ret = LW232_FILTER_SKIP; //LW232_FILTER_PROCESS or LW232_FILTER_SKIP
    switch(addr) {
    //    case 0x01b: //VIN
    //    case 0x1C8:  //lights
    //    case 0x2C0: // pedals
        case 0x3d0: // sound vol, treb..
      ret = LW232_FILTER_PROCESS;
    //    case 0x000: // ?
    //    case 0x003: //shifter
    //    case 0x015: // dor open close affects this as well
    //    case 0x02c: // ???
    //        ret = 0;
    //        break;
    //    case 0x002:
    //    case 0x1a7: //fuel cons or some other
    //      ret = 1;
    //      break;
    //     default: 
    //       ret = 0;
    }

  return ret;
}

void loop() {
    Can232::loop();
}

void serialEvent() {
    Can232::serialEvent();
}
