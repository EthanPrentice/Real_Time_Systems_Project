package src;

import src.adt.*;
import util.Log;

import java.lang.Thread;
import java.util.PriorityQueue;

/**
 * Written for SYSC3303 - Group 6 - Iteration 1 @ Carleton University
 * Creates and runs the elevator sub-system as a thread. Here, the elevator thread gets and displays an event from scheduler, then sends the event back to scheduler
 * @author Nikhil Kharbanda 101012041
 */


public class Elevator implements Runnable {     

	private ElevatorState currState = ElevatorState.STOPPED;
	
	private Scheduler scheduler;	
	private PriorityQueue<Integer> floorQueue = new PriorityQueue<Integer>();

	private boolean isRunning = true;

	private int currFloor = 0;
	
	
	/**
	 * Runs the thread. Gets event from the Scheduler and then sends event back to the Scheduler
	 */
	public synchronized void run() {
		while(isRunning) {         // while elevator is running
			
			if (floorQueue.isEmpty()) {
				try {
					synchronized(floorQueue) {
						floorQueue.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			int targetFloor = floorQueue.remove();
			
			// get rid of duplicate floors
			while (!floorQueue.isEmpty() && floorQueue.peek() == targetFloor) {
				floorQueue.remove();
			}
			
			if (currFloor < targetFloor) {
				changeState(ElevatorState.MOVING_UP);
			}
			else if (currFloor > targetFloor) {
				changeState(ElevatorState.MOVING_DOWN);
			}
			moveToEventFloor(targetFloor);
			
			changeState(ElevatorState.DOORS_OPEN);
		}
	}
	
	/**
	 * Add the target and source floors to the floors that the elevator must stop at
	 * @param e
	 */
	public void pushEvent(Event e) {
		Log.log("Elevator received event from Scheduler: " + e.toString());
		
		floorQueue.add(e.getDestFloor());
		floorQueue.add(e.getSourceFloor());
		
		synchronized(floorQueue) {
			floorQueue.notifyAll();
		}
		
	}
	
	
	private void onDoorsOpen() {
		Log.log("Elevator doors opened");
		
		// Doors open, wait a bit to close
		try {
			// TODO: change to actual load time
			Thread.sleep(500L);           // sleep for 500ms second
		} catch (InterruptedException e) {
			Log.log(e.getMessage());
		}
		
		changeState(ElevatorState.DOORS_CLOSED);
	}
	
	
	private void moveToEventFloor(int targetFloor) {		
		int delta = 1;
		if (currState == ElevatorState.MOVING_DOWN) {
			delta = -1;
		}
		
		while (currFloor != targetFloor) {
			currFloor += delta;
			
			scheduler.notifyElevatorFloorChange(this, currFloor);
			
			try {
				// TODO: change to real time between floors
				Thread.sleep(1000L);
				
				Log.log("Elevator reached floor: " + currFloor);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}
	
	
	private void changeState(ElevatorState newState) {
		currState = newState;
		
		switch(newState) {
		case MOVING_UP:
		case MOVING_DOWN:
			
			break;
			
		case DOORS_OPEN:
			onDoorsOpen();
			break;
			
		case DOORS_CLOSED:
			Log.log("Elevator doors have closed");
			changeState(ElevatorState.STOPPED);
			break;
			
		case STOPPED:
			
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
	public void stop() {
		isRunning = false;
	}
	
	/**
	 * @return the floor the elevator is currently on, with 0 being ground
	 */
	public int getFloor() {
		return currFloor;
	}
	
	public ElevatorState getState() {
		return currState;
	}
}


