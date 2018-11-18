import com.fazecast.jSerialComm.*;
import java.util.Arrays;
class Lidar {
    private String serialPortName;
    private int baudrate;
    private SerialPort lidarSerial;
    private int rawPacket[];
    private BotDriver returnTo;
    private LidarReading readings[];
    private int currentLowestDegree;
    private double currentRPM;
    private boolean portOpen;

    Lidar(BotDriver botDriver)
    {
        this.returnTo = botDriver;
        this.serialPortName = "";
        this.baudrate = 0;
        rawPacket = new int[22]; //Each packet is 22 bytes long.
        readings = new LidarReading[360];
        currentLowestDegree = 0;
        currentRPM = 0;
        portOpen = false;
        for(int i = 0; i < readings.length; i++) //init readings array
        {
            readings[i] = new LidarReading();
        }
    }
    Lidar(String serialPortName, int baudrate, BotDriver botDriver)
    {
        this.returnTo = botDriver;
        this.serialPortName = serialPortName;
        this.baudrate = baudrate;
        rawPacket = new int[22]; //Each packet is 22 bytes long.
        readings = new LidarReading[360];
        currentLowestDegree = 0;
        currentRPM = 0;
        portOpen = false;
        for(int i = 0; i < readings.length; i++) //init readings array
        {
            readings[i] = new LidarReading();
        }
    }

    void ChangeLidarConnection(String serialPortName, int baudrate)
    {
        this.serialPortName = serialPortName;
        this.baudrate = baudrate;
    }

    boolean Connect()
    {
       lidarSerial = SerialPort.getCommPort(serialPortName);
       if(!portOpen)
       {
           System.out.println("Connecting!");
           lidarSerial.setBaudRate(baudrate);
           boolean result = lidarSerial.openPort();
           if (result) //Make sure we succeeded
           {
               System.out.println("Connected!");
               lidarSerial.addDataListener(new LidarEventHandler(this, lidarSerial));
               portOpen = true;
               return true;
           }
       }
       System.out.println("Error Connecting!");
       return false;
    }

    boolean Disconnect()
    {
        if (portOpen) {
            System.out.println("Disconnecting!");
            lidarSerial.removeDataListener();
            boolean result = lidarSerial.closePort();
            portOpen = !result;
            return result;
        }
        else{ return false; }
    }

    static String[] GetOpenPortStrings() //Returns array of port names, used to generate for user in window.
    {
        SerialPort ports[] = SerialPort.getCommPorts();
        String[] portNames = new String[ports.length];
        for(int i = 0; i < ports.length; i++)
        {
           portNames[i] = ports[i].getSystemPortName();
        }
        return portNames;
    }

    private void SetNewPacket(int[] packet) //LidarEventHandler calls to input new packets.
    {
        rawPacket = packet;
        DecodePacket();
    }

    private void DecodePacket()
    {
        //System.out.println("Decoding Packet");
        currentLowestDegree = (rawPacket[1] - 160)*4;
        if(!(currentLowestDegree <= 360 && currentLowestDegree >= 0)){return;} //Make sure degree is valid
        currentRPM = ((double)decodeLittleEndian(rawPacket[2], rawPacket[3]))/64;
        for(int i = 0; i < 4; i++)
        {
            int startIndex = 4 + i*4;

            readings[currentLowestDegree + i] = new LidarReading(currentLowestDegree + i); //create the reading
            writeDecodeReadingData(readings[currentLowestDegree + i], Arrays.copyOfRange(rawPacket, startIndex, startIndex+4)); //Write relevant vals to the reading.
            //Create LidarReadings, feeding them the four bytes per reading. Decoding of those bytes occurs inside LidarReading.
        }
        returnTo.setLidarData(readings, currentRPM, currentLowestDegree); //Constantly update driver with latest info after each new packet.
    }

    private int decodeLittleEndian(int lowerByte, int upperByte)
    {
        int result = upperByte << 8;
        return result | lowerByte;
    }

    private void writeDecodeReadingData(LidarReading reading, int[] readingBytes)
    {
        if((readingBytes[1] & 0b01000000) > 0)//01000000 is uncertain reading.
        {
            //System.out.println("UNCERTAIN");
            reading.setUncertain(true);
        }
        else if((readingBytes[1] & 0b10000000) > 0)//10000000 is invalid reading.
        {
            //System.out.println("INVALID");
            reading.setInvalid(true);
            reading.setDistance(-1);
            reading.setSignalStrength(decodeLittleEndian(readingBytes[2], readingBytes[3]));
            return;
        }
        reading.setDistance(decodeLittleEndian(readingBytes[0], (readingBytes[1] & 0b00111111)));
        reading.setSignalStrength(decodeLittleEndian(readingBytes[2], readingBytes[3]));
        reading.calcVals();
        //System.out.println(distanceReading);
    }

    private class LidarEventHandler implements SerialPortDataListener {
        private Lidar returnTo;
        private int rawPacketIn[];
        private int currentPacketIndex;
        private SerialPort serialPort;
        private boolean readingByte;
        private LidarEventHandler(Lidar lidar, SerialPort serialPort)
        {
            returnTo = lidar;
            currentPacketIndex = 0;
            rawPacketIn = new int[22];
            this.serialPort = serialPort;
            readingByte = false;
        }
        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
        }

        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                return;
            while (serialPort.bytesAvailable() > 0) {
                byte bytesIn[] = new byte[1];
                serialPort.readBytes(bytesIn, 1);
                //System.out.println("BYTES IN!");
                byte b = bytesIn[0];
                    int Ub = Byte.toUnsignedInt(b);
                    //System.out.println(Ub);

                    if ((b == (byte) 250) && !readingByte) {
                        //System.out.println("Starting Packet!");
                        readingByte = true;
                        rawPacketIn[currentPacketIndex] = Ub;
                        currentPacketIndex++;

                    } else if (currentPacketIndex == 20) {
                        rawPacketIn[currentPacketIndex] = Ub;
                        currentPacketIndex = 0;
                        readingByte = false;
                        returnTo.SetNewPacket(rawPacketIn);
                    } else if (readingByte) {
                        rawPacketIn[currentPacketIndex] = Ub;
                        currentPacketIndex++;
                    }
                }
            }
        }

}

