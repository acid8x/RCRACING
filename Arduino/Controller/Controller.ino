#include <elapsedMillis.h>
#include <RF24Network.h>
#include <RF24.h>

/*****************/
#define THIS_NODE 012
/*****************/

#define Channel 115

#define pinX A1
#define pinY A0

#if (THIS_NODE == 011)
#define pinLB 7
#define pinRB 6
#else
#define pinLB 6
#define pinRB 5
#endif

RF24 radio(9, 10);
RF24Network network(radio);

int centerX = 512, centerY = 512, carXcenter, X, Y, minX = 384, minY = 384, maxX = 640, maxY = 640;

bool on = true, off = false, LB, RB;

long countDown;

elapsedMillis timer = 0, timer2 = 0;

struct Payload {
	int X;
	int Y;
	bool LB;
	bool RB;
};

void setup(void) {
	for (int i = 0; i < 5; i++) {
		centerX += analogRead(pinX);
		centerY += analogRead(pinY);
	}
	centerX /= 6;
	centerY /= 6;
	pinMode(pinLB, INPUT_PULLUP);
	pinMode(pinRB, INPUT_PULLUP);
	pinMode(3, OUTPUT);
	digitalWrite(3, on);
	radio.begin();
	radio.setDataRate(RF24_250KBPS);
	radio.setRetries(0, 0);
	radio.setCRCLength(RF24_CRC_16);
	radio.setPALevel(RF24_PA_MAX);
	network.begin(/*channel*/ Channel, /*node address*/ THIS_NODE);
}

void loop(void) {
	network.update();
	if (LB) LB = digitalRead(pinLB);
	if (RB) RB = digitalRead(pinRB);
	if (timer2 > 75) {
		readX();
		readY();
		timer2 = 0;
		Payload a = { X,Y,LB,RB };
		RF24NetworkHeader header(THIS_NODE - 8);
		network.write(header, &a, sizeof(a));
		if (!LB) LB = true;
		if (!RB) RB = true;
	}
}

void readX() {
	int low = 185, high = -5;
	X = analogRead(pinX);
	if (X < minX) minX = X;
	if (X > maxX) maxX = X;
	if (X < centerX) X = map(X, minX, centerX, low, 90);
	else X = map(X, centerX, maxX, 90, high);
	if (X < 0) X = 0;
	if (X > 180) X = 180;
}

void readY() {
	int low = -5, high = 260;
	Y = analogRead(pinY);
	if (Y < minY) minY = Y;
	if (Y > maxY) maxY = Y;
	if (Y < centerY) Y = map(Y, minY, centerY, low, 127);
	else Y = map(Y, centerY, maxY, 127, high);
	if (Y < 0) Y = 0;
	if (Y > 255) Y = 255;
}