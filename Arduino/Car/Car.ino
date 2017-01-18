#include <Pin.h>
#include <PWMServo.h>
#include <RF24Network.h>
#include <RF24.h>
#include <IRremote.h>
#include <elapsedMillis.h>

Pin R = Pin(14);
Pin G = Pin(15);
Pin B = Pin(16);

struct hPayload { char command; int argument; };
struct cPayload { int X; int Y; bool LB; bool RB; };

/******************/
#define THIS_NODE 01
/******************/

#define Channel 115

#define Sender 3
#define Receiver1 2
#define Receiver2 4
#define FWRpin 6
#define REVpin 5

#if (THIS_NODE == 04 || THIS_NODE == 02)
#define Steering SERVO_PIN_B
RF24 radio(7, 8);
#else
#define Steering SERVO_PIN_A
RF24 radio(8, 7);
#endif

RF24Network network(radio);
int node_controller, raceType = 0, lastGate, oldSpeed = 0, gunUse = 0, led = 0;
bool ledState[3];

bool gunReady = true, turboUse = false, turboReady = true, stopY = false, hasBeenShot = false, controllerConnected = false, reverseStop = false;

PWMServo myservo;

IRsend irsend;

IRrecv *irrecvs[2];
decode_results results;

elapsedMillis tGun, tTurbo, tTurboFlash, tHasBeenShot, tHasBeenShotFlash, tRaceType, codeFlash, controllerUpdate;

void setup() {
	myservo.attach(Steering);
	myservo.write(90);
	digitalWrite(FWRpin, LOW);
	digitalWrite(REVpin, LOW);
	pinMode(FWRpin, OUTPUT);
	pinMode(REVpin, OUTPUT);
	digitalWrite(FWRpin, LOW);
	digitalWrite(REVpin, LOW);
	R.setOutput();
	R.setHigh();
	G.setOutput();
	G.setHigh();
	B.setOutput();
	B.setHigh();
	radio.begin();
	radio.setDataRate(RF24_250KBPS);
	radio.setRetries(0, 15);
	radio.setCRCLength(RF24_CRC_16);
	radio.setPALevel(RF24_PA_MAX);
	node_controller = THIS_NODE + 010;
	network.begin(Channel, THIS_NODE);
	irrecvs[0] = new IRrecv(Receiver1);
	irrecvs[0]->enableIRIn();
	irrecvs[1] = new IRrecv(Receiver2);
	irrecvs[1]->enableIRIn();
}

void loop() {
	network.update();
	nRF_receive();
	if (raceType > 0) {
		if (raceType > 1) checkDamageState();
		if (raceType > 1) checkShootState();
		checkTurboState();
		checkIRState();
	}
	else checkRaceType();
	if (controllerConnected && controllerUpdate > 500) {
		ledState[2] = B.getState();
		controllerConnected = false;
		if (!stopY) stopY = true;
	}
	if (stopY) {
		digitalWrite(FWRpin, HIGH);
		digitalWrite(REVpin, HIGH);
	}
	checkCode();
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
			cPayload a;
			network.read(header, &a, sizeof(a));
			controllerUpdate = 0;
			if (!controllerConnected) {
				controllerConnected = true;
				if (ledState[2]) B.setHigh();
				else B.setLow();
				if (!hasBeenShot && stopY) stopY = false;
			}
			else handle_controller(a.X, a.Y, a.LB, a.RB);
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
	if (X < 89) X = map(X, 0, 88, 15, 90);
	else if (X > 91) X = map(X, 92, 180, 90, 165);
	else X = 90;
	myservo.write(X);
	int maxValue = 240;
	if (!turboUse) maxValue = 175;
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
		if (pwm > 240) pwm = 240;
		else if (pwm < 0) pwm = 0;
		if (reverse) {
			digitalWrite(FWRpin, LOW);
			analogWrite(REVpin, pwm);
		}
		else {
			analogWrite(FWRpin, pwm);
			digitalWrite(REVpin, LOW);
		}
		oldSpeed = Speed;
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

void checkCode() {
	if (raceType == 0 || !controllerConnected) {
		if (codeFlash > 333) {
			R.toggleState();
			if (raceType == 0) G.toggleState();
			if (!controllerConnected) B.toggleState();
			else if (raceType == 0) B.setHigh();
			codeFlash = 0;
		}
	}
	else if (!R.getState()) R.setHigh();
}

void checkDamageState() {
	if (hasBeenShot && tHasBeenShot > 3000) {
		setState();
		hasBeenShot = false;
		oldSpeed = 256;
		stopY = false;
	}
	else if (reverseStop && tHasBeenShot > 450) {
		reverseStop = false;
	}
	else if (hasBeenShot) {
		if (tHasBeenShotFlash > 100) {
			tHasBeenShotFlash = 0;
			Toggle(led);
			led++;
			if (led == 3) led = 0;
		}
	}
}

void checkShootState() {
	if (gunUse == 1) {
		tGun = 0;
		B.setLow();
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
		gunReady = true;
		B.setHigh();
	}
}

void checkTurboState() {
	if (turboUse && turboReady) {
		G.setLow();
		tTurbo = 0;
		turboReady = false;
	}
	else if (!turboReady && tTurbo > 10000) {
		G.setHigh();
		turboReady = true;
	}
	else if (!turboReady && tTurbo > 5000) {
		if (turboUse) turboUse = false;
		if (tTurboFlash > 150) {
			G.toggleState();
			tTurboFlash = 0;
		}
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
					getState();
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
	switch (i) {
	case 1:
		R.setHigh();
		G.setHigh();
		B.setLow();
		break;
	default:
		R.setHigh();
		G.setHigh();
		B.setHigh();
		break;
	}
}

void Toggle(int i) {
	if (i == 0) R.toggleState();
	else if (i == 1) G.toggleState();
	else B.toggleState();
}

void getState() {
	ledState[0] = R.getState();
	ledState[1] = G.getState();
	ledState[2] = B.getState();
}

void setState() {
	if (ledState[0]) R.setHigh();
	else R.setLow();
	if (ledState[1]) G.setHigh();
	else G.setLow();
	if (ledState[2]) B.setHigh();
	else B.setLow();
}

void sendHost(char command, int value) {
	hPayload p = { command,value };
	RF24NetworkHeader header(0);
	int retry = 0;
	while (true) {
		if (network.write(header, &p, sizeof(p))) break;
		else retry++;
		if (retry == 5) break;
		delay(1);
	}
}