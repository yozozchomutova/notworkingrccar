#include <Servo.h>

const int motor_trspin = 6;

const int lights_pin = 7;
const int back_lights_pin = 3;

Servo steeringServo;

char Incoming_value = 0;                //Variable for storing Incoming_value

int val;

String bt_constructingMsg = "";
String bt_lastMessage = "";

//Acceleration only!
int acc = 0;
int last_acc = 0;

void setup() 
{
  Serial.begin(9600);
  
  pinMode(motor_trspin, OUTPUT);
  analogWrite(motor_trspin, 0);

  Serial1.begin(9600);       //Sets the data rate in bits per second (baud) for serial data transmission
  pinMode(lights_pin, OUTPUT);
  pinMode(back_lights_pin, OUTPUT);
  //digitalWrite(7, HIGH);

  steeringServo.attach(11);
}
void loop()
{  
  if (bt_lastMessage == "ang") {
    getNextSequence();
    if (bt_lastMessage != "ang") {
      int rot = bt_lastMessage.toInt();
      steeringServo.write(90 - rot);
    }
  } else if (bt_lastMessage == "acc") {
    getNextSequence();
    if (bt_lastMessage != "acc") {
      acc = bt_lastMessage.toInt();
      analogWrite(motor_trspin, acc);

      if (last_acc > acc) {
        digitalWrite(back_lights_pin, HIGH);
      } else {
        digitalWrite(back_lights_pin, LOW);
      }
      last_acc = acc;
    }
  } else if (bt_lastMessage == "lig") {
    getNextSequence();
    if (bt_lastMessage != "lig") {
      if(bt_lastMessage == "y") {
        digitalWrite(lights_pin, HIGH);
        Serial.print("L ON\n");
      } else {
        digitalWrite(lights_pin, LOW);
        Serial.print("L OFF\n");
      }
    }
  } else {
    getNextSequence();
  }
}       

String getNextSequence() {
    
  while(Serial1.available() > 0) {
    char nextVal = Serial1.read();
    //Serial.print("C: " + String(nextVal) + "\n");

    if (nextVal == ';') {
      bt_lastMessage = bt_constructingMsg;
      bt_constructingMsg = "";
      //Serial.print(bt_lastMessage);
      break;
    }

    bt_constructingMsg = bt_constructingMsg + nextVal;
  }

  return "";
}
