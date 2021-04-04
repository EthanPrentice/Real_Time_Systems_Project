package src.ui;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import src.adt.ElevatorState;
import util.Config;

public class ElevatorPanel extends JPanel {
	
	/** Auto-generated */
	private static final long serialVersionUID = 4130185779601142142L;

	// status buttons (floors)
	JButton[] buttons = new JButton[Config.NUM_FLOORS];
	
	// status labels
	private JLabel stateLabel = new JLabel("Elevator state: ");
	private JLabel statusLabel = new JLabel("Elevators status: OK");
	
	
	public ElevatorPanel(int colWidth) {
		setBounds(100, 100, colWidth, 720);
		setLayout(new MigLayout("", "[513px]", "[33px][32px][33px][33px][32px][33px]" +
				"[33px][32px][33px][33px][32px][33px][33px][33px][32px][33px][33px][32px][33px][33px][32px][33px][33px]"));
		setLayout(new MigLayout("", "[1px]", "[1px]"));
		
		// Add buttons to indicate current elevator floor
		for (int i = 0; i < buttons.length; ++i) {
			int floorNum = Config.NUM_FLOORS - i;

			buttons[i] = new JButton("Floor " + floorNum);
			add(buttons[i], "flowx,cell 0 " + i);
		}
		buttons[Config.NUM_FLOORS - 1].setBackground(Color.GREEN);
		
		// Statuses to panel
        add(stateLabel, "cell 0 13");
        add(statusLabel, "cell 0 15");
	}
	
	public void setState(ElevatorState state) {
		stateLabel.setText("Elevator state: " + state.toString());
	}
	
	public void setStatusMessage(String msg) {
		statusLabel.setText("Elevator status: " + msg);
	}
	
	public JButton[] getButtons() {
		return buttons;
	}
	
}
