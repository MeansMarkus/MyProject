import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.openstreetmap.gui.jmapviewer.tilesources.*;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;

public class Driver implements ActionListener{
	
	//class data
    JFrame frame;
    
    JPanel panel;
    
    JButton button;
    
    JComboBox comboBox;
    
    JCheckBox checkBox;
    
    Image raccoon;
    
    JMapViewer mapViewer;
    
    ArrayList<TripPoint> trip;
    
    IconMarker iconMarker;
    
    Timer timer;
    
    boolean timerStart = false;
    
    MapPolygonImpl path;
    
    int interval;

    Driver() throws FileNotFoundException, IOException{
        // Read file and call stop detection
        TripPoint.readFile("triplog.csv");
        trip = TripPoint.getTrip();
        TripPoint.h1StopDetection();

        // Set up frame
    	frame = new JFrame("Trip Analyzer by Markus Means");
        frame.setBounds(0, 0, 1000, 500);
        frame.setLayout(new BorderLayout());

        // Set up Panel for input selections
        panel = new JPanel();
        
        panel.setBackground(Color.red);
        
        panel.setBounds(0, 0, 1000, 100);
            	
        // Play Button
        button = new JButton("Play");
        button.setFocusable(false);
    	
        // CheckBox to enable/disable stops
        checkBox = new JCheckBox("Include Stops");
        checkBox.setFocusable(false);
            	
        // ComboBox to pick animation time
        String[] timeChoice = {"15", "30", "60", "90"};
        comboBox = new JComboBox<>(timeChoice);
        comboBox.setName("Select Time");
        comboBox.setEditable(false);
        comboBox.setFocusable(false);
         	
        // Add all to top panel
        panel.add(button);
        panel.add(checkBox);
        panel.add(comboBox);
        
        // Set up mapViewer
        mapViewer = new JMapViewer();
        Coordinate mapCenter = new Coordinate(33.083, -106.611);
        mapViewer.setTileSource(new OsmTileSource.TransportMap());
        mapViewer.setFocusable(false);
        
        // Add listeners for GUI components
        button.addActionListener(e -> animateTrip());

        // Set the map center and zoom level
        mapViewer.setDisplayPosition(mapCenter, 6);
        
        mapViewer.setZoomControlsVisible(false);
        
        mapViewer.setAlignmentX((float) mapCenter.getLat());
        
        mapViewer.setAlignmentY((float) mapCenter.getLon());
        
        mapViewer.setZoom(6);
        
        frame.add(mapViewer);
        
        // Set up final frame necessities
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel, BorderLayout.NORTH);
        frame.setVisible(true);
       
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        Driver project5 = new Driver();

    }
    
    // Animate the trip based on selections from the GUI components
    public void animateTrip(){
        // Removes markers and path (acts like a play/reset for the animation)
        mapViewer.removeAllMapMarkers();
        
        mapViewer.removeAllMapPolygons();
        
        // Initailize raccoon image using ImageIcon and calling getImage method to return an image
        raccoon = new ImageIcon("raccoon.png").getImage();
        
        boolean stopsIn = checkBox.isSelected();  // Check if stops are gonna be in animation
        int selectTime = Integer.parseInt((String) comboBox.getSelectedItem());
        int timeInterval = 0;

        if (stopsIn){
            trip = TripPoint.getTrip();  //Trip with stops
            timeInterval = selectTime * 775 / trip.size();
        }
        else if (!stopsIn){
            trip = TripPoint.getMovingTrip();  // Trip without stops
            if(selectTime >= 60){
                timeInterval = selectTime * 1000 / trip.size();
            }
            else{
                timeInterval = selectTime * 625 / trip.size();
            }
            
        }

        //Does Timer for animation
        interval = 1;

        timer = new Timer(timeInterval, e -> {   // Timer method. There are other ways to do this

            // Gets current and previous point of trip 
            TripPoint currPoint = trip.get(interval);
            TripPoint prevPoint = trip.get(interval - 1);

            // Creates coordinates 
            Coordinate currCoordinate = new Coordinate(currPoint.getLat(), currPoint.getLon());
            Coordinate prevCoordinate = new Coordinate(prevPoint.getLat(), prevPoint.getLon());

            mapViewer.removeMapMarker(iconMarker);                  // Tells timer to remove previously made iconMarker
            iconMarker = new IconMarker(prevCoordinate, raccoon);   // Create iconMarker with previous coordinate and raccoon image
            mapViewer.addMapMarker(iconMarker);                     // Adds marker to map
            
            // Create MapPolygonImpl path using iconMarker and both coordinates
            path = new MapPolygonImpl(iconMarker, prevCoordinate, currCoordinate);
            path.setColor(new Color(0, 0, 255));     // Set path color to pink
            mapViewer.addMapPolygon(path);                          // Add path to mapViewer
            
            if (interval < trip.size() - 1){   // Checks if interval is less than trip.size(). Meaning there is still more to the trip
                interval++;
            }
            else{    // Checks if the trip is finished and stops the timer and sets timerStart to false
                timer.stop();
                timerStart = false;
            }
        });

        if (!timerStart){  // Checks if timerStart is false and starts the timer
            timerStart = true;
            timer.start();
        }
        
    }

    // Implement actionPerformed method when button is pressed
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button){
            timerStart = false;     // Set timerStart to false to indicate to the timer to run the animation
            animateTrip();          // Calls animateTrip method
        }
    }
}