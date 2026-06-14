package models;

import java.util.LinkedList;
import java.util.Queue;

import exceptions.AirportException;

public class Airport {
	
	//airport info
	private String name;
	private final String code;
	private double x, y;
	private boolean visible = true;
	
	//queue
	private Queue<Flight> waitingQueue = new LinkedList<>();
	private int nextAvailableSlot = 0;
	
	public Airport(String name, String code, double x, double y) throws AirportException {
		//check name
		if (name == null || name.trim().isEmpty()) {
            throw new AirportException("Airport name cannot be empty!");
        }
		//check code
        if (code == null || code.length() != 3) {
            throw new AirportException("Airport code must be exactly 3 letters!");
        }
        for (int i = 0; i < 3; i++) {
            char c = code.charAt(i);
            if (c < 'A' || c > 'Z') {
                throw new AirportException("Airport code must contain only uppercase letters!");
            }
        }
        //check coordinates 
        if (x < -180.0 || x > 180.0) {
            throw new AirportException("Coordinate X must be between -180 and 180!");
        }
        if (y < -90.0 || y > 90.0) {
            throw new AirportException("Coordinate Y must be between -90 and 90!");
        }
        
		this.name = name;
		this.code = code;
		this.x = x;
		this.y = y;
	}
	
	//for reset button
	public void resetQueueState() {
	    this.waitingQueue.clear();
	    this.nextAvailableSlot = 0;
	}

	//getters
	public String getName() {
		return name;
	}
	public String getCode() {
		return code;
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public boolean isVisible() {
		return visible;
	}
	public Queue<Flight> getWaitingQueue() { 
		return waitingQueue; 
	}
	public int getNextAvailableSlot() { 
		return nextAvailableSlot; 
	}

	//setters
	public void setName(String name) {
		this.name = name;
	}
	public void setX(double x) {
		this.x = x;
	}
	public void setY(double y) {
		this.y = y;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public void setNextAvailableSlot(int nextAvailableSlot) { 
		this.nextAvailableSlot = nextAvailableSlot;
	}
	
	@Override
	public String toString() {
		return String.format("%-6s | %-35s | %-6.1f | %-6.1f", code, name, x, y);
	}
}