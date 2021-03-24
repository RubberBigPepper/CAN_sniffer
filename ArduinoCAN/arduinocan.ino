#include "can.h"
#include "mcp2515.h"

#include "CanHacker.h"
#include "CanHackerLineReader.h"
#include "lib.h"
#include "mcp2515.h"
#include <SPI.h>


const int SPI_CS_PIN = 10;
const int INT_PIN = 2;//пин, который отвечает за INT0 

CanHackerLineReader *lineReader = NULL;
CanHacker *canHacker = NULL;

void setup() {
    Serial.begin(115200);
    while (!Serial);
    SPI.begin();
        
    
    Stream *interfaceStream = &Serial;
    canHacker = new CanHacker(interfaceStream, NULL, SPI_CS_PIN);
    canHacker ->setClock(MCP_8MHZ);
    lineReader = new CanHackerLineReader(canHacker);
    
    pinMode(INT_PIN, INPUT);
}

void loop() {
    if (digitalRead(INT_PIN) == LOW) {
        canHacker->processInterrupt();
    }

//  uncomment that lines for Leonardo, Pro Micro or Esplora
//  if (Serial.available()) {
//      lineReader->process(); 
//  }
}

// serialEvent handler not supported by Leonardo, Pro Micro and Esplora
void serialEvent() {
    lineReader->process();
}
