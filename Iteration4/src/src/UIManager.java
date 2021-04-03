package src;

import java.awt.*;
import java.util.HashMap;
import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import src.adt.ElevatorStatus;


public class UIManager {

	HashMap<Character, JFrame> elevatorIDMap = new HashMap<>();

	private final int MAX_FLOOR = 25;

	private JFrame frame;
	private JButton[] buttons = new JButton[MAX_FLOOR];

	JLabel requestLabel = new JLabel ("Requested from: " );
	JLabel directionLabel = new JLabel ("Elevator Direction after pick up: " );
	JLabel destinationLabel = new JLabel ("Elevator going to: " );
	JLabel statusLabel = new JLabel ("Elevators current status: OK");

	public UIManager() { 

	}

	public void updateElevatorStatus(char elevatorId, ElevatorStatus status) {

	}

	public void registerElevator(char elevatorId, ElevatorStatus status) {

	}

	private void updateButtonsFloor() {
		int buttonFloor = buttons.length;
		for (int i = 0; i < buttons.length; i++) {
			int floor = buttons.length - i;
			JButton button = buttons[i];
			if(floor /* == TODO*/) {     //current floor
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
         	if(floor /*== TODO*/) {    //currentFooor
         		button.setBackground(Color.YELLOW);
         	}
         }
	}
	
	private void updateButtonFatalError() {
		 for (int i = 0; i < buttons.length; i++) {
         	int floor = buttons.length - i;
         	JButton button = buttons[i];
         	if(floor /*== TODO*/ ) {   //currentFloor
         		button.setBackground(Color.RED);
         		System.out.println("Setting to REd");
         	}
         }
	}


	public void initFrame() {
		frame = new JFrame();
		frame.setBounds(100, 100, 500, 720);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[513px]", "[33px][32px][33px][33px][32px][33px]" +
				"[33px][32px][33px][33px][32px][33px][33px][33px][32px][33px][33px][32px][33px][33px][32px][33px][33px]"));
		frame.getContentPane().setLayout(new MigLayout("", "[1px]", "[1px]"));

		Canvas canvas = new Canvas();
		frame.getContentPane().add(canvas, "cell 0 " + MAX_FLOOR + ",grow");

		for (int i = 0; i < MAX_FLOOR; i++) {
			int floor = MAX_FLOOR - i;
			JLabel floorLabel = new JLabel("Floor " + floor);
			frame.getContentPane().add(floorLabel, "flowx,cell 0 " + i + ",grow");

			buttons[i] = new JButton("Elevator " + "TODO Enter ID");
			frame.getContentPane().add(buttons[i], "cell 0 " + i);
		}


		frame.getContentPane().add(requestLabel, "cell 3 5");
		frame.getContentPane().add(directionLabel, "cell 3 7");
		frame.getContentPane().add(destinationLabel, "cell 3 9");
		frame.getContentPane().add(statusLabel, "cell 3 11");




		buttons[MAX_FLOOR -1].setBackground(Color.GREEN);
	}



}
