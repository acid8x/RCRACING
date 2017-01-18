#include <SoftwareSerial.h>
#include <RF24Network.h>
#include <RF24.h>
#include <elapsedMillis.h>
#include <IRremote.h>

#define GATE 1
#define REPEAT_RATE_IN_MS 75

IRsend irsend;

elapsedMillis timer = 0;

#if GATE == 1
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

String packetsArray = "";
#endif

void setup(void) {
#if GATE == 1
	//Serial.begin(9600);
	mySerial.begin(9600);
	radio.begin();
	radio.setDataRate(RF24_250KBPS);
	radio.setRetries(0, 15);
	radio.setCRCLength(RF24_CRC_16);
	radio.setPALevel(RF24_PA_MAX);
	network.begin(Channel, 0);
#endif
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
		} else if (inChar == '#') packetsArray += "1C02C03C04C05C0";
		else if (read && inChar > 32 && inChar < 123) {
			inputString += inChar;
			index++;
			if (index == 3) {
				sendMessage(inputString[0], inputString[1], inputString[2]);
				read = false;
			}
		}
	}
#endif
	if (timer > REPEAT_RATE_IN_MS) {
    if (packetsArray.length() > 2) {
      String temp = "";
      for (int i = 3; i < packetsArray.length(); i++) {
        temp += packetsArray[i];
      }
      sendMessage(packetsArray[0],packetsArray[1],packetsArray[2]);
      packetsArray = temp;
    }
	  irsend.sendSony(GATE, 12);
		timer = 0;
	}
} // end loop()

#if GATE == 1
void sendMessage(char id, char com, char arg) {
	String debugMessage = "NRF Sending: ";
	debugMessage += id;
	debugMessage += com;
	debugMessage += arg;
	//Serial.println(debugMessage);
	bool validPacket = true;
	if (id < 49 || id > 57) validPacket = false;
	if (com < 65 || com > 90) validPacket = false;
	if (arg < 48 || arg > 57) validPacket = false;
	if (validPacket) {
		Payload_host p;
		p.command = com;
		p.argument = arg - 48;
		RF24NetworkHeader header(id - 48);
    int retry = 0;
		while(true) {
		  if (network.write(header, &p, sizeof(p))) break;
		  else retry++;
      if (retry == 5) break;
      delay(1);
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
		String debugMessage = "NRF Receive: ";
		debugMessage += message;
		//Serial.println(debugMessage);
		mySerial.println(message);
	}
} // end nRF_receive()
#endif
