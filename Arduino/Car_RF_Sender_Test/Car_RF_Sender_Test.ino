#include <RF24Network.h>
#include <RF24.h>

struct Payload { int X; int Y; bool LB; bool RB; bool configButton; };

/******************/
#define THIS_NODE 14
/******************/

RF24 radio(10, 9);
RF24Network network(radio);

long timer = 0;

void setup() {
  Serial.begin(9600);
  radio.begin();
  radio.setDataRate(RF24_250KBPS);
  radio.setRetries(0, 15);
  radio.setCRCLength(RF24_CRC_16);
  radio.setPALevel(RF24_PA_MAX);
  network.begin(115, THIS_NODE);
}

void loop() {
  network.update();
  if (millis() - timer > 2000) {
    Payload a = { 127,255,1,0,1 };
    RF24NetworkHeader header(THIS_NODE - 010);
    network.write(header, &a, sizeof(a));
    timer = millis();
  }
}
