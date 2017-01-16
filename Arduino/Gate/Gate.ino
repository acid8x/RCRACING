#include <elapsedMillis.h>
#include <IRremote.h>

#define GATE 2
#define REPEAT_RATE_IN_MS 75

IRsend irsend;

elapsedMillis timer = 0;

#if GATE == 1
#define MASTER
#include <SoftwareSerial.h>
#include <RF24Network.h>
#include <RF24.h>
#endif

#ifdef MASTER
#define Channel 115
SoftwareSerial mySerial(7, 8);

RF24 radio(9, 10);
RF24Network network(radio);

String inputString = "";
bool read = false;
int index = 0;

struct Payload_host {
	char command;
	int argument;
};

void setup(void) {
	mySerial.begin(9600);
	radio.begin();
	radio.setDataRate(RF24_250KBPS);
	radio.setRetries(0, 15);
	radio.setCRCLength(RF24_CRC_16);
	radio.setPALevel(RF24_PA_MAX);
	network.begin(Channel, 0);
} // end setup()
#endif // MASTER

void loop(void) {
#ifdef MASTER
	network.update();
	nRF_receive();
	if (mySerial.available() > 0) {
		char inChar = (char)mySerial.read();
		if (inChar == '&') {
			read = true;
			inputString = "";
			index = 0;
		}
		else if (read && inChar > 32 && inChar < 123) {
			inputString += inChar;
			index++;
			if (index == 3) {
				sendMessage(inputString[0], inputString[1], inputString[2]);
				read = false;
			}
		}
	}
#endif // MASTER
	if (timer > REPEAT_RATE_IN_MS) {
		irsend.sendSony(GATE, 12);
		timer = 0;
	}
} // end loop()

#ifdef MASTER
void sendMessage(char id, char com, char arg) {
	bool validPacket = true;
	if (id < 49 || id > 57) validPacket = false;
	if (com < 65 || com > 90) validPacket = false;
	if (arg < 48 || arg > 57) validPacket = false;
	if (validPacket) {
		Payload_host p;
		p.command = com;
		p.argument = arg - 48;
		RF24NetworkHeader header(id - 48);
		network.write(header, &p, sizeof(p));
	}
}

void nRF_receive(void) {
	if (network.available()) {
		RF24NetworkHeader header;
		Payload_host payload;
		network.read(header, &payload, sizeof(payload));
		String message = "";
		message += header.from_node;
		message += payload.command;
		message += payload.argument;
		mySerial.println(message);
	}
} // end nRF_receive()
#endif // MASTER