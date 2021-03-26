package src;

import src.adt.*;
import src.adt.message.ElevStatusNotify;
import src.adt.message.FloorRequest;
import util.Config;
import util.Log;

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
	
//	// floor to array of FloorRequests with key as their source floor
//	private HashMap<Integer, FloorRequest> recoverableRequests;
	
	
	/*
	 * Initializes variables and runs the MessageHandler in a separate thread
	 */
	public void init() {
		for (int i = 0; i < Config.NUM_FLOORS; ++i) {
			errors.add(new ArrayList<ErrorType>());
		}
		
		msgHandler = new ElevatorMessageHandler(this);
		elevatorId = (char) msgHandler.getPort();
		name = "Elevator " + (int) elevatorId;
		
		Thread.currentThread().setName(name);
		
		msgHandlerThread = new Thread(msgHandler, name + " MsgHandler");
		msgHandlerThread.start();
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
							break;
						}
					}
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
		
		// Add error types to the floors
		switch (e.getErrorType()) {
		case NO_ERROR:
			break;
			
		// Always stop floor before the destination floor
		case UNEXPECTED_STOP:
			int stopFloor = e.getDestFloor() + (e.getDirection() == ButtonDirection.DOWN ? 1 : -1);
			errors.get(stopFloor - 1).add(e.getErrorType());
			break;
			
		// Always throw the error at the floor we start at
		case DOORS_ERROR:
			errors.get(e.getSourceFloor() - 1).add(e.getErrorType());
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
		
		Iterator<ErrorType> iter = errors.get(currFloor - 1).iterator();
		ErrorType error;
		while (iter.hasNext()) {
			error = iter.next();
			
			if (error == ErrorType.DOORS_ERROR) {
				throwDoorError();
				
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
	}


	/**
	 * Moves floor-by-floor to the targetFloor 
	 * @param targetFloor
	 */
	private void moveToFloor(int targetFloor) {
		int delta = 1;
		if (currState == ElevatorState.MOVING_DOWN) {
			delta = -1;
		}

		while (currFloor != targetFloor) {
			currFloor += delta;
			
			
			Iterator<ErrorType> iter = errors.get(currFloor).iterator();
			ErrorType error;
			while (iter.hasNext()) {
				error = iter.next();
				
				if (error == ErrorType.UNEXPECTED_STOP) {
					throwUnexpectedStopError();
					
					// clear error from floor
					iter.remove();
				}
			}
			

			// send message to notify elevator change
			ElevStatusNotify m = new ElevStatusNotify(elevatorId, getStatus());
			msgHandler.send(m);

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
	}

	
	/**
	 * Manages state changes in accordance with the Elevator State Machine diagram
	 * @param newState the state to change to
	 */
	private void changeState(ElevatorState newState) {
		currState = newState;
		
		ElevStatusNotify msg;

		switch(newState) {
		case MOVING_UP:
		case MOVING_DOWN:
			moveToFloor(targetFloor);
			
			// open doors once the floor has been reached
			changeState(ElevatorState.DOORS_OPEN);
			break;

		case DOORS_OPEN:
			// send message to notify elevator change
			msg = new ElevStatusNotify(elevatorId, getStatus());
			msgHandler.send(msg);
			
			onDoorsOpen();
			break;

		case DOORS_CLOSED:
			// send message to notify elevator change
			msg = new ElevStatusNotify(elevatorId, getStatus());
			msgHandler.send(msg);
			
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
			
			ElevStatusNotify m = new ElevStatusNotify(elevatorId, getStatus());
			msgHandler.send(m);
			break;
		}
	}
	
	
	private void throwDoorError() {
		try {
			msgHandler.unregister();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Log.log("ERROR: Doors could not open / close.  Recovering...", Log.Level.INFO);
		
		try {
			// Recover from the error
			if (!Config.USE_ZERO_FLOOR_TIME) {
				Thread.sleep(10000L); // wait 10 seconds in normal time
			}
			else {
				Thread.sleep(5000L); // wait 5 seconds in quick time
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Log.log("Door error has been recovered.  Continuing.", Log.Level.INFO);
		
		try {
			msgHandler.register();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void throwUnexpectedStopError() {
		try {
			msgHandler.unregister();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Log.log("ERROR: Elevator unexpectedly stopped between floors!", Log.Level.INFO);
		Log.log("Unrecoverable fault. Exiting", Log.Level.INFO);
		
		System.exit(-1);
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
	
	public ElevatorStatus getStatus() {
		return new ElevatorStatus(currFloor, currState, floorOccupancy);
	}

	/**
	 * @return the last event received from the scheduler
	 */
	public FloorRequest getLastEvent() {
		return this.lastEvent;
	}
	
	
	static public void main(String[] args) {
		// set to INFO for demo.  Use verbose / debug for testing
		Log.setLevel(Log.Level.INFO);
		
		Elevator e = new Elevator();
		e.run();
	}
	
	
}
