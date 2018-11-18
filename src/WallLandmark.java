public class WallLandmark extends Landmark{
    private double[] line;
    private double slope;
    private LidarReading startPoint;
    private LidarReading endPoint;
    void setCartesianEndpoints(double x1, double y1, double x2, double y2)
    {
        double[] temp = {x1, y1, x2, y2};
        line = temp;
    }
    void setSlope(double slp)
    {
        slope = slp;
    }
    double getSlope()
    {
        return slope;
    }
    double[] getLine()
    {
        return line;
    }
}
