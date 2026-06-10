package test;

import models.Airport;
import models.AviationCenter;
import models.Flight;
import services.FileHandler;

public class Main {

	public static void main(String[] args) {
		System.out.println("=== STARTING AIR TRAFFIC SIMULATOR ENGINE EMULATOR ===");
		
		AviationCenter center = new AviationCenter();
        FileHandler fileHandler = new FileHandler(center);
        
        try {
        	System.out.println("\n Importing airports and flights...");
        	fileHandler.importFile("testFile.json");
        	//airports
        	System.out.println("Successfully imported " + center.getAirports().size() + " airports.");
        	for (Airport a : center.getAirports()) {
                System.out.println(" -> " + a);
            }
        	//flights
        	System.out.println("Successfully imported " + center.getFlights().size() + " flights.");
        	for (Flight f : center.getFlights()) {
                System.out.println(" -> " + f);
            }
        	
        	System.out.println("\n Importing JSON finished...");
			
		} catch (Exception e) {
			System.err.println(e);
		}
        
//        try {
//        	System.out.println("\n Importing airports and flights...");
//        	fileHandler.importFile("testFile.csv");
//        	//airports
//        	System.out.println("Successfully imported " + center.getAirports().size() + " airports.");
//        	for (Airport a : center.getAirports()) {
//                System.out.println(" -> " + a);
//            }
//        	//flights
//        	System.out.println("Successfully imported " + center.getFlights().size() + " flights.");
//        	for (Flight f : center.getFlights()) {
//                System.out.println(" -> " + f);
//            }
//        	
//        	System.out.println("\n Importing CSV finished...");
//			
//		} catch (Exception e) {
//			System.err.println(e);
//		}
	}

}
