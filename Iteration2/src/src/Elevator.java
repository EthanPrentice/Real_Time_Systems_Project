package src;

import src.adt.*;
import src.adt.message.FloorRequest;
import util.Config;
import util.Log;

import java.lang.Thread;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Written for SYSC3303 - Group 6 - Iteration 1 @ Carleton University
 * Creates and runs the elevator sub-system as a thread. Here, the elevator thread gets and displays an event from scheduler, then sends the event back to scheduler
 * @author Nikhil Kharbanda 101012041
 * @edited Ethan Prentice (101070194)
 */


public class Elevator implements Runnable {
	
	private static char elevatorCount = 0;
	
	
	private char elevatorId;
	
	private Scheduler scheduler;
	
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
    
    // holds how many people increase in elevator at a given floor
    private int[] floorOccupancy = new int[Config.NUM_FLOORS];

	private boolean stopRequested = false;

	// keep track of the floor the elevator is going to, and the floor it is currently on
	// 0 is the ground floor and all elevators are initialized to this floor
	private int targetFloor = 0;
	private int currFloor = 0;
	

	// testing purposes
	private FloorRequest lastEvent;
	
	
	public Elevator(String name) {
		this.name = name;
		this.elevatorId = elevatorCount++;
	}
	

	/**
	 * Runs the thread. Gets event from the Scheduler and then sends event back to the Scheduler
	 */
	public synchronized void run() {
		while(!stopRequested || !floorQueue.isEmpty()) {         // while elevator is running, or has more events in it's queue

			while (floorQueue.isEmpty()) {
				try {
					synchronized(floorQueueLock) {
						floorQueueLock.wait();
						
						// if elevator has no events and has been signaled to exit, exit
						if (stopRequested && floorQueue.isEmpty()) {
							return;
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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

		floorQueue.add(e.getDestFloor());
		floorQueue.add(e.getSourceFloor());
		
		addEventOccupancy(e);
		
		lastEvent = e;
		
		// Notify that the floorQueue has had new floors added to it
		synchronized(floorQueueLock) {
			floorQueueLock.notifyAll();
		}

	}
	
	
	private void addEventOccupancy(FloorRequest floorRequest) {
		for (int i = floorRequest.getSourceFloor() - 1; i < floorRequest.getDestFloor(); ++i) {
			++floorOccupancy[i];
		}
	}
	
	private int getMaxOccupancy(int minFloor, int maxFloor) {
		int occupancy = 0;
		for (int i = minFloor; i <= maxFloor; ++i) {
			occupancy = Integer.max(occupancy, floorOccupancy[i]);
		}
		return occupancy;
	}
	
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

		// Doors open, wait a bit to close
		try {
			// TODO: change to actual load time
			Thread.sleep(500L);           // sleep for 500ms second
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

			// TODO: send message to notify elevator change
			// ElevStatusRequest
			scheduler.notifyElevatorFloorChange(this, currFloor);

			try {
				// TODO: change to real time between floors
				Thread.sleep(1000L);

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

		switch(newState) {
		case MOVING_UP:
		case MOVING_DOWN:
			moveToFloor(targetFloor);
			// open doors once the floor has been reached
			changeState(ElevatorState.DOORS_OPEN);
			break;

		case DOORS_OPEN:
			onDoorsOpen();
			break;

		case DOORS_CLOSED:
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
			scheduler.notifyElevatorStopped(this);
			break;
		}
	}


	/**
	 * Setter method for scheduler
	 * @param s
	 */
	public void setScheduler(Scheduler s) {
		scheduler = s;
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
	 * @return the floor the elevator is currently on, with 0 being ground
	 */
	public int getFloor() {
		return currFloor;
	}

	/**
	 * @return the elevator's current ElevatorState
	 */
	public ElevatorState getState() {
		return currState;
	}

	/**
	 * @return the last event received from the scheduler
	 */
	public FloorRequest getLastEvent() {
		return this.lastEvent;
	}
	
	/**
	 * @return the name of the elevator
	 */
	public String getName() {
		return name;
	}
	
	public char getElevatorId() {
		return elevatorId;
	}
}
