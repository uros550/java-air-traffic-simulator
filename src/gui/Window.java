package gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.Dialog.ModalityType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import models.Airport;
import models.AviationCenter;
import models.Flight;
import services.FileHandler;
import services.InactivityTimer;

public class Window extends Frame {
	
    private final AviationCenter aCenter = new AviationCenter();
    private final FileHandler fileHandler = new FileHandler(aCenter);
    
    private InactivityTimer iTimer;
    
    //map
    private MapCanvas mapCanvas;
    //tables
    private List airportList;
    private List flightList;
    
    //airport fields
    private final TextField airportName = new TextField(12);
    private final TextField airportCode = new TextField(4);
    private final TextField airportX = new TextField(4);
    private final TextField airportY = new TextField(4);
    //flight fields
    private final TextField flightStart = new TextField(4);
    private final TextField flightEnd = new TextField(4);
    private final TextField flightStartTime = new TextField(6);
    private final TextField flightDuration = new TextField(4);
    //file field
    private final TextField fileField = new TextField(20);
    
    
    
    public Window() {
    	//start
		setupWindow();
		populateWindow();
		setupInactivityTimer();
		pack();
        
        setVisible(true);
    }

	private void setupWindow() {
		//classic setters
        setTitle("Airspace");
		setBounds(500, 200, 950, 550);
        setResizable(false);
        
        //x -> closes the program
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });
	}
	
	public void populateWindow() {
		setupCenterPanel();
		setupEastPanel();
		setupSouthPanel();
		refreshTables(); //initially populate with headers
	}

	private void setupInactivityTimer() {
		//starting timer
		iTimer = new InactivityTimer(this);
		iTimer.start();
		
		//connecting to global listener
		Toolkit.getDefaultToolkit().addAWTEventListener(
		        e -> iTimer.resetTimer(),
		        AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK
		    );
	}

	private void setupCenterPanel() {
		//create map
    	mapCanvas = new MapCanvas(aCenter);

    	//configure map
        mapCanvas.setBackground(Color.BLACK);
		int dimensions = Math.min(getWidth(), getHeight()) * 5 / 6;
        mapCanvas.setPreferredSize(new Dimension(dimensions, dimensions));
        
    	//create panel
		Panel centerPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
        
		//add map to panel
		centerPanel.add(mapCanvas);
		
		//add panel in center
		add(centerPanel, BorderLayout.CENTER);
	}

	private void setupEastPanel() {
		//create tables
		airportList = new List();
	    flightList = new List();
	    
	    //create panel
		Panel eastPanel = new Panel(new BorderLayout());
		
		//create sub-panel for user input
		Panel inputPanel = new Panel(new GridLayout(2, 1));
		
		//create sub-sub-panel for airport input
		Panel airportInput = new Panel(new FlowLayout(FlowLayout.LEFT));
		//name label
		airportInput.add(new Label("Name:")); 
		airportInput.add(airportName);
		//code label
		airportInput.add(new Label("Code:")); 
		airportInput.add(airportCode);
		//coordinates labels
		airportInput.add(new Label("X:")); 
		airportInput.add(airportX);
		airportInput.add(new Label("Y:")); 
		airportInput.add(airportY);
		//add button
		Button addAirportBtn = new Button("Add");
		airportInput.add(addAirportBtn);

		//create sub-sub-panel for flight input
		Panel flightInput = new Panel(new FlowLayout(FlowLayout.LEFT));
		//from label
		flightInput.add(new Label("From:")); 
		flightInput.add(flightStart);
		//to label
		flightInput.add(new Label("To:")); 
		flightInput.add(flightEnd);
		//take off time label
		flightInput.add(new Label("Time:")); 
		flightInput.add(flightStartTime);
		//duration label
		flightInput.add(new Label("Dur(m):")); 
		flightInput.add(flightDuration);
		//add button
		Button addFlightBtn = new Button("Add");
		flightInput.add(addFlightBtn);
		
		//add both into sub-panel
		inputPanel.add(airportInput);
		inputPanel.add(flightInput);
		
		
		//font for tables
		Font tableFont = new Font("Monospaced", Font.PLAIN, 12);
		airportList.setFont(tableFont);
		flightList.setFont(tableFont);
		
		//create sub-panel for tables
		Panel listsPanel = new Panel(new GridLayout(2, 1));
		listsPanel.add(airportList);
		listsPanel.add(flightList);
		
		//add both in panel
		eastPanel.add(inputPanel, BorderLayout.NORTH);
		eastPanel.add(listsPanel, BorderLayout.CENTER);
		
		//add panel in east part of window
		add(eastPanel, BorderLayout.EAST);
		
		//airport button listener
		addAirportBtn.addActionListener(e -> {
			try {
				//get info
				String name = airportName.getText().trim();
				String code = airportCode.getText().trim().toUpperCase();
				double x;
				double y;
				try {
					x = Double.parseDouble(airportX.getText().trim());
					y = Double.parseDouble(airportY.getText().trim());
				} catch (Exception ex) {
					throw new Exception("Coordinates must be valid numbers!");
				}
				
				//create airport and add in aviation center
				aCenter.addAirport(new Airport(name, code, x, y));
				refreshTables();
				mapCanvas.repaint();
				
				//clear text fields
				airportName.setText(""); 
				airportCode.setText("");
				airportX.setText(""); 
				airportY.setText("");
				
			} catch (Exception ex) {
				errorDialog(ex.getMessage());
			}
		});
		
		//flight button listener
		addFlightBtn.addActionListener(e -> {
			try {
				//get info
				String from = flightStart.getText().trim().toUpperCase();
				String to = flightEnd.getText().trim().toUpperCase();
				String time = flightStartTime.getText().trim();
				int duration;
				try {
					duration = Integer.parseInt(flightDuration.getText().trim());
				} catch (Exception ex) {
					throw new Exception("Duration must be a valid number!");
				}
				
				//get origin and destination airports
				Airport fromA = aCenter.findAirportByCode(from);
				Airport toA = aCenter.findAirportByCode(to);
				
				//get take off time integer
				if (!time.contains(":") || time.length() != 5) throw new Exception("Wrong take off time format!");
				String[] parts = time.split(":");
				int takeOffMinutes = Integer.parseInt(parts[0].trim()) * 60 + Integer.parseInt(parts[1].trim());
				
				//create flight and add in aviation center
				aCenter.addFlight(new Flight(fromA, toA, time, takeOffMinutes, duration));
				refreshTables();
				mapCanvas.repaint();
				
				//clear text fields
				flightStart.setText(""); 
				flightEnd.setText("");
				flightStartTime.setText(""); 
				flightDuration.setText("");
				
			} catch (Exception ex) {
				errorDialog(ex.getMessage());
			}
		});
	}

	private void setupSouthPanel() {
		//create panel
		Panel southPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
		southPanel.setBackground(Color.LIGHT_GRAY);
		
		//filename label
		southPanel.add(new Label("Import CSV/JSON File:"));
		southPanel.add(fileField);
		//load button
		Button loadBtn = new Button("Load File");
		southPanel.add(loadBtn);
		
		//add panel in south part of window
		add(southPanel, BorderLayout.SOUTH);
		
		//import file listener
		loadBtn.addActionListener(e -> {
			String filename = fileField.getText().trim();
			if (filename.isEmpty()) {
				errorDialog("Please enter a filename.");
				return;
			}
			try {
				//try import
				fileHandler.importFile(filename);
				refreshTables();
				mapCanvas.repaint();
				
				//clear text field
				fileField.setText("");
				
			} catch (Exception ex) {
				errorDialog(ex.getMessage());
			}
		});
	}
	
	//helper methods
	
	private void refreshTables() {
		//reset
		airportList.removeAll();
		//add header
		airportList.add(String.format("%-6s | %-35s | %-6s | %-6s", "CODE", "NAME", "X", "Y"));
		airportList.add("-----------------------------------------------------------");
		for (Airport a : aCenter.getAirports()) {
			//show every airport details
			airportList.add(String.format("%-6s | %-35s | %-6.1f | %-6.1f", a.getCode(), a.getName(), a.getX(), a.getY()));
		}
		
		//reset
		flightList.removeAll();
		//add header
		flightList.add(String.format("%-6s | %-6s | %-10s | %-10s", "FROM", "TO", "TAKEOFF", "DUR(m)"));
		flightList.add("------------------------------------------");
		for (Flight f : aCenter.getFlights()) {
			//show every flight details
			flightList.add(String.format("%-6s | %-6s | %-10s | %-10d", f.getFrom().getCode(), f.getTo().getCode(), f.getTakeOffTime(), f.getFlightDurMin()));
		}
	}

	private void errorDialog(String msg) {
		Dialog dialog = new Dialog(this, ModalityType.APPLICATION_MODAL);
		dialog.setTitle("Error");
	    dialog.setLayout(new BorderLayout());

	    //text message
	    Label textLabel = new Label(msg);
	    //button to dispose
	    Button ok = new Button("OK");
	    ok.addActionListener(e -> dialog.dispose());
	    Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
	    buttonPanel.add(ok);
	    
	    //add to dialog
	    dialog.add(textLabel, BorderLayout.CENTER);
	    dialog.add(buttonPanel, BorderLayout.SOUTH);

	    dialog.pack(); 
	    dialog.setLocationRelativeTo(this);
	    dialog.setVisible(true);
	}
}