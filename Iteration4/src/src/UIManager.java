package src;

import java.awt.*;
import java.util.HashMap;
import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import src.adt.ElevatorState;
import src.adt.ElevatorStatus;
import src.ui.ElevatorPanel;
import util.Config;


public class UIManager {
	
	private final static int COLUMN_WIDTH = 200;
	

	private HashMap<Character, ElevatorPanel> elevatorIdMap = new HashMap<>();
	private HashMap<Character, ElevatorStatus> elevatorStatuses = new HashMap<>();

	private JFrame frame;

	public UIManager() {
		frame = getRootFrame();		
		frame.setVisible(true);
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
			int floor = buttons.length - i;
			JButton button = buttons[i];
			
			if (status.getFloor() + 1 == floor) {     //current floor
				button.setBackground(floorColor);
			}
			else {
				button.setBackground(null);
			}			
		}
		
		panel.setState(status.getState());
	}

	
	public void registerElevator(char elevatorId, ElevatorStatus status) {		
		ElevatorPanel newPanel = new ElevatorPanel(COLUMN_WIDTH);
		elevatorIdMap.put(elevatorId, newPanel);
		updateElevatorStatus(elevatorId, status);
		
		frame.getContentPane().add(newPanel);
		
		if (elevatorIdMap.size() != 1) {
			Rectangle currBounds = frame.getBounds();
			currBounds.width += 500; // add width of new column
			frame.setBounds(currBounds);
		}
		
		frame.invalidate();
		frame.validate();
		frame.repaint();
	}
	
	
	public void unregisterElevator(char elevatorId) {
		ElevatorPanel panel = elevatorIdMap.get(elevatorId);
		ElevatorStatus status = elevatorStatuses.get(elevatorId);
		
		int buttonIndex = (Config.NUM_FLOORS - 1) - status.getFloor();
		panel.getButtons()[buttonIndex].setBackground(Color.RED);
		panel.setState(ElevatorState.STOPPED);
		panel.setStatusMessage("Fatal Error");
	}

	
//	private void updateButtonDoorStuckError() {
//		 for (int i = 0; i < buttons.length; i++) {
//         	int floor = buttons.length - i;
//         	
//         	JButton button = buttons[i];
//         	if (floor /*== TODO*/) {    //currentFooor
//         		button.setBackground(Color.YELLOW);
//         	}
//         }
//	}
//	
//	private void updateButtonFatalError() {
//		 for (int i = 0; i < buttons.length; i++) {
//         	int floor = buttons.length - i;
//         	JButton button = buttons[i];
//         	if (floor /*== TODO*/ ) {   //currentFloor
//         		button.setBackground(Color.RED);
//         		System.out.println("Setting to REd");
//         	}
//         }
//	}

	
	public JFrame getRootFrame() {
		JFrame frame = new JFrame();
		frame.setBounds(100, 100, COLUMN_WIDTH, 720);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		return frame;
	}

}
