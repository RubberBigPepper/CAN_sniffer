

#include <SPI.h>
#include <mcp_can.h>


// the cs pin of the version after v1.1 is default to D9
// v0.9b and v1.0 is default D10
const int SPI_CS_PIN = 13;

MCP_CAN CAN(SPI_CS_PIN);                                    // Set CS pin
//MCP2515 CAN(SPI_CS_PIN);                                    // Set CS pin
//struct can_frame canMsg;
int curByte=0;//текущий байт считывания
int addr=0;
unsigned char stmp[8] = {0, 0, 0, 0, 0, 0, 0, 0};

void setup()
{
    delay(5000);
    Serial.begin(115200);
    SPI.begin();

    while (CAN_OK != CAN.begin(CAN_500KBPS, MCP_8MHz))              // init can bus : baudrate = 500k
    {
        Serial.println("CAN BUS Shield init fail");
        Serial.println(" Init CAN BUS Shield again");
        delay(100);
    }
    Serial.println("CAN BUS Shield init ok!");
    /*CAN.init_Mask(0, 0,0); // разрешить маскам получать обычные сообщения
    CAN.init_Filt(0, 0, 0); // разрешить фильтрам получать обычные сообщения
    CAN.init_Mask(1, 0,0); // разрешить маскам получать расширенные сообщения
    CAN.init_Filt(1, 1, 0); // разрешить маскам получать расширенные сообщения*/
}


void loop()
{
    unsigned char len = 0;
    unsigned char buf[8];

    if(CAN_MSGAVAIL == CAN.checkReceive())            // check if data coming
    {
        CAN.readMsgBuf(&len, buf);    // read data,  len: data length, buf: data buf

        unsigned long canId = CAN.getCanId();
        
        /* это вариант для просто теста
         * Serial.println("-----------------------------");
        Serial.print("Get data from ID: 0x");
        Serial.println(canId, HEX);
        Serial.print(": ");

        for(int i = 0; i<len; i++)    // print the data
        {
            Serial.print("0x");
            Serial.print(buf[i], HEX);
            Serial.print(", ");
        }
        Serial.println();*/
        //это вариант для парсинга на стороне андроида
        Serial.print("ID:0x");
        Serial.print(canId, HEX);
        for(int i = 0; i<len; i++)    // print the data
        {
            Serial.print(",0x");
            Serial.print(buf[i], HEX);
        }
        Serial.println();
    }    
    if (Serial.available() >0) {  //если есть доступные данные, 10 байт - первые два это abritration_id
        // считываем байт
        int incomingByte = Serial.read();
        switch(curByte){
          case 0:
            addr=incomingByte<<8;
            break;
          case 1:
            addr+=incomingByte;
            break;
          case 2:
          case 3:
          case 4:
          case 5:
          case 6:
          case 7:
          case 8:
          case 9:
            stmp[curByte-2]=incomingByte;
            if (curByte==9){
              curByte=-1;
              if (CAN_OK==CAN.sendMsgBuf(addr, 0, 8, stmp))
                Serial.print("sent:0x");
              else
                Serial.print("sent error:0x");
              Serial.print(addr, HEX);
              for(int n=0;n<8;n++){
                Serial.print(",0x");
                Serial.print(stmp[n],HEX);
              }
              Serial.println();
            }
            break;
        }
        curByte++;        
    }

}
