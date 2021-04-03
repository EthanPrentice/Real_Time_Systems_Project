package src;

import java.awt.*;
import java.util.HashMap;
import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import src.adt.ElevatorStatus;
import util.Config;


public class UIManager {
	
	private final static int COLUMN_WIDTH = 300;
	

	private HashMap<Character, JPanel> elevatorIdMap = new HashMap<>();
	private HashMap<Character, ElevatorStatus> elevatorStatuses = new HashMap<>();

	private JFrame frame;
	private JButton[] buttons = new JButton[Config.NUM_FLOORS];



	
	public UIManager() {
		frame = getRootFrame();		
		frame.setVisible(true);
	}

	
	public void updateElevatorStatus(char elevatorId, ElevatorStatus status) {
		elevatorStatuses.put(elevatorId, status);
	}

	
	public void registerElevator(char elevatorId, ElevatorStatus status) {
		JPanel newPanel = getElevatorPanel();
		elevatorIdMap.put(elevatorId, newPanel);
		updateElevatorStatus(elevatorId, status);
		
		frame.getContentPane().add(newPanel);
		
		if (elevatorIdMap.size() != 1) {
			Rectangle currBounds = frame.getBounds();
			currBounds.width += 500; // add width of new column
			frame.setBounds(currBounds);
		}
	}

	
	private void updateButtonsFloor() {
		int buttonFloor = buttons.length;
		for (int i = 0; i < buttons.length; i++) {
			int floor = buttons.length - i;
			JButton button = buttons[i];
			if (floor /* == TODO*/) {     //current floor
				button.setBackground(Color.GREEN);
			}
			else {
				button.setBackground(null);
			}
			
			requestLabel.setText("Requested from: " );
			
			String directionl;    //TODO, read direction
	        directionLabel.setText("Elevator's direction: "+ directionl);
	        
	        destinationLabel.setText("Elevator going to " + end);  
			
		}
	}
	
	private void updateButtonDoorStuckError() {
		 for (int i = 0; i < buttons.length; i++) {
         	int floor = buttons.length - i;
         	
         	JButton button = buttons[i];
         	if (floor /*== TODO*/) {    //currentFooor
         		button.setBackground(Color.YELLOW);
         	}
         }
	}
	
	private void updateButtonFatalError() {
		 for (int i = 0; i < buttons.length; i++) {
         	int floor = buttons.length - i;
         	JButton button = buttons[i];
         	if (floor /*== TODO*/ ) {   //currentFloor
         		button.setBackground(Color.RED);
         		System.out.println("Setting to REd");
         	}
         }
	}

	
	public JFrame getRootFrame() {
		JFrame frame = new JFrame();
		frame.setBounds(100, 100, COLUMN_WIDTH, 720);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		return frame;
	}
	

	public JPanel getElevatorPanel() {
		JPanel panel = new JPanel();
		panel.setBounds(100, 100, COLUMN_WIDTH, 720);
		panel.setLayout(new MigLayout("", "[513px]", "[33px][32px][33px][33px][32px][33px]" +
				"[33px][32px][33px][33px][32px][33px][33px][33px][32px][33px][33px][32px][33px][33px][32px][33px][33px]"));
		panel.setLayout(new MigLayout("", "[1px]", "[1px]"));

		Canvas canvas = new Canvas();
		panel.add(canvas, "cell 0 " + Config.NUM_FLOORS + ",grow");

		for (int i = 0; i < buttons.length; i++) {
			int floor = Config.NUM_FLOORS - i;
			JLabel floorLabel = new JLabel("Floor " + floor);
			panel.add(floorLabel, "flowx,cell 0 " + i + ",grow");

			buttons[i] = new JButton("Elevator " + "TODO Enter ID");
			panel.add(buttons[i], "cell 0 " + i);
		}
		
		JLabel requestLabel = new JLabel("Requested from: ");
		JLabel directionLabel = new JLabel("Elevator Direction after pick up: ");
		JLabel destinationLabel = new JLabel("Elevator going to: ");
		JLabel statusLabel = new JLabel("Elevators current status: OK");

		panel.add(requestLabel, "cell 3 5");
		panel.add(directionLabel, "cell 3 7");
		panel.add(destinationLabel, "cell 3 9");
		panel.add(statusLabel, "cell 3 11");

		buttons[Config.NUM_FLOORS - 1].setBackground(Color.GREEN);
		
		return panel;
	}



}
