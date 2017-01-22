#include <elapsedMillis.h>
#include <RF24Network.h>
#include <RF24.h>
#include <Pin.h>

Pin T = Pin(A4);
Pin D = Pin(A5);
Pin S = Pin(A2);
Pin Z = Pin(A3);

int B = 3, A = 4, Y = 5, X = 6, RB = 7, LB = 8, CSN = 9, CE = 10, Steering = A0, Brake = A1, Gaz = A7, ledX = A2, ledA = A4, ledB = A5, ledY = A3;

struct Payload {
	int b;
	int a;
	int y;
	int x;
	int rb;
	int lb;
	int steering;
	int throttle;
};

struct cPayload {
  int steering;
  int throttle;
  bool lb;
  bool rb;
};

RF24 radio(CE, CSN);
RF24Network network(radio);

const int this_node = 023;
const int car_node = 03;
const int rf_channel = 115;

String received = "";
bool flash = false;

elapsedMillis timer;

int centerX = 512, minX = 384, maxX = 640, minGaz = 384, maxGaz = 640, minBrake = 384, maxBrake = 640;

int buttons[6] = { B,A,Y,X,RB,LB };
int buttonsValue[6] = { 1,1,1,1,1,1 };
int leds[4] = { ledB, ledA, ledY, ledX };

void setup() {
	for (int i = 0; i < 5; i++) {
		centerX += analogRead(A0);
	}
	centerX /= 6;
	for (int i = 0; i < 6; i++) pinMode(buttons[i], INPUT_PULLUP);
  Z.setOutput();
	Z.setLow();
  T.setOutput();
	T.setHigh();
  S.setOutput();
	S.setHigh();
  D.setOutput();
	D.setHigh();
	radio.begin();
	radio.setDataRate(RF24_250KBPS);
	radio.setRetries(0, 0);
	network.begin(rf_channel, this_node);
}

void loop() {
	network.update();
	nRF_receive();
  if (received != "") {
    for (int i = 0; i < received.length(); i++)
    {
      switch (received[i])
      {
      case 'S':
        S.setHigh();
        break;
      case 's':
        S.setLow();
        break;
      case 'T':
        T.setHigh();
        break;
      case 't':
        T.setLow();
        break;
      case 'D':
        flash = true;
        break;
      case 'd':
        flash = false;
        break;
      }
    }
    received = "";
  }
	if (buttonsValue[1]) buttonsValue[1] = digitalRead(buttons[1]);
  if (buttonsValue[3]) buttonsValue[3] = digitalRead(buttons[3]);
  if (timer > 75) {
    if (flash) D.toggleState();
    else if (!D.getState()) D.setHigh();
    cPayload payload;
    payload.rb = buttonsValue[3];
    payload.lb = buttonsValue[1];
    payload.steering = 180 - readX();
    payload.throttle = 255 - readY();
		RF24NetworkHeader header(car_node);
		network.write(header, &payload, sizeof(payload));
    buttonsValue[1] = 1;
    buttonsValue[3] = 1;
		timer = 0;
	}
}

void nRF_receive(void) {
	if (network.available()) {
		RF24NetworkHeader header;
		network.peek(header);
		if (header.from_node == car_node) {
			unsigned int message;
			network.read(header, &message, sizeof(unsigned int));
      char c = message;
      received += c;
		}
	}
}

int readX() {
	int Xval = analogRead(Steering);
	if (Xval < minX) minX = Xval;
	if (Xval > maxX) maxX = Xval;
	if (Xval < centerX) Xval = map(Xval, minX, centerX, -5, 90);
	else Xval = map(Xval, centerX, maxX, 90, 185);
	if (Xval < 0) Xval = 0;
	if (Xval > 180) Xval = 180;
	return Xval;
}

int readY() {
	int gazValue = analogRead(Gaz);
	int brakeValue = analogRead(Brake);
	if (gazValue < minGaz) minGaz = gazValue;
	if (gazValue > maxGaz) maxGaz = gazValue;
	if (brakeValue < minBrake) minBrake = brakeValue;
	if (brakeValue > maxBrake) maxBrake = brakeValue;
	gazValue = map(gazValue, minGaz, maxGaz, 260, -5);
	gazValue /= 2;
	gazValue += 127;
	if (gazValue < 127) gazValue = 127;
	if (gazValue > 255) gazValue = 255;
	brakeValue = map(brakeValue, minBrake, maxBrake, 260, -5);
	brakeValue /= 2;
	if (brakeValue < 0) brakeValue = 0;
	if (brakeValue > 127) brakeValue = 127;
	int Yval = gazValue - brakeValue;
	return Yval;
}
