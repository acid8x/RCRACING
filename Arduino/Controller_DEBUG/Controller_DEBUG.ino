#include <elapsedMillis.h>
#include <RF24Network.h>
#include <RF24.h>

/*****************/
#define THIS_NODE 012
/*****************/

#define Channel 115

RF24 radio(7, 8);
RF24Network network(radio);

bool on = true, off = false, LB = true, RB = true, configButton = true;

int X = 90, Y = 127;

elapsedMillis timer = 0, timer2 = 0;

struct Payload {
	int X;
	int Y;
	bool LB;
	bool RB;
  bool configButton;
};

void setup(void) {
  pinMode(4,INPUT_PULLUP);
  Serial.begin(9600);
	radio.begin();
	radio.setDataRate(RF24_250KBPS);
	radio.setRetries(0, 0);
	radio.setCRCLength(RF24_CRC_16);
	radio.setPALevel(RF24_PA_MAX);
	network.begin(/*channel*/ Channel, /*node address*/ THIS_NODE);
}

void loop(void) {
  configButton = digitalRead(4);
	network.update();
  if (Serial.available() > 0) {
    char c = Serial.read();
    if (c == '+') X++;
    else if (c == '-') X--;
    Serial.println(X);
  }
	if (timer2 > 75) {
		readX();
		readY();
		timer2 = 0;
		Payload a = { X,Y,LB,RB,configButton };
		RF24NetworkHeader header(THIS_NODE - 010);
		network.write(header, &a, sizeof(a));
	}
}

void readX() {
  return X;
}

void readY() {
  return Y;
}
