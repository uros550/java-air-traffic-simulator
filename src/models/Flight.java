package models;

import exceptions.FlightException;

public class Flight {
    
	//flight info
    private Airport from;
    private Airport to;
    private String takeOffTime;    
    private int takeOffTimeInt;    
    private int flightDurMin;
    private boolean tookOff = false;
    
    public Flight(Airport from, Airport to, String takeOffTime, int takeOffTimeInt, int flightDurMin) throws FlightException {
    	//check airports
        if (from == null || to == null) {
            throw new FlightException("Departure and destination airports must exist!");
        }
        if (from.getCode().equals(to.getCode())) {
            throw new FlightException("Departure and destination airports cannot be the same!");
        }
        //check take off
        if (takeOffTimeInt < 0 || takeOffTimeInt >= 1440) {
            throw new FlightException("Take-off time must be between 00:00 and 23:59!");
        }
        //check duration
        if (flightDurMin <= 0) {
            throw new FlightException("Flight duration must be greater than zero!");
        }
        
        this.from = from;
        this.to = to;
        this.takeOffTime = takeOffTime;
        this.takeOffTimeInt = takeOffTimeInt;
        this.flightDurMin = flightDurMin;
    }

    //getters
    public Airport getFrom() { 
        return from; 
    }    
    public Airport getTo() { 
        return to; 
    }    
    public String getTakeOffTime() { 
        return takeOffTime; 
    }    
    public int getTakeOffTimeInt() { 
        return takeOffTimeInt; 
    }    
    public int getFlightDurMin() { 
        return flightDurMin; 
    }
    public boolean hasTakenOff() { 
        return tookOff; 
    }

    //setters
    public void setFrom(Airport from) { 
        this.from = from; 
    }
    public void setTo(Airport to) { 
        this.to = to; 
    }
    public void setTakeOffTime(String takeOffTime) { 
        this.takeOffTime = takeOffTime; 
    }
    public void setTakeOffTimeInt(int takeOffTimeInt) { 
        this.takeOffTimeInt = takeOffTimeInt; 
    }
    public void setFlightDurMin(int flightDurMin) { 
        this.flightDurMin = flightDurMin; 
    }
    public void setTookOff(boolean tookOff) { 
        this.tookOff = tookOff; 
    }
    
    
    @Override
    public String toString() {
        return String.format("%-6s | %-6s | %-10s | %-10d", from.getCode(), to.getCode(), takeOffTime, flightDurMin);
    }
}