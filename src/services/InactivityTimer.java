package services;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;

public class InactivityTimer extends Thread {

    private volatile long lastAction;
    private final long closingTime = 60000;
    private final long warningTime = 55000;
    private final Frame parent;
    
    private volatile boolean running = true;
    private volatile boolean paused = false;
    
    private Dialog dialog = null;
    private Label label = null;

    public InactivityTimer(Frame parent) {
        this.parent = parent;
        this.lastAction = System.currentTimeMillis();
        //to be sure it wont stop program from closing if x is pressed
        setDaemon(true); 
    }

    @Override
    public void run() {
        while (running) {
            if (paused) {
                sleepThread(200);
                continue;
            }

        	//current inactive time
            long idleTime = System.currentTimeMillis() - lastAction;

            //if should close
            if (idleTime >= closingTime) {
                System.exit(0);
            } 
            //if should pop dialog
            else if (idleTime >= warningTime) {
                long timeLeft = closingTime - idleTime;
              //seconds rounding on bigger -> ceil 
                int secondsLeft = (int) Math.ceil(timeLeft / 1000.0);

                if (dialog == null) {
                    //create and show dialog
                    EventQueue.invokeLater(() -> createAndShowDialog(secondsLeft));
                } else {
                    //update dialog if exists
                    EventQueue.invokeLater(() -> {
                        if (label != null) {
                            label.setText("The program will shut down in " + secondsLeft + " seconds. Continue?");
                            dialog.pack(); //compact dialog
                        }
                    });
                }
                
                sleepThread(1000); //update every second
            } 
            else {
                sleepThread(200); //check inactivity every 200ms
            }
        }
    }

    //helper methods
    
    private void createAndShowDialog(int secondsLeft) {
        dialog = new Dialog(parent, "Inactivity warning", ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new FlowLayout());
        
        label = new Label("The program will shut down in " + secondsLeft + " seconds. Do you want to continue?");
        Button yes = new Button("Yes");
        Button no = new Button("No");

        yes.addActionListener(e -> {
            resetTimer();
            dialog.dispose();
            dialog = null; //reset to null
        });

        no.addActionListener(e -> System.exit(0));

        dialog.add(label);
        dialog.add(yes);
        dialog.add(no);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    //to not write try catch every time
    private void sleepThread(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            running = false;
        }
    }

    
    public void setPaused(boolean paused) {
        this.paused = paused;
        if (!paused) {
            resetTimer();
        }
    }

    public void resetTimer() {
        lastAction = System.currentTimeMillis();
    }
}