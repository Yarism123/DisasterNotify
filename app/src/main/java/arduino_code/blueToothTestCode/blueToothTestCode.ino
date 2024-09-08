#include <SoftwareSerial.h> // 소프트웨어 시리얼 라이브러리 포함
#define BT_RXD 8
#define BT_TXD 7
SoftwareSerial bluetooth(BT_RXD,BT_TXD);

void setup(){
  Serial.begin(9600);
  bluetooth.begin(9600);
}

void loop(){
  if(bluetooth.available()){
    Serial.write(bluetooth.read());
  }
  if(Serial.available()){
    bluetooth.write(Serial.read());
  }
}
