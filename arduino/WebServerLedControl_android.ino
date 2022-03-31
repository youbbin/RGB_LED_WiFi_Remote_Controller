#include "WiFiEsp.h"
#include <Wire.h>
#include <BH1750FVI.h>

// Settings
BH1750FVI::eDeviceMode_t DEVICEMODE = BH1750FVI::k_DevModeContHighRes;

// Create the Lightsensor instance
BH1750FVI LightSensor(DEVICEMODE);

#ifndef HAVE_HWSERIAL1
#include "SoftwareSerial.h"
SoftwareSerial Serial1(2, 3); // RX, TX
#endif


char ssid[] = "WITLAB";            // your network SSID (name)
char pass[] = "*********";        // your network password
int status = WL_IDLE_STATUS;
int pinR=9;
int pinG=10;
int pinB=11;
int pinCds=A1;
double R=128;
double G=255;
double B=0;
int ledStatus = LOW;

WiFiEspServer server(80);

// use a ring buffer to increase speed and reduce memory allocation
RingBuffer buf(8);

void setup()
{
  pinMode(pinR, OUTPUT);
  pinMode(pinG, OUTPUT);
  pinMode(pinB, OUTPUT);
  pinMode(pinCds, INPUT);
  Serial.begin(115200);   // initialize serial for debugging
  Serial1.begin(9600);    // initialize serial for ESP module
  WiFi.init(&Serial1);    // initialize ESP module
  LightSensor.begin();  

  // check for the presence of the shield
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi shield not present");
    while (true);
  }

  // attempt to connect to WiFi network
  while (status != WL_CONNECTED) {
    Serial.print("Attempting to connect to WPA SSID: ");
    Serial.println(ssid);
    // Connect to WPA/WPA2 network
    status = WiFi.begin(ssid, pass);
  }

  Serial.println("You're connected to the network");
  printWifiStatus();
  
  // start the web server on port 80
  server.begin();
}


void loop()
{
  WiFiEspClient client = server.available();  // listen for incoming clients
  
  if (client) {
    // if you get a client,
    Serial.println("New client");             // print a message out the serial port
    
    buf.init();                               // initialize the circular buffer
    while (client.connected()) {              // loop while the client's connected
      if (client.available()) {               // if there's bytes to read from the client,
        char c = client.read();               // read a byte, then
        buf.push(c);                          // push it to the ring buffer
        
        // that's the end of the HTTP request, so send a response
        if (buf.endsWith("\r\n\r\n")) {
          sendHttpResponse(client);
          break;
        }
              
        if (buf.endsWith("On")) {
          Serial.println("Turn led ON");
          analogWrite(pinR, R),analogWrite(pinG, G),analogWrite(pinB, B);
          ledStatus = HIGH;
        }
        
        else if (buf.endsWith("Off")) {
          Serial.println("Turn led OFF");    
          analogWrite(pinR, 0),analogWrite(pinG, 0),analogWrite(pinB, 0);
          ledStatus = LOW;
        }
        
        else if (buf.endsWith("RGB")){ //RGB000000000 형식의 문자열 받아옴
          double r=0;
          for(int i=0;i<3;i++){
            char ch=client.read();
            r+= String(ch).toInt()*pow(10,2-i);
          }
          R=r;
          Serial.println(R);
          
          double g=0;
          for(int i=0;i<3;i++){
            char ch=client.read();
            g+= String(ch).toInt()*pow(10,2-i);
          }
          G=g;
          Serial.println(G);
          
          double b=0;
          for(int i=0;i<3;i++){
            char ch=client.read();
            b+= String(ch).toInt()*pow(10,2-i);
          }
          B=b;
          Serial.println(B);
        }
        
        else if (buf.endsWith("BRT")){ //BRT000 형식의 문자열 받아옴
          double brt=0;
          for(int i=0;i<3;i++){
            char ch=client.read();
            brt+= String(ch).toInt()*pow(10,2-i);
          }
          Serial.print("brt=");
          Serial.println(brt);
          
          //밝기 적용
          R=R*(brt/100);
          G=G*(brt/100);
          B=B*(brt/100);
          int cds=analogRead(pinCds);
          
          if(ledStatus==HIGH){
             analogWrite(pinR, R),analogWrite(pinG, G),analogWrite(pinB, B);
           }
        }
      }
    }
    // close the connection
    client.stop();
    Serial.println("Client disconnected");
  }
}


void sendHttpResponse(WiFiEspClient client)
{
  // HTTP headers always start with a response code (e.g. HTTP/1.1 200 OK)
  // and a content-type so the client knows what's coming, then a blank line:
  client.println("HTTP/1.1 200 OK");
  client.println("Content-type:text/html");
  client.println();
  uint16_t lux = LightSensor.GetLightIntensity();
  Serial.print("Light: ");
  Serial.println(lux);
  client.print("Illuminance: ");
  client.print(lux);
  client.print(" lux");
  client.print("<br>");
  
  // The HTTP response ends with another blank line:
  client.println();
}

void printWifiStatus()
{
  // print the SSID of the network you're attached to
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print your WiFi shield's IP address
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  // print where to go in the browser
  Serial.println();
  Serial.print("To see this page in action, open a browser to http://");
  Serial.println(ip);
  Serial.println();
}
