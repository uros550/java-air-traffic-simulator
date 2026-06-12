package gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import models.Airport;
import models.AviationCenter;

public class MapCanvas extends Canvas {
    
	private final Window owner;
    private final AviationCenter aCenter;
    
    //blinking airport
    private Airport selectedAirport = null;
    private boolean blinkOn = true;
    
    private final int AIRPORT_SIZE = 10;

    public MapCanvas(Window owner, AviationCenter center) {
        this.aCenter = center;
        this.owner = owner;
     
        //map click listener
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMapClick(e.getX(), e.getY());
            }
        });
        
        //thread for blinking
        startBlinkThread();
    }

	private void handleMapClick(int x, int y) {
		if (aCenter.getAirports().isEmpty()) return;
		
		Airport clickedAirport = null;
		
		//check if any airport is clicked
		for (Airport a : aCenter.getAirports()) {
			//check visibility
			if (!a.isVisible()) continue;
			//calculate coordinates
            int pixelX = calculatePixelX(a.getX());
            int pixelY = calculatePixelY(a.getY());

            //check if clicked at or adjacent to an airport
			if (Math.abs(x - pixelX) < 10 && Math.abs(y - pixelY) < 10) {
				clickedAirport = a;
				break;
			}
        }
		
		if (clickedAirport != null) {
            if (selectedAirport == clickedAirport) {
                //clicked was already selected -> turn off
                selectedAirport = null;
                if (owner.getInactivityTimer() != null) {
                    owner.getInactivityTimer().setPaused(false); //start timer
                }
            } 
            else {
                //new airport is clicked -> turn on
                selectedAirport = clickedAirport;
                if (owner.getInactivityTimer() != null) {
                    owner.getInactivityTimer().setPaused(true); //stop timer
                }
            }
        }
		else {
			//clicked somewhere else -> turn off
			selectedAirport = null;
            if (owner.getInactivityTimer() != null) {
                owner.getInactivityTimer().setPaused(false); //start timer
            }
		}
		repaint();
	}
	
	private void startBlinkThread() {
		Thread blinkThread = new Thread(() -> {
            try {
                while (true) {
                    //blink if selected
                    if (selectedAirport != null) {
                        blinkOn = !blinkOn;
                        repaint();
                    }
                    Thread.sleep(500); //blink every half a second
                }
            } catch (InterruptedException e) {}
        });
        blinkThread.setDaemon(true);
        blinkThread.start();
	}

    @Override
    public void paint(Graphics g) {
    	super.paint(g);

    	drawAirports(g);
		drawAirplanes(g);
    }
    

	private void drawAirports(Graphics g) {
		//save previous color
		Color prevColor = g.getColor();
		//if no airports
		if (aCenter.getAirports().isEmpty()) return;
		//draw every airport
		for (Airport a : aCenter.getAirports()) {
			//check visibility
			if (!a.isVisible()) continue;
			//get actual coordinates
			int pixelX = calculatePixelX(a.getX());
			int pixelY = calculatePixelY(a.getY());
			
			//draw airport
			if (a == selectedAirport && blinkOn) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.GRAY);
            }
			g.fillRect(pixelX - AIRPORT_SIZE/2, pixelY - AIRPORT_SIZE/2, AIRPORT_SIZE, AIRPORT_SIZE);
			//draw code
			g.setColor(Color.WHITE);
			String displayCode = a.getCode().toUpperCase();
			g.drawString(displayCode, pixelX + AIRPORT_SIZE, pixelY);
		}
		
		//restore old color
		g.setColor(prevColor);
	}
	
	private void drawAirplanes(Graphics g) {
		// TODO Auto-generated method stub
		
	}

	private int calculatePixelX(double x) {
	    double stepX = (double) getWidth() / 360.0;
	    return (int) ((x + 180) * stepX);
	}

	private int calculatePixelY(double y) {
	    double stepY = (double) getHeight() / 180.0;
	    return (int) ((90 - y) * stepY);
	}
	
	
	
	//OPTIMIZATION 
	private Image offscreenImage = null;
	private Graphics offscreenGraphics = null;

	@Override
	public void update(Graphics g) {
		//if image does not exist in memory or window changed its size
	    if (offscreenImage == null || offscreenImage.getWidth(this) != getWidth() || offscreenImage.getHeight(this) != getHeight()) {
	        //create new
	    	offscreenImage = createImage(getWidth(), getHeight());
	        offscreenGraphics = offscreenImage.getGraphics();
	    }

	    //clear off screen background
	    offscreenGraphics.setColor(getBackground());
	    offscreenGraphics.fillRect(0, 0, getWidth(), getHeight());

	    //paint off screen
	    paint(offscreenGraphics);

	    //transport already painted onto real screen
	    g.drawImage(offscreenImage, 0, 0, this);
	}
    
}