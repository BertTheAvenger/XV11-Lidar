public class Landmark {
    private LidarReading reading;
    private int landmarkType;
    private LidarReading[] lr;
    //0 is unset
    //1 is common spike.
    Landmark()
    {
        landmarkType = 0;
        this.reading = new LidarReading();
    }
    void setLandmark(LidarReading reading, int type)
    {
        landmarkType = type;
        this.reading = reading;
    }
    LidarReading getReading()
    {
        return reading;
    }
    int getType(){return landmarkType;}

    void setReadings(LidarReading[] lr)
    {
        this.lr = lr;
    }


}
