#include <RF24Network.h>
#include <RF24.h>

struct cPayload { int X; int Y; bool LB; bool RB; bool configButton; };

/******************/
#define CAR_NODE 04
/******************/

RF24 radio(9, 10);
RF24Network network(radio);

void setup() {
  Serial.begin(9600);
  while(!Serial);
	radio.begin();
	radio.setDataRate(RF24_250KBPS);
	radio.setRetries(0, 15);
	radio.setCRCLength(RF24_CRC_16);
	radio.setPALevel(RF24_PA_MAX);
	network.begin(115, CAR_NODE);
  Serial.println("READY");
}

void loop() {
	network.update();
	nRF_receive();
}

void nRF_receive(void) {
	if (network.available()) {
		RF24NetworkHeader header;
		network.peek(header);
		cPayload a; // X, Y, LB, RB, CB
		network.read(header, &a, sizeof(a));
		handle_controller(a.X, a.Y, a.LB, a.RB, a.configButton);
	}
}

void handle_controller(int X, int Y, bool LB, bool RB, bool configButton) {
  String msg = "C: ";
  msg += X;
  msg += ",";
  msg += Y;
  msg += ",";
  msg += LB;
  msg += ",";
  msg += RB;
  msg += ",";
  msg += configButton;
  Serial.println(msg);
}
