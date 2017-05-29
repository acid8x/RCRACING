#include <EEPROM.h>
#include <Pin.h>
#include <PWMServo.h>
#include <RF24Network.h>
#include <RF24.h>
#include <IRremote.h>
#include <elapsedMillis.h>

struct hPayload { char command; int argument; };
struct cPayload { int X; int Y; bool LB; bool RB; bool configButton; };

/******************/
#define CAR_NODE 04
/******************/

Pin led[3]{ Pin(A0),Pin(A1),Pin(A2) }; // R, G, B

Pin FWR = Pin(6);
Pin REV = Pin(5);

#if CAR_NODE < 5
int Steering = SERVO_PIN_A;
int CONTROLLER_NODE = CAR_NODE + 010;
#else
int Steering = -1;
int CONTROLLER_NODE = -1;
#endif

RF24 radio(7, 8);
RF24Network network(radio);

bool flashLed[3]{ true,true,true };

int raceType = 0,
	lastGate,
	gunUse = 0,
	Speed = 0,
	oldSpeed,
	centerX,
	maxX,
	minX,
	configOption = 2,
	rainbowPin = 0,
	reverseValue;

bool gunReady = true,
	turboUse = false,
	turboReady = true,
	stopY = true,
	hasBeenShot = false,
	reverseStop = false,
	configMode = false,
	configPress = false,
	configHold = false,
	configRelease = false,
	code99 = false,
	forceStop = false;

long counting = 0;

PWMServo myservo;

IRsend irsend;

IRrecv *irrecvs[2];
decode_results results;

elapsedMillis flash, tGun, tTurbo, tHasBeenShot, tRaceType, controllerUpdate, tRainbow, reverseTimer;

void setup() {
	EEPROM.begin();
	readEEPROM();
	if (Steering != -1) myservo.attach(Steering);
	myservo.write(centerX);
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
	network.begin(115, CAR_NODE);
	irrecvs[0] = new IRrecv(2);
	irrecvs[0]->enableIRIn();
	irrecvs[1] = new IRrecv(4);
	irrecvs[1]->enableIRIn();
}

void loop() {
	network.update();
	nRF_receive();
	UpdateSpeed(Speed);
	if (!hasBeenShot && flash > 200) updateLeds();
	if (!configMode) {
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
}

void nRF_receive(void) {
	if (network.available()) {
		RF24NetworkHeader header;
		network.peek(header);
		if (header.from_node == 0) {
			hPayload p;
			network.read(header, &p, sizeof(p));
			handle_host(p.command, p.argument);
		}
		else {
			cPayload a; // X, Y, LB, RB, CB
			network.read(header, &a, sizeof(a));
			controllerUpdate = 0;
			if (!hasBeenShot && stopY) {
				stopY = false;
				if (gunReady) setLed(2, 1);
				else setLed(2, 0);
			} else handle_controller(a.X, a.Y, a.LB, a.RB, a.configButton);
		}
	}
}

void handle_controller(int X, int Y, bool LB, bool RB, bool configButton) {
	configTrigger(LB, RB, configButton);
	if (!configMode) {
		if (!RB && gunReady) {
			sendHost('S', 0);
			gunUse = 1;
			gunReady = false;
		}
		if (!LB && turboReady) {
			turboUse = true;
			turboReady = false;
		}
	}
	if (X <= 90) X = map(X, 0, 90, minX, centerX);
	else if (X > 90) X = map(X, 91, 180, centerX, maxX);
	if (Steering != -1) myservo.write(X);
	int maxValue = 255;
	if (!turboUse) maxValue = 200;
	Speed = map(Y, 0, 255, maxValue*-1, maxValue);
}

void UpdateSpeed(int value) {
  if (stopY) value = 0;
  else if (value == oldSpeed) return;
  else oldSpeed = value;
  if (reverseStop) {
    if (reverseTimer > 333) reverseStop = false;
    else value = reverseValue;
  }
  bool reverse = false;
  if (value < 0) reverse = true;
  if (reverse) value *= -1;
  if (value > 255) value = 255;
  else if (value < 20) value = 0;
  if (reverse) {
    FWR.setDutyCycle(0);
    REV.setDutyCycle(value);
  }
  else {
    FWR.setDutyCycle(value);
    REV.setDutyCycle(0);
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
		forceStop = argument;
		code99 = argument;
		stopY = argument;
		setRaceType(raceType);
		break;
	case 'G':
		lastGate = argument;
		break;
	}
}

void checkDamageState() {
	if (hasBeenShot && tHasBeenShot > 3000) {
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
	else if (hasBeenShot || code99) {
		if (tRainbow > 75) {
			led[rainbowPin++].toggleState();
			if (rainbowPin == 3) rainbowPin = 0;
			tRainbow = 0;
		}
	}
}

void checkShootState() {
	if (gunUse == 1) {
		tGun = 0;
		setLed(2, 0);
		irsend.sendSony(CAR_NODE + 10, 12);
		gunUse++;
	}
	else if (gunUse == 2 && tGun > 40) {
		gunUse++;
		irsend.sendSony(CAR_NODE + 10, 12);
	}
	else if (gunUse == 3 && tGun > 80) {
		gunUse++;
		irsend.sendSony(CAR_NODE + 10, 12);
	}
	else if (gunUse == 4 && tGun > 120) {
		gunUse = 0;
		irsend.sendSony(CAR_NODE + 10, 12);
		irrecvs[0]->enableIRIn();
		irrecvs[1]->enableIRIn();
	}
	else if (!gunReady && tGun > 3000) {
		gunReady = true;
		setLed(2, 1);
	}
}

void checkTurboState() {
	if (turboUse && turboReady) {
		setLed(1, 0);
		tTurbo = 0;
		turboReady = false;
	}
	else if (!turboReady && tTurbo > 10000) {
		setLed(1, 1);
		turboReady = true;
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
			if (results.value < 16 || results.value == 99) value[i] = results.value;
			irrecvs[i]->resume();
		}
	}
	int code = 0;
	if (value[0] != -1 || value[1] != -1) {
		if (value[1] == value[0]) code = value[0];
		else if (value[0] == -1 && (value[1] < 16 || value[1] == 99)) code = value[1];
		else if (value[1] == -1 && (value[0] < 16 || value[0] == 99)) code = value[0];
	}
	if (code > 0) {
		if (raceType > 0 && raceType < 4) {
			if (code99 && code < 10 && !forceStop) code99 = false;
			if (raceType > 1 && code > 10) {
				if (tHasBeenShot > 5000) {
					sendController(1);
					tHasBeenShot = 0;
					hasBeenShot = true;
					stopY = true;
					reverseStop = true;
					reverseValue = Speed * -1;
					sendHost('D', code - 10);
				}
			}
			else if (raceType < 3 && code < 10) {
				if (lastGate != code) {
					lastGate = code;
					sendHost('G', code);
				}
			}
			else if (code == 66) {
				code99 = true;
				stopY = true;
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

void sendController(unsigned int message) {
	if (Steering == -1) return;
	RF24NetworkHeader header(CONTROLLER_NODE);
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

void readEEPROM() {
	delay(100);
	minX = EEPROM.read(0);
	delay(100);
	if (minX > 180) {
		minX = 0;
		EEPROM.write(0, minX);
		delay(100);
	}
	centerX = EEPROM.read(1);
	delay(100);
	if (centerX > 180) {
		centerX = 90;
		EEPROM.write(1, centerX);
		delay(100);
	}
	maxX = EEPROM.read(2);
	delay(100);
	if (maxX > 180) {
		maxX = 180;
		EEPROM.write(2, maxX);
		delay(100);
	}
}

void updateEEPROM() {
	delay(100);
	EEPROM.update(0, minX);
	delay(100);
	EEPROM.update(1, centerX);
	delay(100);
	EEPROM.update(2, maxX);
	delay(100);
}

void configTrigger(bool LB, bool RB, bool configButton) {
	if (configButton && configPress) {
		counting = 0;
		configPress = false;
		configHold = false;
		configRelease = true;
	}
	else if (configButton && configRelease) configRelease = false;
	else if (!configButton && !configPress) configPress = true;
	else if (!configButton && configPress) configHold = true;
	if (configPress && counting == 0) counting = millis();
	if (counting > 0 && millis() - counting > 3000) {
		if (configMode == true) {
			configMode = false;
			updateEEPROM();
			setLed(0, 2);
			setLed(1, 2);
			setLed(2, 2);
		}
		else {
			configMode = true;
			configOption = 0;
			setLed(0, 2);
			setLed(1, 0);
			setLed(2, 0);
		}
		counting = -1;
	}
	else if (configRelease && configMode == true) {
		setLed(configOption, 0);
		configOption++;
		if (configOption == 3) configOption = 0;
		setLed(configOption, 2);
	}
	if (configMode) {
		if (!RB) {
			if (configOption == 0) maxX++;
			else if (configOption == 1) minX++;
			else if (configOption == 2) centerX++;
		}
		if (!LB) {
			if (configOption == 0) maxX--;
			else if (configOption == 1) minX--;
			else if (configOption == 2) centerX--;
		}
	}
}
