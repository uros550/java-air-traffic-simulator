package models;

import java.util.ArrayList;
import java.util.List;
import exceptions.AirportException;
import exceptions.FlightException;

public class AviationCenter {
    
    //airports and flights
    private final List<Airport> airports;
    private final List<Flight> flights;
    
    public AviationCenter() {
        this.airports = new ArrayList<>();
        this.flights = new ArrayList<>();
    }
    
    
    public void addAirport(Airport a) throws AirportException {
    	//check code and coordinates
        for (Airport airport : airports) {
            if (airport.getCode().equals(a.getCode())) {
                throw new AirportException("Airport with the code " + a.getCode() + " already exists!");
            }
            if (airport.getX() == a.getX() && airport.getY() == a.getY()) {
                throw new AirportException("Airport with the coordinates (" + a.getX() + ", " + a.getY() + ") already exists!");
            }
        }
        airports.add(a);
    }
    
    
    public void addFlight(Flight f) throws FlightException {
    	//check from i to
        boolean startExists = false;
        boolean endExists = false;
        
        for (Airport a : airports) {
            if (a.getCode().equals(f.getFrom().getCode())) {
                startExists = true;
            }
            if (a.getCode().equals(f.getTo().getCode())) {
                endExists = true;
            }
        }
        
        if (!startExists) {
            throw new FlightException("Origin airport " + f.getFrom().getCode() + " doesn't exist!");
        }
        if (!endExists) {
            throw new FlightException("Destination airport " + f.getTo().getCode() + " doesn't exist!");
        }
        
        flights.add(f);
    }
    
    //finder
    public Airport findAirportByCode(String code) {
        for (Airport a : airports) {
            if (a.getCode().equals(code)) {
                return a;
            }
        }
        return null;
    }

    //getters
    public List<Airport> getAirports() {
        return airports;
    }
    public List<Flight> getFlights() {
        return flights;
    }
}