#include <SoftwareSerial.h>
#include <RF24Network.h>
#include <RF24.h>
#include <elapsedMillis.h>
#include <IRremote.h>

#define GATE 1
#define REPEAT_RATE_IN_MS 100

IRsend irsend;

elapsedMillis timer;

#if GATE == 1
#define Channel 115

SoftwareSerial mySerial(7, 8);

RF24 radio(9, 10);
RF24Network network(radio);

String inputString = "";
bool read = false;
int index = 0;

int ircode;

struct Payload_host {
	char command;
	int argument;
};

String packetsArray = "";
#endif

void setup(void) {
#if GATE == 1
	mySerial.begin(9600);
	radio.begin();
	radio.setDataRate(RF24_250KBPS);
	radio.setRetries(0, 15);
	radio.setCRCLength(RF24_CRC_16);
	radio.setPALevel(RF24_PA_MAX);
	network.begin(Channel, 0);
#endif
	ircode = GATE;
} // end setup()

void loop(void) {
#if GATE == 1
	network.update();
	nRF_receive();
	if (mySerial.available() > 0) {
		char inChar = (char)mySerial.read();
		if (inChar == '&') {
			read = true;
			inputString = "";
			index = 0;
		} else if (inChar == '#') {
			for (char i = 49; i < 54; i++) sendMessage(i, 'C', '0');
		} else if (inChar == '^') {
			ircode = 99;
			for (char i = 49; i < 54; i++) sendMessage(i, 'R', '1');
		} else if (inChar == '*') {
			ircode = GATE;
			for (char i = 49; i < 54; i++) sendMessage(i, 'R', '0');
		} else if (read && inChar > 32 && inChar < 123) {
			inputString += inChar;
			index++;
			if (index == 3) {
				sendMessage(inputString[0], inputString[1], inputString[2]);
				read = false;
			}
		}
	}
	if (packetsArray.length() > 2) {
		String temp = "";
		for (int i = 3; i < packetsArray.length(); i++) temp += packetsArray[i];
		sendMessage(packetsArray[0], packetsArray[1], packetsArray[2]);
		packetsArray = temp;
	}
#endif
	if (timer > REPEAT_RATE_IN_MS) {
		int toSend;
		if (ircode == 99) toSend = 66;
		else toSend = ircode;
		irsend.sendSony(toSend, 12);
		delay(40);
		irsend.sendSony(toSend, 12);
		timer = 0;
	}
}

#if GATE == 1
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
		if (!network.write(header, &p, sizeof(p))) {
			packetsArray += id;
			packetsArray += com;
			packetsArray += arg;
		}
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
}
#endif