package gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
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
import java.awt.ScrollPane;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.Dialog.ModalityType;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import models.Airport;
import models.AviationCenter;
import models.Flight;
import services.FileHandler;
import services.InactivityTimer;

public class Window extends Frame {
	
    private final AviationCenter aCenter = new AviationCenter();
    private final FileHandler fileHandler = new FileHandler(aCenter);
    private java.util.List<Flight> activeFlights = new ArrayList<>();
    
    //timers
    private InactivityTimer iTimer;
    
    //simulation
    private int simMinutes = 0;
    private volatile boolean simRunning = false;
    private Thread simThread = null;
    private Label simTimeLabel;
    private Button btnStart, btnPause, btnReset;
    
    //map
    private MapCanvas mapCanvas;
    
    //tables
    private Panel airportContainerPanel;
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
		setBounds(-10, 90, 950, 550);
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
    	mapCanvas = new MapCanvas(this, aCenter);

    	//configure map
        mapCanvas.setBackground(Color.BLACK);
        mapCanvas.setPreferredSize(new Dimension(180*7, 90*7));
        
    	//create panel
		Panel centerPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
        
		//add map to panel
		centerPanel.add(mapCanvas);
		
		//add panel in center
		add(centerPanel, BorderLayout.CENTER);
	}

	private void setupEastPanel() {
		//create tables
		airportContainerPanel = new Panel(new GridLayout(0, 1)); //as panel not list so we can add check box 
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
		
		
		//font for flight table
		Font tableFont = new Font("Monospaced", Font.PLAIN, 12);
		airportContainerPanel.setFont(tableFont);
		flightList.setFont(tableFont);
		
		//add scroll feature
		ScrollPane airportScrollPane = new ScrollPane();
		airportScrollPane.add(airportContainerPanel);
		
		//header panel
		Panel headerPanel = new Panel(new BorderLayout());
		Label headerLabel = new Label(String.format("   %-10s | %-76s | %-16s | %-6s", "CODE", "NAME", "X", "Y"));
		headerPanel.add(headerLabel, BorderLayout.NORTH);
		headerPanel.add(airportScrollPane, BorderLayout.CENTER);
		
		//create sub-panel for tables
		Panel listsPanel = new Panel(new GridLayout(2, 1));
		listsPanel.add(headerPanel);
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
		Panel southPanel = new Panel(new BorderLayout());
		southPanel.setBackground(Color.LIGHT_GRAY);
		
		//create sub-panel for file input
		Panel fileInput = new Panel(new FlowLayout(FlowLayout.RIGHT));	
		//filename label
		fileInput.add(new Label("Import CSV/JSON File:"));
		fileInput.add(fileField);
		//load button
		Button loadBtn = new Button("Load File");
		fileInput.add(loadBtn);
		
		//create sub-panel for simulation time
		Panel simPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
		simPanel.add(new Label("Time: "));
		//add time label
		simTimeLabel = new Label("00:00");
		Font timeFont = new Font("Arial", Font.BOLD, 15);
		simTimeLabel.setFont(timeFont);
		simPanel.add(simTimeLabel);
		//buttons
		btnStart = new Button("Start");
		btnPause = new Button("Pause");
		btnReset = new Button("Reset");
		//initially can not be paused
		btnPause.setEnabled(false);
		//add buttons
		simPanel.add(btnStart);
		simPanel.add(btnPause);
		simPanel.add(btnReset);
		
		//add sub-panels in panel
		southPanel.add(simPanel);
		southPanel.add(fileInput, BorderLayout.EAST);
		
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
		
		//start button listener
		btnStart.addActionListener(e -> {
		    if (simRunning) return; //already running

		    //pause inactivity timer
		    if (iTimer != null) {
		        iTimer.setPaused(true);
		    }

		    //setting buttons
		    simRunning = true;
		    btnStart.setEnabled(false);	
		    btnPause.setEnabled(true);	
		    btnReset.setEnabled(false); //can not reset while active

		    //thread for simulation
		    simThread = new Thread(() -> {
		        try {
		            while (simRunning) {
		                
		                Thread.sleep(200); 

		                //TIME
		                simMinutes += 2; //200ms -> 2mins

		                //if 24h pass, reset new day
		                if (simMinutes >= 1440) {
		                    simMinutes = 0;
		                }	                

		                //get formatted time
		                String sTime = timeFormat(simMinutes);
		                //update time UI
		                simTimeLabel.setText(sTime);
		                
		                //FLIGHTS TO TAKE OFF
		                for (Flight f : aCenter.getFlights()) {
		                	//get rounded take off time
		                	int virtualTakeOffTime = roundToNextEven(f.getTakeOffTimeInt());
		                	
		                	if (!f.hasTakenOff() && virtualTakeOffTime == simMinutes) {
		                        f.getFrom().getWaitingQueue().add(f); //add in origin airport queue
		                    }
		                }
		                
		                for (Airport a : aCenter.getAirports()) {
		                	if (!a.getWaitingQueue().isEmpty() && simMinutes >= a.getNextAvailableSlot()) {
		                		Flight flyingPlane = a.getWaitingQueue().poll();
		                        //plane took off
		                        flyingPlane.setTookOff(true); 
		                        flyingPlane.setActualTakeOffTime(simMinutes);
		                        
		                        //add to active airplanes
		                        activeFlights.add(flyingPlane);
		                        
		                        //update next 10min slot
		                        a.setNextAvailableSlot(simMinutes + 10);
		                        
		                        //System.out.println("Poletanje! Let sa " + a.getCode() + " je uspešno poleteo u " + simMinutes + " wanted TakeOffTime " + flyingPlane.getTakeOffTime());
		                	}
		                }
		                
		                //update active airplanes
		                for (Flight f : activeFlights) {
		                	int passedTime = simMinutes - f.getActualTakeOffTime();
		                	
		                	//percentage of flight passed
		                	double t = (double) passedTime / f.getFlightDurMin();
		                	if (t > 1.0) t = 1.0;
		                	
		                	//get from and to coordinates
		                	double xOrigin = f.getFrom().getX();
		                	double yOrigin = f.getFrom().getY();
		                	double xDestination = f.getTo().getX();
		                	double yDestination = f.getTo().getY();
		                	
		                	//get current coordinates
		                	double currX = xOrigin + t * (xDestination - xOrigin);
		                	double currY = yOrigin + t * (yDestination - yOrigin);
		                	
		                	//setting coordinates
		                	f.setCurrentX(currX);
		                	f.setCurrentY(currY);
		                	
		                	//System.out.println("Flight " + f.getActualTakeOffTime() + " coordinates: " + f.getCurrentX() + ", " + f.getCurrentY() + " to airport: " + f.getFrom().getName() + " to airport: " + f.getTo().getName());
		                	//System.out.println("From airport: " + f.getFrom().getX() + ", " + f.getFrom().getY() + " to airport: " + f.getTo().getX() + ", " + f.getTo().getY());
		                }
		                
		                //clearing airplanes that landed
		                activeFlights.removeIf(f -> 
		                	(simMinutes - f.getActualTakeOffTime()) >= f.getFlightDurMin()
		                );
		                
		                mapCanvas.repaint();
		            }
		        } catch (InterruptedException ex) {}
		    });
		    //start simulation thread
		    simThread.start();
		});
		
		//pause button listener
		btnPause.addActionListener(e -> {
			simRunning = false;
			
			//stop simThread
			if (simThread != null) {
				simThread.interrupt();
			}
			
			//start inactivity timer
			if (iTimer != null) {
				iTimer.setPaused(false);
			}
			
			//setting buttons
			btnStart.setEnabled(true);
			btnPause.setEnabled(false);
			btnReset.setEnabled(true);
		});
		
		//reset button listener
		btnReset.addActionListener(e -> {
			simRunning = false;
			
			//stop simThread
			if (simThread != null) {
				simThread.interrupt();
			}
			
			//start inactivity timer
			if (iTimer != null) {
				iTimer.setPaused(false);
			}
			
			//no active flights
			activeFlights.clear();
			//reset airport queues
			for (Airport a : aCenter.getAirports()) {
				a.resetQueueState();
			}
			//reset flights
			for (Flight f : aCenter.getFlights()) { 
		        f.setTookOff(false);
		    }
			
			//reset time
			simMinutes = 0;
			simTimeLabel.setText("00:00");
			
			//setting buttons
			btnStart.setEnabled(true);
		    btnPause.setEnabled(false);
		    btnReset.setEnabled(false);
		    
		    //clear map
		    mapCanvas.repaint();
		});
	}
	
	//helper methods
	
	private int roundToNextEven(int minutes) {
	    if (minutes % 2 != 0) {
	        return minutes + 1;
	    }
	    return minutes;
	}
	
	private String timeFormat(int mins) {
		int h = simMinutes / 60;
        int m = simMinutes % 60;
        String formattedTime = String.format("%02d:%02d", h, m);
        return formattedTime;
	}

	private void refreshTables() {
		//reset
		airportContainerPanel.removeAll();
		
	    for (Airport a : aCenter.getAirports()) {
	    	String labelText = a.toString();
	    	//check box
	    	Checkbox cb = new Checkbox(labelText, a.isVisible());
			Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	    	cb.setFont(monoFont);
	    	//check box listener
	    	cb.addItemListener(e -> {
	    		//check selected
	            boolean isChecked = (e.getStateChange() == ItemEvent.SELECTED);
	            a.setVisible(isChecked);
	            mapCanvas.repaint();
	        });
	    	//add to table
	    	airportContainerPanel.add(cb);
	    }
	    airportContainerPanel.revalidate();
	    airportContainerPanel.repaint();
		
		
		//reset
		flightList.removeAll();
		//add header
		flightList.add(String.format("%-6s | %-6s | %-10s | %-10s", "FROM", "TO", "TAKEOFF", "DUR(m)"));
		flightList.add("---------------------------------------");
		for (Flight f : aCenter.getFlights()) {
			//show every flight details
			flightList.add(f.toString());
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
	
	public InactivityTimer getInactivityTimer() {
	    return this.iTimer;
	}

	public java.util.List<Flight> getActiveFlights() {
		return activeFlights;
	}
	
	
}