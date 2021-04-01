package src;

import src.adt.*;
import src.adt.message.ElevStatusNotify;
import src.adt.message.FloorRequest;
import util.Config;
import util.Log;

import javax.swing.*;
import java.awt.*;
import net.miginfocom.swing.MigLayout;
import java.io.IOException;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Written for SYSC3303 - Group 6 - Iteration 1 @ Carleton University
 * Creates and runs the elevator sub-system as a thread. Here, the elevator thread gets and displays an event from scheduler, then sends the event back to scheduler
 * @author Nikhil Kharbanda 101012041
 * @edited Ethan Prentice (101070194)
 */
public class Elevator implements Runnable {
	
	// Handles the sending and receiving of Messages
	// Is run in a separate thread, started in constructor
	Thread msgHandlerThread;
	ElevatorMessageHandler msgHandler;
	
<<<<<<< Updated upstream
	Thread errHandlerThread;
	ElevatorErrorHandler errHandler;
	
	// Unique identifier for this instance of the Elevator
	private char elevatorId;
	
	// Elevator name to differentiate elevators in the logs
	private String name;

	// default elevator state is stopped when the elevator is initialized
	private ElevatorState currState = ElevatorState.STOPPED;

	// Store the floors to go to in a priority queue
	// Depending on elevator direction these will be ordered least->greatest or the opposite
	private Object floorQueueLock = new Object();
	private PriorityQueue<Integer> floorQueue = new PriorityQueue<Integer>();
	private Comparator<Integer> downComparator = new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
        }
    };
    
    // Manages the errors that will be injected into the system on each floor to simulate hardware faults
    private ArrayList<ArrayList<ErrorType>> errors = new ArrayList<ArrayList<ErrorType>>(Config.NUM_FLOORS);
    
    
    // holds how many people increase in elevator at a given floor
    private int[] floorOccupancy = new int[Config.NUM_FLOORS];

	private boolean stopRequested = false;

	// keep track of the floor the elevator is going to, and the floor it is currently on
	// 0 is the ground floor and all elevators are initialized to this floor
	private int targetFloor = 0;
	private int currFloor = 0;
	

	// testing purposes
	private FloorRequest lastEvent;
	
	// floor to array of FloorRequests with key as their source floor
	private HashMap<Integer, ArrayList<FloorRequest>> recoverableRequests = new HashMap<>();
	
	
	/*
	 * Initializes variables and runs the MessageHandler in a separate thread
	 */
	public void init() {
		for (int i = 0; i < Config.NUM_FLOORS; ++i) {
			errors.add(new ArrayList<ErrorType>());
		}
		
		for (int i = 0; i < Config.NUM_FLOORS; ++i) {
			recoverableRequests.put(i + 1, new ArrayList<FloorRequest>());
		}
		
		msgHandler = new ElevatorMessageHandler(this);
		elevatorId = (char) msgHandler.getPort();
		name = "Elevator " + (int) elevatorId;
		
		Thread.currentThread().setName(name);
		
		msgHandlerThread = new Thread(msgHandler, name + " MsgHandler");
		msgHandlerThread.start();
		
		errHandler = new ElevatorErrorHandler(this);
		errHandlerThread = new Thread(errHandler, name + " ErrHandler");
		errHandlerThread.start();
	}
	

	/**
	 * Runs the thread. Gets event from the Scheduler and then sends event back to the Scheduler
	 */
	@Override
	public void run() {
		init();
		
		while(!stopRequested || !floorQueue.isEmpty()) {         // while elevator is running, or has more events in it's queue

			while (floorQueue.isEmpty()) {
				try {
					synchronized(floorQueueLock) {
						floorQueueLock.wait();
						
						// if elevator has no events and has been signaled to exit, exit
						if (stopRequested && floorQueue.isEmpty()) {
							msgHandler.requestStop();
							errHandler.requestStop();
							break;
						}
					}
					
					// Wait a small amount of time in-case of incoming piled up duplicate requests
					//  so that we do not act too fast
					Thread.sleep(50L);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if (stopRequested && floorQueue.isEmpty()) {
				break;
			}

			targetFloor = floorQueue.remove();

			// get rid of duplicate floors in case multiple events are on the same floor
			while (!floorQueue.isEmpty() && floorQueue.peek().equals(targetFloor)) {
				floorQueue.remove();
			}

			// depending on the direction from the current floor, change elevator state
			if (currFloor < targetFloor) {
				changeState(ElevatorState.MOVING_UP);
			}
			else if (currFloor > targetFloor) {
				changeState(ElevatorState.MOVING_DOWN);
			}
			else {
				// if current floor = target floor, open right away
				changeState(ElevatorState.DOORS_OPEN);
			}
		}
		
		msgHandler.requestStop();
		errHandler.requestStop();
		
		// Exit once the message handler has stopped
		try {
			msgHandlerThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.log("EXITING", Log.Level.DEBUG);
	}

	/**
	 * Add the target and source floors to the floors that the elevator must stop at
	 * @param e
	 */
	public void pushEvent(FloorRequest e) {
		Log.log(name + " received event from Scheduler: " + e.toString(), Log.Level.INFO);

		// If the floorQueue is empty we could be going in a new direction
		// Change the floor queue to use the default ordering or downComparator depending on new direction
		if (floorQueue.isEmpty()) {
			if (e.getSourceFloor() < e.getDestFloor()) {
				floorQueue = new PriorityQueue<>(11);
			}
			else {
				floorQueue = new PriorityQueue<>(11, downComparator);
			}
		}

		// Add floors to stop at to the queue
		floorQueue.add(e.getDestFloor());
		floorQueue.add(e.getSourceFloor());
		
		// add request to recoverableEvents in-case there is a fault and another elevator must service
		//    these requests
		recoverableRequests.get(e.getSourceFloor()).add(e);
		
		// Add error types to the floors
		switch (e.getErrorType()) {
		case NO_ERROR:
			break;
			
		// Always stop floor in middle of the request
		case UNEXPECTED_STOP:
			int stopFloor = e.getSourceFloor() + Math.abs(e.getDestFloor() - e.getSourceFloor()) / 2;
			errors.get(stopFloor - 1).add(e.getErrorType());
			break;
			
		// Always throw the error at the floor we end at
		case DOORS_ERROR:
			errors.get(e.getDestFloor() - 1).add(e.getErrorType());
			break;
		
		default:
			throw new IllegalArgumentException("Unexpected error type!");
		}
		
		// Update floor occupancies
		addEventOccupancy(e);
		
		lastEvent = e;
		
		// Notify that the floorQueue has had new floors added to it
		synchronized(floorQueueLock) {
			floorQueueLock.notifyAll();
		}

	}
	
	/**
	 * Update the floor occupancy to increase between the src and dst floors of the request
	 */
	private void addEventOccupancy(FloorRequest floorRequest) {
		if (floorRequest.getDirection() == ButtonDirection.UP) {
			for (int i = floorRequest.getSourceFloor() - 1; i < floorRequest.getDestFloor(); ++i) {
				++floorOccupancy[i];
			}
		}
		else {
			for (int i = floorRequest.getDestFloor() - 1; i < floorRequest.getSourceFloor(); ++i) {
				++floorOccupancy[i];
			}
		}

	}
	
	/**
	 * Get the max occupancy between minFloor and maxFloor
	 */
	private int getMaxOccupancy(int minFloor, int maxFloor) {
		int occupancy = 0;
		for (int i = minFloor; i <= maxFloor; ++i) {
			occupancy = Integer.max(occupancy, floorOccupancy[i]);
		}
		return occupancy;
	}
	
	/**
	 * Get the max occupancy during the event of [e]
	 */
	public int getMaxOccupancy(FloorRequest e) {
		if (e.getDirection() == ButtonDirection.UP) {
			return getMaxOccupancy(e.getSourceFloor(), e.getDestFloor());
		}
		else {
			return getMaxOccupancy(e.getDestFloor(), e.getSourceFloor());
		}
	}
	

	private void onDoorsOpen() {
		Log.log("Elevator doors opened", Log.Level.INFO);
		
		recoverableRequests.get(currFloor).clear();
		
		Iterator<ErrorType> iter = errors.get(currFloor - 1).iterator();
		ErrorType error;
		while (iter.hasNext()) {
			error = iter.next();
			
			if (error == ErrorType.DOORS_ERROR) {
				// Wait to show that door has encountered an error and trigger error handler
				try {
					synchronized(this) {
						wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// clear error from floor
				iter.remove();
			}
		}

		// Doors open, wait a bit to close
		try {
			// NOTE: This uses actual load time from data from iteration 0
			if (!Config.USE_ZERO_FLOOR_TIME) {
				Thread.sleep(9350L);
			}
			else {
				Thread.sleep(50L);
			}
			
		} catch (InterruptedException e) {
			Log.log(e.getMessage());
		}

		changeState(ElevatorState.DOORS_CLOSED);
		recoverableRequests.get(currFloor).clear();
	}


	/**
	 * Moves floor-by-floor to the targetFloor 
	 * @param targetFloor
<<<<<<< Updated upstream
	 * @return successful
	 */
	private boolean moveToFloor(int targetFloor) {
=======
	 */
	private void moveToFloor(int targetFloor) {
>>>>>>> Stashed changes
		int delta = 1;
		if (currState == ElevatorState.MOVING_DOWN) {
			delta = -1;
		}

		while (currFloor != targetFloor) {
			currFloor += delta;
			
			Iterator<ErrorType> iter = errors.get(currFloor).iterator();
			ErrorType error;
<<<<<<< Updated upstream
=======
			
>>>>>>> Stashed changes
			while (iter.hasNext()) {
				error = iter.next();
				
				if (error == ErrorType.UNEXPECTED_STOP) {
<<<<<<< Updated upstream
					// Wait to show that elevator has encountered an error and trigger error handler
					try {
						synchronized(this) {
							wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// clear error from floor
					iter.remove();
					return false;
				}
			}
			
			notifyStatusChanged();
=======
					throwUnexpectedStopError();
					
					// clear error from floor
					iter.remove();
				}
			}
			

			// send message to notify elevator change
			ElevStatusNotify m = new ElevStatusNotify(elevatorId, getStatus());
			msgHandler.send(m);
>>>>>>> Stashed changes

			try {
				// NOTE: This uses actual time between floors from data from iteration 0
				if (!Config.USE_ZERO_FLOOR_TIME) {
					Thread.sleep(4750L);
				}
				else {
					Thread.sleep(50L);
				}

				Log.log("Elevator reached floor: " + currFloor, Log.Level.INFO);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
<<<<<<< Updated upstream
		
		return true;
=======
>>>>>>> Stashed changes
	}

	
	/**
	 * Manages state changes in accordance with the Elevator State Machine diagram
	 * @param newState the state to change to
	 */
	private void changeState(ElevatorState newState) {
		currState = newState;
<<<<<<< Updated upstream
=======
		
		ElevStatusNotify msg;
>>>>>>> Stashed changes

		switch(newState) {
		case MOVING_UP:
		case MOVING_DOWN:
<<<<<<< Updated upstream
			notifyStatusChanged();
			
			if (!moveToFloor(targetFloor)) {
				return;
			}
=======
			moveToFloor(targetFloor);
>>>>>>> Stashed changes
			
			// open doors once the floor has been reached
			changeState(ElevatorState.DOORS_OPEN);
			break;

		case DOORS_OPEN:
<<<<<<< Updated upstream
			notifyStatusChanged();
=======
			// send message to notify elevator change
			msg = new ElevStatusNotify(elevatorId, getStatus());
			msgHandler.send(msg);
>>>>>>> Stashed changes
			
			onDoorsOpen();
			break;

		case DOORS_CLOSED:
<<<<<<< Updated upstream
			notifyStatusChanged();
=======
			// send message to notify elevator change
			msg = new ElevStatusNotify(elevatorId, getStatus());
			msgHandler.send(msg);
>>>>>>> Stashed changes
			
			Log.log("Elevator doors have closed", Log.Level.INFO);
			if (floorQueue.isEmpty()) {
				changeState(ElevatorState.STOPPED);
			}
			break;

		case STOPPED:
			// reset occupancy
			for (int i = 0; i < floorOccupancy.length; ++i) {
				floorOccupancy[i] = 0;
			}
			
<<<<<<< Updated upstream
			notifyStatusChanged();
=======
			ElevStatusNotify m = new ElevStatusNotify(elevatorId, getStatus());
			msgHandler.send(m);
>>>>>>> Stashed changes
			break;
		}
	}
	
	
	/**
	 * This is a recoverable error that can so we do not unregister from the scheduler
	 * From the spec, we can assume that we always recover from this error, so we simulate the
	 *   elevator recovering with a time delay
	 */
<<<<<<< Updated upstream
	public void handleDoorError() {
=======
	private void throwDoorError() {
>>>>>>> Stashed changes
		Log.log("ERROR: Doors could not open / close.  Recovering...", Log.Level.INFO);
		
		try {
			// Simulate recovering from the error after a given amount of time
			// ie. a foot was put in the door, or the sensor didn't say the door was closed
			if (!Config.USE_ZERO_FLOOR_TIME) {
				Thread.sleep(10000L); // wait 10 seconds in normal time
			}
			else {
				Thread.sleep(2000L); // wait 2 seconds in quick time
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Log.log("Door error has been recovered.  Continuing.", Log.Level.INFO);
<<<<<<< Updated upstream
		
		errHandler.updateStatus(getStatus());
		
		synchronized(this) {
			notifyAll();
		}
=======
>>>>>>> Stashed changes
	}
	
	
	/**
	 * This is a non-recoverable error, so we must unregister from the Scheduler
	 * Any requests that have not been started (ie. passengers have not been picked up)
	 *   will be sent back to the Scheduler to be sent to another operable elevator
	 * Unfortunately, those in the elevator at the time of the fault will have to wait to be saved
	 *   and these requests will not be rescheduled to another elevator
	 */
<<<<<<< Updated upstream
	public void handleUnexpectedStopError() {
		try {
			msgHandler.unregister();
			forceStop();
=======
	private void throwUnexpectedStopError() {
		try {
			msgHandler.unregister();
>>>>>>> Stashed changes
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Log.log("ERROR: Elevator unexpectedly stopped between floors!", Log.Level.INFO);
		Log.log("Unrecoverable fault. Exiting", Log.Level.INFO);
		
<<<<<<< Updated upstream
		errHandler.updateStatus(getStatus());
		
		synchronized(this) {
			notifyAll();
		}
	}
	
	/**
	 * Notifies relevant subsystems & threads that this Elevator's status has changed
	 */
	private void notifyStatusChanged() {
		ElevStatusNotify m = new ElevStatusNotify(elevatorId, getStatus());
		msgHandler.send(m);
		errHandler.updateStatus(getStatus());
=======
		forceStop();
>>>>>>> Stashed changes
	}
	

	/**
	 * sets the isRunning value to false. Simulates the elevator not running
	 */
	public void requestStop() {
		stopRequested = true;
		synchronized(floorQueueLock) {
			floorQueueLock.notifyAll();
		}
	}
	
	/**
	 * sets the isRunning value to false. Simulates the elevator not running
	 */
	public void forceStop() {
		stopRequested = true;
		msgHandler.forceStop();
<<<<<<< Updated upstream
		errHandler.requestStop();
		
		// do not use changeState here on purpose.
		currState = ElevatorState.STOPPED;
		
=======
>>>>>>> Stashed changes
		synchronized(floorQueueLock) {
			floorQueue.clear();
			floorQueueLock.notifyAll();
		}
	}
	
	public ElevatorStatus getStatus() {
		return new ElevatorStatus(currFloor, currState, floorOccupancy);
	}

	/**
	 * @return the last event received from the scheduler
	 */
	public FloorRequest getLastEvent() {
		return this.lastEvent;
	}
	
	
	public ArrayList<FloorRequest> getRecoverableRequests() {
		ArrayList<FloorRequest> reqs = new ArrayList<>();
		
		for (Integer key : recoverableRequests.keySet()) {
			reqs.addAll(recoverableRequests.get(key));
		}
		
		return reqs;
	}
	
<<<<<<< Updated upstream
	
=======
	 private void initialize() {
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

	            buttons[i] = new JButton("Elevator " + elevatorId);
	            frame.getContentPane().add(buttons[i], "cell 0 " + i);
	        }
	        
	        
	        frame.getContentPane().add(requestLabel, "cell 3 5");
	        frame.getContentPane().add(directionLabel, "cell 3 7");
	        frame.getContentPane().add(destinationLabel, "cell 3 9");
	        frame.getContentPane().add(statusLabel, "cell 3 11");
	        
	        
	        
	        
	        buttons[MAX_FLOOR -1].setBackground(Color.GREEN);
	    }

	
	
	@SuppressWarnings("null")
>>>>>>> Stashed changes
	static public void main(String[] args) {
		// set to INFO for demo.  Use verbose / debug for testing
		Log.setLevel(Log.Level.INFO);
		
<<<<<<< Updated upstream
		Elevator e = new Elevator();
=======
		 Elevator instance = null;
		 instance = new Elevator();
		Elevator e = new Elevator();
        instance.initialize();
        instance.frame.setVisible(true);
>>>>>>> Stashed changes
		e.run();
	}
	
	
}
