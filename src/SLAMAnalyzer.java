import com.alee.utils.ArrayUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class SLAMAnalyzer {
    private LidarReading currentReadings[];
    private LidarReading previousReadings[];
    private ArrayList<Landmark> currentLandmarks;
    private ArrayList<Landmark> previousLandmarks;

    private int spikeThreshold = 500;

    SLAMAnalyzer()
    {
        currentReadings = new LidarReading[360];
        previousReadings = new LidarReading[360];
        currentLandmarks = new ArrayList<>();
        previousLandmarks = new ArrayList<>();

    }

    void analyzeNewReadings(LidarReading[] newReadings)
    {
        //Store all old values for analysis.
        previousReadings = Arrays.copyOf(currentReadings, 360); //Might not work b/c uninititalized array.
        currentReadings = newReadings;
        previousLandmarks = new ArrayList<>(currentLandmarks); //Might not work b/c uninititalized array.
        currentLandmarks = new ArrayList<Landmark>();
        detectLandmarks();

    }

    ArrayList<Landmark> getLandmarks()
    {
        return currentLandmarks;
    }


   /*
   ArrayList<Landmark> detectLandmarks()
    {
        int unassignedIndexes[] = new int[360];
        for(int i = 0; i < unassignedIndexes.length; i++)
        { unassignedIndexes[i] = i; }

        int numRevisions = 20;
        int checkRange = 10; //Check in a range of 10 points from selected point.
        int randomPointCheck = 5; //Checks 5 random points withing checkrange
        int maxPointRetry = 10; //Will only retry attempt to check upto ten points in range.
        int inclusionRange = 20; //If within Xmm of best fit line, include in line landmark.
        int minimumPointInclusion = 20;

        ArrayList<Integer> landmarksFound = new ArrayList<>();

        for(int i = 0; i < numRevisions; i++)
        {
            if(unassignedIndexes.length < randomPointCheck)
            { break; }

            LidarReading[] selectedReadings = new LidarReading[randomPointCheck]; //Create arr for 5 random points

            int centerSearchIndex;
            while (true) {

                centerSearchIndex = unassignedIndexes[(int)(Math.random() * unassignedIndexes.length)]; //Generate random num that isn't already part of a landmark.

                int numRetrys = 0;
                boolean goodPoint = true;
                for (int j = 0; j < randomPointCheck; j++) {
                    LidarReading r = unassignedReadings[wrap360(centerSearchIndex + (int) (checkRange / 2 - Math.random() * (checkRange + 1)))];
                    if(r == null) {break;}
                    selectedReadings[j] = r;
                    if (selectedReadings[j].getInvalid()) { //Make sure point is useable
                        j--; //Erase point
                        numRetrys++;
                    }
                    if (numRetrys >= maxPointRetry) {
                        goodPoint = false;
                        break;
                    }
                }
                 for(LidarReading r:selectedReadings) //Remove selected readings from ArrayUtils.
                {
                    int ind = Arrays.binarySearch(unassignedReadings, r);
                    unassignedReadings[ind] = null;
                    unassignedIndexes = ArrayUtils.remove(unassignedIndexes, ind);
                }
                if(goodPoint){break;}
            }

            //Calc Line slope and Int
            double xAvg = 0;
            double yAvg = 0;
            for(LidarReading l :selectedReadings)
            {
                xAvg += l.getX();
                yAvg += l.getY();
            }
            xAvg /= selectedReadings.length;
            yAvg /= selectedReadings.length;

            double mH = 0;
            double mL = 0;
            for (LidarReading l: selectedReadings)
            {
                mH += (l.getX()-xAvg)*(l.getY() - yAvg); //top
                mL += (l.getX() - xAvg) * (l.getX() - xAvg); //Square bottom
            }
            double m = mH/mL;
            System.out.println(m);
            if(Double.isNaN(m)) //If line isn't valid, retry to find a valid one.
            {
                i--;
            }
            else
            {
                double a = m;
                double b = 1.0;
                double c = yAvg - xAvg*m;

                int ctr = 0;
                int goodPoints = 0;
                LidarReading lowerPoint;
                LidarReading upperPoint;
                while (true)
                {
                    LidarReading r = unassignedReadings[wrap360(centerSearchIndex-ctr)];
                    if(r != null)
                    {
                        double distance = Math.abs(a*r.getX() + b*r.getY() + c)/
                                            Math.sqrt(Math.pow(a,2.0) + Math.pow(b,2.0));

                        if(distance < inclusionRange)
                        {
                            ctr++;
                        }
                        else
                        {
                            break;
                        }

                    }
                    double distance = Math.abs(a*r.getX() + b*r.getY() + c);
                }
                WallLandmark l = new WallLandmark();
                l.setLandmark(currentReadings[centerSearchIndex], 2);
                l.setSlope(m);
                currentLandmarks.add(l);
            }
            //System.out.println(m);
        }

        for(int i = 1; i < 359; i++)
        {
            int a = currentReadings[i+1].getDistance();
            int b = currentReadings[i].getDistance();
            int c = currentReadings[i-1].getDistance();
            if((a-b + c-b) > spikeThreshold)
            {
                //System.out.println(a-b + c-b );
                Landmark lm = new Landmark();
                lm.setLandmark(currentReadings[i], 1);
                currentLandmarks.add(lm);
            }
        }
        return currentLandmarks;

    }
    */

   void detectLandmarks()
   {
       boolean[] registeredPoints = new boolean[360]; //Keeps track of points that are part of a landmark.
       for(int i = 0; i < registeredPoints.length; i++){registeredPoints[i] = false;}//init array

       int numRevisions = 20;
       int checkRange = 10; //Check in a range of 10 points from selected point.
       int randomPointCheck = 5; //Checks 5 random points withing checkrange
       int maxPointRetry = 10; //Will only retry attempt to check upto ten points in range.
       int inclusionRange = 20; //If within Xmm of best fit line, include in line landmark.
       int minimumPointInclusion = 20;
       for(int p = 0; p < numRevisions; p++)
       {
           //RANSAC
           boolean whileFlag = false;
           int retrys = 0;
           int centerIndex = 0;
           while (!whileFlag) //Find a valid point to check.
           {
               centerIndex = (int)(Math.random()*(currentReadings.length + 1)); //Random index 0<x<360
               int validCount = 0;
               for(int i = 0; i <= checkRange/2; i++) //Check points "spreading" away from center. If there are atleast enough to check, continue.
               {
                   int upperIndex = wrap360(centerIndex+i);
                   int lowerIndex = wrap360(centerIndex-i);
                   if(!currentReadings[upperIndex].getInvalid() && !registeredPoints[upperIndex])
                   { validCount++; }
                   if(!currentReadings[lowerIndex].getInvalid() && !registeredPoints[lowerIndex])
                   { validCount++; }
                   if(randomPointCheck + 1 < validCount)
                   {
                       whileFlag = true;
                       break;
                   }
               }
               retrys++;
               //Check if index can even be used
               if(retrys > maxPointRetry) //All points that could have been used have already been used, return.
               {return;}
           }
           LidarReading[] selectedPoints = new LidarReading[randomPointCheck];
           int validCount = 0;
           for(int i = 0; i <= checkRange/2; i++) //Check points "spreading" away from center. If there are atleast enough to check, continue.
           {
               LidarReading[] readingsArrPortion = new LidarReading[checkRange];
               boolean registerArrPortion[] = new boolean[checkRange];
               int ctr = 0;
               int upperIndex = wrap360(centerIndex+i);
               int lowerIndex = wrap360(centerIndex-i);

               if(!currentReadings[upperIndex].getInvalid() && !registeredPoints[upperIndex])
               {
                   selectedPoints.
                   validCount++;
               }
               if(!currentReadings[lowerIndex].getInvalid() && !registeredPoints[lowerIndex])
               {
                   validCount++;
               }
               if(randomPointCheck + 1 < validCount)
               {
                   whileFlag = true;
                   break;
               }
           }




       }
   }

   double[] leastSquareLine(LidarReading[] readings)
   {
       //Calc Line slope and Int
       double xAvg = 0;
       double yAvg = 0;
       for(LidarReading l :readings)
       {
           xAvg += l.getX();
           yAvg += l.getY();
       }
       xAvg /= readings.length;
       yAvg /= readings.length;

       double mH = 0;
       double mL = 0;
       for (LidarReading l: readings)
       {
           mH += (l.getX()-xAvg)*(l.getY() - yAvg); //top
           mL += (l.getX() - xAvg) * (l.getX() - xAvg); //Square bottom
       }
       double m = mH/mL;
       System.out.println(m);
       if(Double.isNaN(m)){return new double[3];} //If line isn't valid, return array with all nulls.
       else{return new double[]{m,xAvg,yAvg};}
   }
    int wrap360(int index) //Constrain between 0 and 259 to prevent OOB error.
    {
        if(index >= 360)
        { return index - 360; }
        else if(index < 0)
        { return 360 + index; }
        return index;
    }




}
