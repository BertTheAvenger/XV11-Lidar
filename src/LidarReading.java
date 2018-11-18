public class LidarReading {
    private int distanceReading;
    private boolean uncertainReading;
    private boolean invalidReading;
    private int readingDegree;
    private int signalStrength;
    private double x;
    private double y;
    public LidarReading() //Accepts four bytes for a data subpacket.
    {
        this.readingDegree = 0;
        this.invalidReading = false;
        this.uncertainReading = false;
    }
    public LidarReading(int degree) //Accepts four bytes for a data subpacket.
    {
        this.readingDegree = degree;
    }

    int getDistance()
    {
        return distanceReading;
    }
    int getDegree() { return readingDegree; }
    int getSignalStrength()
    {
        return signalStrength;
    }
    boolean getInvalid()
    {
        return invalidReading;
    }
    boolean getUncertain() { return uncertainReading; }

    double getX(){return x;}
    double getY(){return y;}

    void setDistance(int distanceReading) { this.distanceReading = distanceReading; }
    void setDegree(int readingDegree) { this.readingDegree = readingDegree;}
    void setInvalid(boolean invalidReading) { this.invalidReading = invalidReading; }
    void setSignalStrength(int signalStrength) { this.signalStrength = signalStrength; }
    void setUncertain(boolean uncertain){uncertainReading = uncertain;}

    void calcVals()
    {
        if(!invalidReading)
        {
            x = distanceReading*Math.cos(readingDegree*Math.PI/180);
            y = distanceReading*Math.sin(readingDegree*Math.PI/180);
        }
    }

    private int decodeLittleEndian(int lowerByte, int upperByte)
    {
        int result = upperByte << 8;
        return result | lowerByte;
    }
}
