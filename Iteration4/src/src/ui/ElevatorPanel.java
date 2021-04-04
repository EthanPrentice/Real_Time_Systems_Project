package src.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import src.adt.ElevatorState;
import src.adt.message.FloorRequest;
import util.Config;

public class ElevatorPanel extends JPanel {
	
	/** Auto-generated */
	private static final long serialVersionUID = 4130185779601142142L;
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	private static final int FLOORS_PER_COLUMN = 11;
	

	// status buttons (floors)
	JButton[] buttons = new JButton[Config.NUM_FLOORS];
	
	// status labels
	private JLabel stateLabel = new JLabel("State: ");
	private JLabel statusLabel = new JLabel("Status: OK");
	
	// scroll views
	private HashMap<FloorRequest, JLabel> currReqLabels = new HashMap<>();
	private HashMap<FloorRequest, JLabel> completedReqLabels = new HashMap<>();
	private JPanel currReqsPane;
	private JPanel completedReqsPane;
	
	
	public ElevatorPanel(char elevatorId, int colWidth) {
		setPreferredSize(new Dimension(colWidth, 720));
		setLayout(new MigLayout());
		
		stateLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
		
		JLabel elevatorName = new JLabel("Elevator " + (int) elevatorId);
		elevatorName.setFont(elevatorName.getFont().deriveFont(Font.BOLD + Font.ITALIC));
		add(elevatorName, "al center, cell 0 0");
		
		// Add buttons to indicate current elevator floor
		for (int i = 0; i < buttons.length; ++i) {
			int yPos = (Config.NUM_FLOORS - 1 - i) % FLOORS_PER_COLUMN;

			buttons[i] = new JButton("Floor " + (i + 1));
			buttons[i].setFocusable(false);
			
			add(buttons[i], "al center,cell 0 " + (yPos + 1));
		}
		buttons[Config.NUM_FLOORS - 1].setBackground(Color.GREEN);
		
		// Statuses to panel
        add(stateLabel, "al center,cell 0 " + Config.NUM_FLOORS + 1);
        add(statusLabel, "al center,cell 0 " + Config.NUM_FLOORS + 2);
        
        JLabel subHeaderLabel = new JLabel("Queued & in-progress requests");
        subHeaderLabel.setFont(subHeaderLabel.getFont().deriveFont(Font.BOLD + Font.ITALIC));
        subHeaderLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
        add(subHeaderLabel, "al center,cell 0 " + Config.NUM_FLOORS + 3);
        
        subHeaderLabel = new JLabel("Completed requests");
        subHeaderLabel.setFont(subHeaderLabel.getFont().deriveFont(Font.BOLD + Font.ITALIC));
        subHeaderLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
        add(subHeaderLabel, "al center,cell 0 " + Config.NUM_FLOORS + 5);
        
        
        // Scroll panes for requests
        currReqsPane = new JPanel();
        completedReqsPane = new JPanel();
        
		JScrollPane scrollPane = createScrollPane(currReqsPane, colWidth, 300);
		add(scrollPane, "grow,cell 0 " + Config.NUM_FLOORS + 4);
		
		scrollPane = createScrollPane(completedReqsPane, colWidth, 300);
		add(scrollPane, "grow,cell 0 " + Config.NUM_FLOORS + 6);
		
		updateUI();
	}
	
	
	public void addCurrentRequest(FloorRequest req) {
		JLabel label = new JLabel(reqToString(req));		
		currReqLabels.put(req, label);
		
		currReqsPane.add(label);
		currReqsPane.repaint();
	}
	
	
	public void addCompletedRequest(FloorRequest req) {
		JLabel label = currReqLabels.get(req);		
		currReqLabels.remove(req);
		
		label.setText(reqToString(req));
		
		currReqsPane.remove(label);
		currReqsPane.repaint();
		
		completedReqLabels.put(req, label);
		completedReqsPane.add(label);
		completedReqsPane.repaint();
	}
	
	
	private JScrollPane createScrollPane(JPanel panel, int width, int height) {
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JScrollPane pane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pane.setPreferredSize(new Dimension(width, height));
		return pane;
	}
	
	public void removeFromQueued(ArrayList<FloorRequest> reqs) {
		JLabel label;
		for (FloorRequest req : reqs) {
			label = currReqLabels.get(req);
			currReqLabels.remove(req);
			currReqsPane.remove(label);
		}
		currReqsPane.invalidate();
		currReqsPane.validate();
		currReqsPane.repaint();
	}
	
	public void setState(ElevatorState state) {
		stateLabel.setText("State: " + state.toString());
	}
	
	public void setStatusMessage(String msg) {
		statusLabel.setText("Status: " + msg);
	}
	
	public JButton[] getButtons() {
		return buttons;
	}
	
	
	private static String reqToString(FloorRequest req) {
		String format = "[%s] %s";
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return String.format(format, sdf.format(timestamp), req.getPrettyString());
	}
	
}
