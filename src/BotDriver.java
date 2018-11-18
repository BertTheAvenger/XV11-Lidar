public class BotDriver {
    //Main Classes
    private BotWindow botWindow;
    private Lidar lidar;
    private SLAMAnalyzer slam;

    //Lidar data
    private LidarReading lidarData[];
    private double lidarRPM;
    private int lidarDegreePortion;


    public static void main(String[] args)
    { new BotDriver(); }

    private BotDriver()
    {
        botWindow = new BotWindow(this);
        lidar = new Lidar(this); //Init new lidar.
        slam = new SLAMAnalyzer();

    }

    void setLidarData(LidarReading lidarData[], double lidarRPM, int lidarDegree)
    {
        this.lidarData = lidarData;
        this.lidarRPM = lidarRPM;
        this.lidarDegreePortion = lidarDegree;


        if(lidarDegreePortion >= 356) //If last readings of the revolution, analyze readings
        {
            slam.analyzeNewReadings(lidarData);
            updateGUI();
        }
    }

    Lidar getLidar()
    {
        return lidar;
    }

    private void updateGUI()
    {
        botWindow.updateLidarData(lidarData, lidarDegreePortion);
        botWindow.updateSlamData(slam.getLandmarks());
    }

}
