import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;

public class BotWindow {
    private JFrame mainFrame;
    private visualizationPanel mainVis; //MainVisualization
    private JPanel visAdjust;
    private int mainWidth = 750;
    private int mainHeight = 500;
    private int contentHeight;
    private int contentWidth;
    private JPanel infoPanel;
    private String lidarSerialPort;
    private int lidarBaudRate;
    private String movementDriverSerialPort;
    private int movementDriverBaudRate;
    private static final int baudRates[] = {9600, 115200, 230400, 250000};
    private Lidar lds;
    private boolean lidarConnected;
    private BotDriver returnTo;
    int drawRadius = 750;
    Landmark currentLandmarks;

    public BotWindow(BotDriver returnTo)
    {
        lidarBaudRate = 115200; //Default Baudrate
        lidarSerialPort = "COM10";
        movementDriverBaudRate = 115200; //Default Baudrate
        this.returnTo = returnTo;
        lidarConnected = false;
        PrepareWindow();
    }
    private void PrepareWindow()
    {
        try {
            UIManager.setLookAndFeel("com.alee.laf.WebLookAndFeel");
        }
        catch (Exception e){
            System.out.println(e);
        }
        mainFrame = new JFrame("Lidar");
        mainFrame.setSize(mainWidth,mainHeight);
        mainFrame.setLayout(null);
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
        JMenuBar jmb = SetupMenuBar();
        mainFrame.setJMenuBar(jmb);

        visAdjust = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.gridx = 1;

        visAdjust.setBackground(new Color(35,35,35));
        mainFrame.add(visAdjust);

        JSlider scaleAdjustSlider = new JSlider(30,4999, 1000);
        scaleAdjustSlider.setMajorTickSpacing(1000);
        scaleAdjustSlider.setBackground(new Color(35,35,35));
        scaleAdjustSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                mainVis.setDrawRaduis(scaleAdjustSlider.getValue());
                mainVis.repaint();
            }
        });
        scaleAdjustSlider.setVisible(true);
        visAdjust.add(scaleAdjustSlider, gbc);
        visAdjust.setVisible(true);


        mainFrame.getContentPane().setPreferredSize(new Dimension(mainWidth, mainHeight));
        mainFrame.pack();
        contentHeight = mainFrame.getContentPane().getHeight();
        contentWidth = mainFrame.getContentPane().getWidth();
        mainVis = new visualizationPanel();
        mainVis.refreshBounds(contentWidth, 0, contentHeight, contentHeight);
        mainVis.setVisible(true);
        mainFrame.add(mainVis);

        infoPanel = new JPanel();
        infoPanel.setBounds(0,0,contentWidth-contentHeight, contentHeight);
        infoPanel.setBackground(new Color(0,0,0));
        mainFrame.add(infoPanel);


        mainFrame.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                UpdateWindowLayout();
            }

            @Override
            public void componentMoved(ComponentEvent e) {

            }

            @Override
            public void componentShown(ComponentEvent e) {

            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        }); //Handles events for window resize, movement, etc... Run almost last!
        mainFrame.setVisible(true);

    }



    private void UpdateWindowLayout()
    {
        int mainVisAdjustPanelHeight = 30; //amount of px to give to adjustment tools.

        Rectangle currentRect = mainFrame.getBounds();
        mainHeight = (int) currentRect.getHeight();
        mainWidth = (int) currentRect.getWidth();
        contentHeight = mainFrame.getContentPane().getHeight();  //Width and height are flipped in swing...?
        contentWidth = mainFrame.getContentPane().getWidth();
        mainVis.refreshBounds(contentWidth - contentHeight + mainVisAdjustPanelHeight, 0, contentHeight - mainVisAdjustPanelHeight, contentHeight - mainVisAdjustPanelHeight);
        infoPanel.setBounds(0,0,contentWidth - contentHeight + mainVisAdjustPanelHeight, contentHeight);
        visAdjust.setBounds(contentWidth - contentHeight + mainVisAdjustPanelHeight, contentHeight - mainVisAdjustPanelHeight, contentHeight - mainVisAdjustPanelHeight, mainVisAdjustPanelHeight );

    }

    private JMenuItem connectToLidar;
    private JMenuBar SetupMenuBar()
    {
        JMenuBar mb = new JMenuBar();
            mb.add(createToolsMenu());
            mb.add(createViewMenu());
        return mb;
    }

    private void SetupSerialMenu(JMenu m, int commandID, String currentCOMVal, int currentBaudVal)
    {
        m.removeAll();
        String ports[] = Lidar.GetOpenPortStrings();
        AddRadioStringArray(m, commandID, currentCOMVal, ports);
        m.addSeparator();
        JMenu baudSelect = new JMenu("Baudrate");
        m.add(baudSelect);
        String stringArr[] = Arrays.toString(baudRates).split("[\\[\\]]")[1].split(", ");
        AddRadioStringArray(baudSelect, commandID+2, Integer.toString(currentBaudVal), stringArr);
    }
    private void AddRadioStringArray(JMenu m, int commandID, String currentVal, String[] vals)//Menu to add under,
    // commandtype for return/ID in event handler (passinvar), current val for comparison/selected.
    {
        ButtonGroup b = new ButtonGroup();

        for(String p:vals)
        {
            JRadioButtonMenuItem mi = new JRadioButtonMenuItem(p);
            m.add(mi);
            b.add(mi);
            if(p.equals(currentVal))
            {
                mi.setSelected(true);
            }
            mi.addActionListener(new GUIEventHandler(commandID));
            //Commandtype is returned when event is called for itemmenu event identification. See PassInVar.
        }

    }

    private JMenu createToolsMenu()
    {
        JMenu serial = new JMenu("Serial");
            JMenu lidarSerialPortM = new JMenu("Lidar Serial Port");
                lidarSerialPortM.addMenuListener(new MenuListener() {
                    @Override
                    public void menuSelected(MenuEvent e) {
                        SetupSerialMenu(lidarSerialPortM, 1, lidarSerialPort, lidarBaudRate);
                    }
                    @Override
                    public void menuDeselected(MenuEvent e) {}
                    @Override
                    public void menuCanceled(MenuEvent e) { }
                });
            JMenu movementDriverSerialPortM = new JMenu("Movement Driver Serial Port");
                movementDriverSerialPortM.addMenuListener(new MenuListener() {
                @Override
                public void menuSelected(MenuEvent e) {
                    SetupSerialMenu(movementDriverSerialPortM, 2, movementDriverSerialPort, movementDriverBaudRate);
                }
                @Override
                public void menuDeselected(MenuEvent e) {}
                @Override
                public void menuCanceled(MenuEvent e) { }
            });

            connectToLidar = new JMenuItem("Connect To Lidar");
                connectToLidar.addActionListener(new GUIEventHandler(5));
        serial.add(movementDriverSerialPortM);
        serial.add(lidarSerialPortM);
        serial.addSeparator();
        serial.add(connectToLidar);
        return serial;
    }

    private JMenu createViewMenu()
    {
        JMenu view = new JMenu("View");
            JMenuItem togSweep = new JMenuItem("Toggle Sweep");
                togSweep.addActionListener(new GUIEventHandler(7));
                view.add(togSweep);
            JMenuItem openGraphMenu = new JMenuItem("Open Graph");
                openGraphMenu.addActionListener(new GUIEventHandler(8));

        return view;
    }


    private void LidarConnectToDisconnect()
    {
        connectToLidar.setText("Disconnect From Lidar");
        connectToLidar.removeActionListener(connectToLidar.getActionListeners()[0]);
        connectToLidar.addActionListener(new GUIEventHandler(6));
    }

    private void LidarDisconnectToConnect()
    {
        connectToLidar.setText("Connect To Lidar");
        connectToLidar.removeActionListener(connectToLidar.getActionListeners()[0]);
        connectToLidar.addActionListener(new GUIEventHandler(5));
    }

    void updateLidarData(LidarReading lr[], int lidarDegreePortion)
    {
        mainVis.setLidarData(lr, lidarDegreePortion);
    }
    void updateSlamData(ArrayList<Landmark> currentLandmarks){mainVis.setSlamData(currentLandmarks);}


    private class visualizationPanel extends JPanel {
        private double absHeight;
        private double absWidth;
        private Graphics2D g2d;
        private boolean drawSweep;
        private boolean mirrorX;

        private double maxRadiusmm;
        private LidarReading readings[];
        private ArrayList<Landmark> currentLandmarks;
        private int lidarDegreePortion;

        visualizationPanel() {
            setBackground(new Color(35,35,35));
            setPreferredSize(new Dimension(400, 400));
            absHeight = 500;
            absWidth = 500;
            maxRadiusmm = 2700;
            drawSweep = true;
            readings = new LidarReading[360];
            currentLandmarks = new ArrayList<>();
            mirrorX = true;
            for(int i = 0; i < readings.length; i++) //init readings array
            {
                readings[i] = new LidarReading();
            }
            lidarDegreePortion = 0;
        }

        @Override
        public void paintComponent(Graphics g) {

            super.paintComponent(g);
            //Repaint static items
            g2d = (Graphics2D) g;
            g2d.setColor(new Color(100,100,100));
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(new Line2D.Double(0, (absHeight/2), (absWidth), (absHeight/2)));
            g2d.draw(new Line2D.Double((absWidth/2), 0, (absHeight/2), absHeight));
            g2d.drawString("Lidar View", 20, 20);
            g2d.drawString("Draw Diameter: " + maxRadiusmm*2, (int)(absWidth - 130), (int)(absHeight - 20));

            //g2d.setColor(new Color(100,100,100));
            fillOvalpc(.5,.5,.02, .02);

            //Paint Dynamic stuff..
            g2d.setColor(new Color(100,180,100));
            ((Graphics2D) g).setStroke(new BasicStroke(1));
            if(drawSweep)
            {addSweep();}

            addScannedPoints();
            addSlamData();


        }

        void toggleSweep()
        {
            drawSweep = !drawSweep;
            repaint();
        }

        private void addScannedPoints()
        {
            g2d.setColor(new Color(100,180,100));
            for(LidarReading r:readings)
            {
                if(!r.getInvalid())
                {
                    double adjDist = (double)(r.getDistance())/maxRadiusmm;
                    //System.out.println(adjDist);
                    double x = (adjDist* Math.cos(r.getDegree()*(Math.PI/180))) +.5;
                    if(mirrorX){x = (adjDist* -Math.cos(r.getDegree()*(Math.PI/180))) +.5;}
                    double y = (adjDist* Math.sin(r.getDegree()*(Math.PI/180))) +.5;
                    //System.out.println("Adding at " + x + " " + y + " At degree " + r.getDegree());
                    fillOvalpc( x, y,.005,.005);
                }
            }
        }

        private void addSlamData()
        {

            g2d.setColor(new Color(200, 0,0));
            for(int i = 0; i < currentLandmarks.size(); i++)
            {
                if(currentLandmarks.get(i).getType() == 1) {
                    LidarReading r = currentLandmarks.get(i).getReading();
                    if (!r.getInvalid()) {
                        g2d.setColor(new Color(255,0,0));
                        double adjDist = (double) (r.getDistance()) / maxRadiusmm;
                        //System.out.println(adjDist);
                        double x = (adjDist * Math.cos(r.getDegree() * (Math.PI / 180))) + .5;
                        if (mirrorX) {

                            x = (adjDist * -Math.cos(r.getDegree() * (Math.PI / 180))) + .5;
                        }
                        double y = (adjDist * Math.sin(r.getDegree() * (Math.PI / 180))) + .5;
                        //System.out.println("Adding at " + x + " " + y + " At degree " + r.getDegree());
                        fillOvalpc(x, y, .005, .005);
                    }
                }
                else if(currentLandmarks.get(i).getType() == 2)
                {
                    g2d.setColor(new Color(0,0,255));
                    LidarReading r = currentLandmarks.get(i).getReading();
                    WallLandmark wl = (WallLandmark) currentLandmarks.get(i);

                    fillOvalpc(-r.getX()/maxRadiusmm + .5, r.getY()/maxRadiusmm + .5, .005, .005);
                    double x1 = -r.getX()/maxRadiusmm + .5 - .1;
                    double y1 =  r.getY()/maxRadiusmm + .5 - .1*-wl.getSlope();
                    double x2 =  -r.getX()/maxRadiusmm + .5 + .1;
                    double y2 =  r.getY()/maxRadiusmm + .5 + .1*-wl.getSlope();
                    drawLinep(x1, y1, x2, y2);
                }
            }
        }

        private void addSweep()
        {
            double x = (.5*Math.cos(lidarDegreePortion*(Math.PI/180))) +.5;
            double y = (.5*Math.sin(lidarDegreePortion*(Math.PI/180))) +.5;
            drawLinep(.5,.5,x,y);
        }

        public void refreshBounds(int x, int y, int width, int height)
        {
            absHeight = height;
            absWidth = width;
            setBounds(x, y, width, height);
            //System.out.println(height + "\t" + width);
        }

        private void fillRectpc(double xp, double yp, double widthp, double heightp)
        {
            int x = (int)((absWidth*xp)-(absWidth*widthp/2));
            int y = (int)((absHeight*yp)-(absHeight*heightp/2));
            //System.out.println(x + "\t" + y);
            g2d.fillRect(x,y,(int)(absWidth*widthp),(int)(absHeight*heightp));
            //g.f
        }

        private void drawLinep(double x1p, double y1p, double x2p, double y2p)
        {
            double x1 = absWidth*x1p;
            double y1 = absHeight*y1p;
            double x2 = absWidth*x2p;
            double y2 = absHeight*y2p;
            g2d.draw(new Line2D.Double(x1, y1, x2, y2));
            //g.f
        }

        private void fillOvalpc(double xp, double yp, double widthp, double heightp)
        {
            double x = ((absWidth*xp)-(absWidth*widthp/2));
            double y = ((absHeight*yp)-(absHeight*heightp/2));
            //System.out.println(x + "\t" + y);
            g2d.fill(new Ellipse2D.Double(x,y,(absWidth*widthp),(absHeight*heightp)));

            //g.f
        }
        void setLidarData(LidarReading[] lr, int lidarDegreePortion)
        {
            readings = lr;
            this.lidarDegreePortion = lidarDegreePortion;
            repaint();

        }
        void setSlamData(ArrayList<Landmark> currentLandmarks)
        {
            this.currentLandmarks = currentLandmarks;
            repaint();
        }

        void setDrawRaduis(int raduis)
        {
            maxRadiusmm = raduis;
        }

    }

    private class GUIEventHandler implements ActionListener {
        private int commandID;
        GUIEventHandler(int commandID)
        {
            this.commandID = commandID;
        }
        public void actionPerformed(ActionEvent e)
        {
            switch (commandID)
            {
                case 1: lidarSerialPort = e.getActionCommand(); break;
                case 2: movementDriverSerialPort = e.getActionCommand(); break;
                case 3: lidarBaudRate = Integer.parseInt(e.getActionCommand()); break;
                case 4: movementDriverBaudRate = Integer.parseInt(e.getActionCommand()); break; //Movement baudrate
                case 5:
                    returnTo.getLidar().ChangeLidarConnection(lidarSerialPort, lidarBaudRate);
                    if(returnTo.getLidar().Connect()){LidarConnectToDisconnect();} break;
                case 6: if(returnTo.getLidar().Disconnect()){LidarDisconnectToConnect();} break;
                case 7: mainVis.toggleSweep(); break;
                case 8: break;
                default: break;
            }
            //System.out.println(e.getActionCommand() + " " + commandID);


        }
    }

//Command IDs
/*
1: Lidar serial port
2: Movement serial port
3: Lidar baudrate
4: Movement Baudrate
5: Init lidar connection
6: Disconnect Lidar
*/
}



