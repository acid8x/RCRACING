#include <Pin.h>
#include <PWMServo.h>
#include <RF24Network.h>
#include <RF24.h>
#include <IRremote.h>
#include <elapsedMillis.h>

struct hPayload { char command; int argument; };
struct cPayload { int X; int Y; bool LB; bool RB; };

/******************/
#define THIS_NODE 03
/******************/

Pin led[3]{ Pin(14),Pin(15),Pin(16) };

Pin FWR = Pin(6);
Pin REV = Pin(5);

#if (THIS_NODE == 04 || THIS_NODE == 02)
#define Steering SERVO_PIN_B
RF24 radio(7, 8);
#else
#define Steering SERVO_PIN_A
RF24 radio(8, 7);
#endif

RF24Network network(radio);

bool flashLed[3]{ true,true,true };

int node_controller, 
	node_new_controller, 
	raceType = 0, 
	lastGate, 
	gunUse = 0,
	oldSpeed,
	rainbowPin = 0;

bool gunReady = true, 
	turboUse = false, 
	turboReady = true, 
	stopY = false, 
	hasBeenShot = false, 
	reverseStop = false, 
	new_controller_connected = false;

PWMServo myservo;

IRsend irsend;

IRrecv *irrecvs[2];
decode_results results;

elapsedMillis flash, tGun, tTurbo, tHasBeenShot, tRaceType, controllerUpdate, tRainbow;

void setup() {
	myservo.attach(Steering);
	myservo.write(90);
	FWR.setOutput();
	FWR.setDutyCycle(0);
	REV.setOutput();
	REV.setDutyCycle(0);
	led[0].setOutputHigh();
	led[1].setOutputHigh();
	led[2].setOutputHigh();
	radio.begin();
	radio.setDataRate(RF24_250KBPS);
	radio.setRetries(0, 15);
	radio.setCRCLength(RF24_CRC_16);
	radio.setPALevel(RF24_PA_MAX);
	node_controller = THIS_NODE + 010;
	node_new_controller = THIS_NODE + 020;
	network.begin(115, THIS_NODE);
	irrecvs[0] = new IRrecv(2);
	irrecvs[0]->enableIRIn();
	irrecvs[1] = new IRrecv(4);
	irrecvs[1]->enableIRIn();
}

void loop() {
	network.update();
	nRF_receive();
	if (!hasBeenShot && flash > 200) updateLeds();
	if (raceType > 0) {
		if (raceType > 1) checkDamageState();
		if (raceType > 1) checkShootState();
		checkTurboState();
		checkIRState();
	}
	else checkRaceType();
	if (!stopY && controllerUpdate > 500) {
		setLed(2, 2);
		stopY = true;
	}
}

void nRF_receive(void) {
	if (network.available()) {
		RF24NetworkHeader header;
		network.peek(header);
		if (header.from_node == 0) {
			if (header.type == 65) {
				unsigned int i;
				network.read(header, &i, sizeof(i));
				handle_host('C', 0);
			}
			else {
				hPayload p;
				network.read(header, &p, sizeof(p));
				handle_host(p.command, p.argument);
			}
		}
		else {
      if (header.from_node == node_new_controller && !new_controller_connected) new_controller_connected = true;
			cPayload a; //int X; int Y; bool LB; bool RB; };
			network.read(header, &a, sizeof(a));
			controllerUpdate = 0;
			if (!hasBeenShot && stopY) {
				stopY = false;
				if (gunReady) setLed(2, 1);
				else setLed(2, 0);
			} else handle_controller(a.X, a.Y, a.LB, a.RB);
		}
	}
}

void handle_controller(int X, int Y, bool LB, bool RB) {
	if (!RB && gunReady) {
		sendHost('S', 0);
		gunUse = 1;
		gunReady = false;
	}
	if (!LB && turboReady) {
		turboUse = true;
		turboReady = false;
	}
	myservo.write(X);
	int maxValue = 255;
	if (!turboUse) maxValue = 200;
	int Speed = map(Y, 0, 255, maxValue*-1, maxValue);
	if (reverseStop) {
		Speed = oldSpeed;
		oldSpeed = 0;
	}
	bool reverse = false;
	if (Speed < 0) reverse = true;
	if (!stopY && Speed != oldSpeed) {
		if (reverseStop) reverse = !reverse;
		int pwm = Speed;
		if (reverse) pwm *= -1;
		if (pwm > 255) pwm = 255;
		else if (pwm < 20) pwm = 0;
		if (reverse) {
			FWR.setDutyCycle(0);
			REV.setDutyCycle(pwm);
		}
		else {
			FWR.setDutyCycle(pwm);
			REV.setDutyCycle(0);
		}
		oldSpeed = Speed;
	}
	else if (stopY) {
		FWR.setDutyCycle(255);
		REV.setDutyCycle(255);
	}
}

void handle_host(char command, int argument) {
	if (argument > 47) argument -= 48;
	switch (command)
	{
	case 'C':
		raceType = argument;
		setRaceType(raceType);
		stopY = false;
		gunUse = 0;
		if (raceType == 1) gunReady = false;
		else gunReady = true;
		turboUse = false;
		turboReady = true;
		lastGate = 0;
		break;
	case 'T':
		turboReady = argument;
		break;
	case 'A':
		gunReady = argument;
		break;
	case 'R':
		stopY = argument;
		break;
	case 'G':
		lastGate = argument;
		break;
	}
}

void checkDamageState() {
	if (hasBeenShot && tHasBeenShot > 3000) {
		if (new_controller_connected) sendNewController('d');
		hasBeenShot = false;
		setLed(0, 1);
		if (turboReady) setLed(1, 1);
		else setLed(1, 0);
		if (gunReady) setLed(2, 1);
		else setLed(2, 0);
		oldSpeed = 256;
		stopY = false;
	}
	else if (reverseStop && tHasBeenShot > 450) {
		reverseStop = false;
	}
	else if (hasBeenShot) {
		if (tRainbow > 75) {
			led[rainbowPin++].toggleState();
			if (rainbowPin == 3) rainbowPin = 0;
			tRainbow = 0;
		}
	}
}

void checkShootState() {
	if (gunUse == 1) {
		if (new_controller_connected) sendNewController('s');
		tGun = 0;
		setLed(2, 0);
		irsend.sendSony(THIS_NODE + 10, 12);
		gunUse++;
	}
	else if (gunUse == 2 && tGun > 75) {
		gunUse++;
		irsend.sendSony(THIS_NODE + 10, 12);
	}
	else if (gunUse == 3 && tGun > 150) {
		gunUse = 0;
		irsend.sendSony(THIS_NODE + 10, 12);
		irrecvs[0]->enableIRIn();
		irrecvs[1]->enableIRIn();
	}
	else if (!gunReady && tGun > 3000) {
		if (new_controller_connected) sendNewController('S');
		gunReady = true;
		setLed(2, 1);
	}
}

void checkTurboState() {
	if (turboUse && turboReady) {
		if (new_controller_connected) sendNewController('t');
		setLed(1, 0);
		tTurbo = 0;
		turboReady = false;
	}
	else if (!turboReady && tTurbo > 10000) {
		setLed(1, 1);
		turboReady = true;
		if (new_controller_connected) sendNewController('T');
	}
	else if (turboUse && !turboReady && tTurbo > 5000) {
		setLed(1, 2);
		turboUse = false;
	}
}

void checkIRState() {
	int value[2] = { -1,-1 };
	for (int i = 0; i < 2; i++) {
		if (irrecvs[i]->decode(&results)) {
			if (results.value < 16) value[i] = results.value;
			irrecvs[i]->resume();
		}
	}
	int code = 0;
	if (value[0] != -1 || value[1] != -1) {
		if (value[1] == value[0]) code = value[0];
		else if (value[0] == -1 && value[1] < 16) code = value[1];
		else if (value[1] == -1 && value[0] < 16) code = value[0];
	}
	if (code > 0) {
		if (raceType > 0 && raceType < 4) {
			if (raceType > 1 && code > 10) {
				if (tHasBeenShot > 5000) {
					tHasBeenShot = 0;
					hasBeenShot = true;
					stopY = true;
					reverseStop = true;
					sendHost('D', code - 10);
					if (new_controller_connected) sendNewController('D');
				}
			}
			else if (raceType < 3 && code < 10) {
				if (lastGate != code) {
					lastGate = code;
					sendHost('G', code);
				}
			}
		}
	}
}

void checkRaceType() {
	if (tRaceType > 2000) {
		tRaceType = 0;
		sendHost('Z', 0);
	}
}

void setRaceType(int i) {
	if (i != 0) {
		setLed(0, 1);
		setLed(1, 1);
	}
	else {
		setLed(0, 2);
		setLed(1, 2);
	}
	if (i == 1)	setLed(2, 0);
	else setLed(2, 1);
}

void sendHost(char command, int value) {
	hPayload p = { command,value };
	RF24NetworkHeader header(0);
	int retry = 0;
	while (true) {
		if (network.write(header, &p, sizeof(p))) break;
		else retry++;
		if (retry == 3) break;
		delay(1);
	}
}

void sendNewController(char command) {
	RF24NetworkHeader header(node_new_controller);
	unsigned int message = command;
	int retry = 0;
	while (true) {
		if (network.write(header, &message, sizeof(unsigned int))) break;
		else retry++;
		if (retry == 3) break;
		delay(1);
	}
}

void setLed(int pin, int state) {
	if (state == 2) {
		flashLed[pin] = true;
	}
	else {
		flashLed[pin] = false;
		if (state == 1) led[pin].setHigh();
		else led[pin].setLow();
	}
}

void updateLeds() {
	for (int i = 0; i < 3; i++) {
		if (flashLed[i]) led[i].toggleState();
	}
	flash = 0;
}