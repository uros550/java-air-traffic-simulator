package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import exceptions.AirportException;
import exceptions.FileException;
import exceptions.FlightException;
import models.Airport;
import models.Flight;
import models.AviationCenter;

public class FileHandler {

    private final AviationCenter center;

    public FileHandler(AviationCenter center) {
        this.center = center;
    }
    
    //import public method
    public void importFile(String filename) throws IOException, AirportException, FileException, FlightException {
    	//check file
        File file = new File(filename);
        if (!file.exists()) {
            throw new IOException("File " + filename + " doesn't exist!");
        }
        
        //check format
        if (filename.toLowerCase().endsWith(".json")) {
            importJSON(file);
        } 
        else if (filename.toLowerCase().endsWith(".csv")){
            importCSV(file);
        }
        else {
        	throw new FileException("File " + filename + " isn't JSON or CSV format!");
        }
    }

    //CSV format
    private void importCSV(File file) throws IOException, AirportException, FlightException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            //check if header
            boolean isHeader = true;
            boolean isAirports = false;

            while ((line = reader.readLine()) != null) {
            	//remove leading and trailing white spaces
                line = line.trim();
                //skip empty lines
                if (line.isEmpty()) continue;
                //get airports or flights
                if (line.startsWith("#")) {
                	if (line.equals("# AIRPORTS")) {
                		isAirports = true;
                		isHeader = true;
                		continue;
                	}
                	else {
                		isAirports = false;
                		isHeader = true;
                		continue;
                	}
                }
                //skip header
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length != 4) {
                    throw new AirportException("Expected 4 columns!");
                }

                if (isAirports) {
                	//get info
                    String code = parts[0].trim();
                    String name = parts[1].trim();
                    double x, y;
                    //check numbers
                    try {
                        x = Double.parseDouble(parts[2].trim());
                        y = Double.parseDouble(parts[3].trim());
                    } catch (NumberFormatException e) {
                        throw new AirportException("Coordinates must be valid numbers!");
                    }
                    //create airport and add it in aviation center
                    Airport airport = new Airport(name, code, x, y);
                    center.addAirport(airport);
                }
                else {
                	//get info
                    String fromCode = parts[0].trim();
                    String toCode = parts[1].trim();
                    String takeOffTime = parts[2].trim();
                    String durationStr = parts[3].trim();
                    //checking if null is done in Flight constructor 
                    Airport fromAirport = center.findAirportByCode(fromCode);
                    Airport toAirport = center.findAirportByCode(toCode);

                    int takeOffTimeInt = parseTimeToMinutes(takeOffTime);
                    int duration;
                    //check duration
                    try {
                        duration = Integer.parseInt(durationStr);
                    } catch (NumberFormatException e) {
                        throw new FlightException("Flight duration must be an integer!");
                    }
                    //create flight and add it in aviation center
                    Flight flight = new Flight(fromAirport, toAirport, takeOffTime, takeOffTimeInt, duration);
                    center.addFlight(flight);
                }
            }
        }
    }

    
  //JSON format
    private void importJSON(File file) throws IOException, AirportException, FlightException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                //remove leading and trailing white spaces
                line = line.trim();
                //will get correct lines and first line (which will be skipped)
                if (!line.startsWith("{") || line.contains("\"airports\"") || line.contains("\"flights\"")) continue;

                //check if line belongs to airports
                if (line.contains("\"code\"")) {
                    //extract method will return value or null 
                    String code = extractJSONValue(line, "code");
                    String name = extractJSONValue(line, "name");
                    String xStr = extractJSONValue(line, "x");
                    String yStr = extractJSONValue(line, "y");
                    if (code == null || name == null || xStr == null || yStr == null) continue;
                    
                    //check coordinates
                    double x, y;
                    try {
                        x = Double.parseDouble(xStr);
                        y = Double.parseDouble(yStr);
                    } catch (NumberFormatException e) {
                        throw new AirportException("Coordinates must be valid numbers!");
                    }
                    //create airport and add it in aviation center
                    Airport airport = new Airport(name, code, x, y);
                    center.addAirport(airport);
                } 
                //check if line belongs to flights
                else if (line.contains("\"from\"")) {
                    //extract method will return value or null 
                    String fromCode = extractJSONValue(line, "from");
                    String toCode = extractJSONValue(line, "to");
                    String takeOffTime = extractJSONValue(line, "departure");
                    String durationStr = extractJSONValue(line, "duration");
                    if (fromCode == null || toCode == null || takeOffTime == null || durationStr == null) continue;

                    //string to airport
                    Airport fromAirport = center.findAirportByCode(fromCode);
                    Airport toAirport = center.findAirportByCode(toCode);
                    //get take off time integer
                    int takeOffTimeInt = parseTimeToMinutes(takeOffTime);
                    int duration;
                    //check duration
                    try {
                        duration = Integer.parseInt(durationStr);
                    } catch (NumberFormatException e) {
                        throw new FlightException("JSON flight duration must be an integer!");
                    }
                    //create flight and add it in aviation center
                    Flight flight = new Flight(fromAirport, toAirport, takeOffTime, takeOffTimeInt, duration);
                    center.addFlight(flight);
                }
            }
        }
    }
    
    //helper methods

    //takeOffString -> takeOffInt
    private int parseTimeToMinutes(String timeStr) throws FlightException {
        if (!timeStr.contains(":")) {
            throw new FlightException("Missing ':' character!");
        }
        String[] parts = timeStr.split(":");
        //try to parseInt 
        try {
            int hours = Integer.parseInt(parts[0].trim());
            int minutes = Integer.parseInt(parts[1].trim());
            return hours * 60 + minutes;
        } catch (NumberFormatException e) {
            throw new FlightException("Hours and minutes must be valid numbers!");
        }
    }

    //parse JSON files
    private String extractJSONValue(String line, String key) {
    	//find key start
        String searchKey = "\"" + key + "\": ";
        int keyIndex = line.indexOf(searchKey);
        if (keyIndex == -1) return null;
        
        //find value start
        int valueStart = keyIndex + searchKey.length();
        int valueEnd;

        //if string
        if (line.charAt(valueStart) == '"') {
            valueStart++; //skip opening quote
            valueEnd = line.indexOf('"', valueStart); //end index on next quotation mark
        } 
        //if integer
        else {
            //next comma and bracket
            int commaIndex = line.indexOf(',', valueStart);
            int bracketIndex = line.indexOf('}', valueStart);
            //end index on next comma or next bracket depending on what is first
            if (commaIndex != -1 && bracketIndex != -1) {
                valueEnd = (commaIndex < bracketIndex) ? commaIndex : bracketIndex;
            } else {
                valueEnd = (commaIndex != -1) ? commaIndex : bracketIndex;
            }
        }

        if (valueEnd == -1) return null;
        return line.substring(valueStart, valueEnd).trim(); //it does not include what is on end index
    }
}