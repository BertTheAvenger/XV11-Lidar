

import java.awt.*;
import java.awt.event.*;

class AwtControlDemo {

    private Frame mainFrame;
    private Label headerLabel;
    private Label statusLabel;
    private Panel controlPanel;

    public AwtControlDemo(){
        prepareGUI();
    }

    public static void main(String[] args){
        AwtControlDemo  awtControlDemo = new AwtControlDemo();
    }

    private void prepareGUI(){
        mainFrame = new Frame("Java AWT Examples");
        mainFrame.setSize(400,400);
        mainFrame.setLayout(null);
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
            public void windowActivated(WindowEvent windowEvent){
                System.out.println("Activated");
            }
        });
        mainFrame.setVisible(true);
        MenuBar mb = new MenuBar();
        Menu top = new Menu("TOP");
            MenuItem kek = new MenuItem("KEK");
            kek.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("KEKed");
                }
            });
                top.add(kek);

        mb.add(top);
        mainFrame.setMenuBar(mb);
    }
}