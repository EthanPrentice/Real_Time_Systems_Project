package src.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;

import src.adt.ElevatorState;
import src.adt.ElevatorStatus;
import src.adt.message.FloorRequest;
import util.Config;

public class UIManager {
	
	private final static int COLUMN_WIDTH = 300;
	

	private HashMap<Character, ElevatorPanel> elevatorIdMap = new HashMap<>();
	private HashMap<Character, ElevatorStatus> elevatorStatuses = new HashMap<>();

	private JFrame frame;
	private JPanel emptyPanel;

	public UIManager() {
		frame = getRootFrame();	
		frame.setVisible(Config.SHOW_UI);
		
		// Add message until elevators are added
		JLabel emptyLabel = new JLabel("No Elevators Registered", JLabel.CENTER);		
		emptyPanel = new JPanel(new BorderLayout());
		emptyPanel.setBounds(frame.getBounds());
		emptyPanel.add(emptyLabel, BorderLayout.CENTER);
		frame.add(emptyPanel);
	}

	
	public void registerElevator(char elevatorId, ElevatorStatus status) {		
		ElevatorPanel newPanel = new ElevatorPanel(elevatorId, COLUMN_WIDTH);
		elevatorIdMap.put(elevatorId, newPanel);
		updateElevatorStatus(elevatorId, status);
		
		
		
		if (elevatorIdMap.size() != 1) {
			Rectangle currBounds = frame.getBounds();
			currBounds.width += 500; // add width of new column
			currBounds.height = Math.max(currBounds.height, newPanel.getHeight());
			frame.setPreferredSize(new Dimension(currBounds.width, currBounds.height));
		}
		else {
			frame.remove(emptyPanel);
		}
		
		frame.add(newPanel);
		
		frame.pack();
		
		frame.invalidate();
		frame.validate();
		frame.repaint();
	}
	
	
	public void updateElevatorStatus(char elevatorId, ElevatorStatus status) {
		elevatorStatuses.put(elevatorId, status);
		
		ElevatorPanel panel = elevatorIdMap.get(elevatorId);
		
		JButton[] buttons = panel.getButtons();
		
		Color floorColor = Color.GREEN;
		if (status.getState() == ElevatorState.DOORS_JAMMED) {
			floorColor = Color.YELLOW;
			panel.setStatusMessage("Recovering...");
		}
		else {
			panel.setStatusMessage("OK");
		}
		
		for (int i = 0; i < buttons.length; i++) {
			JButton button = buttons[i];
			
			if (status.getFloor() - 1 == i) {     //current floor
				button.setBackground(floorColor);
			}
			else {
				button.setBackground(null);
			}			
		}
		
		panel.setState(status.getState());
	}
	
	
	public void addFloorRequest(char elevatorId, FloorRequest req) {
		ElevatorPanel panel = elevatorIdMap.get(elevatorId);
		panel.addCurrentRequest(req);
	}
	
	
	public void completeFloorRequest(char elevatorId, FloorRequest req) {
		ElevatorPanel panel = elevatorIdMap.get(elevatorId);
		panel.addCompletedRequest(req);
	}
	
	
	public void unregisterElevator(char elevatorId, ArrayList<FloorRequest> recoveredReqs) {
		ElevatorPanel panel = elevatorIdMap.get(elevatorId);
		ElevatorStatus status = elevatorStatuses.get(elevatorId);
		
		panel.getButtons()[status.getFloor() - 1].setBackground(Color.RED);
		panel.setState(ElevatorState.STOPPED);
		panel.setStatusMessage("Fatal Error");
		
		panel.removeFromQueued(recoveredReqs);
	}

	
	private JFrame getRootFrame() {
		JFrame frame = new JFrame();
		frame.setBounds(0, 0, COLUMN_WIDTH, 720);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setTitle("Elevator System Display");
		
		return frame;
	}

}
