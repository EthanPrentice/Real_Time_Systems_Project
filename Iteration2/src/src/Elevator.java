package src;

import src.adt.*;
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

	private ElevatorState currState = ElevatorState.STOPPED;

	private Scheduler scheduler;

	private Object floorQueueLock = new Object();
	private PriorityQueue<Integer> floorQueue = new PriorityQueue<Integer>();

	private Comparator<Integer> downComparator = new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
        }
    };

	private boolean isRunning = true;

	private int targetFloor = 0;
	private int currFloor = 0;

	private Event lastEvent;

	/**
	 * Runs the thread. Gets event from the Scheduler and then sends event back to the Scheduler
	 */
	public synchronized void run() {
		while(isRunning || !floorQueue.isEmpty()) {         // while elevator is running

			while (floorQueue.isEmpty()) {
				try {
					synchronized(floorQueueLock) {
						floorQueueLock.wait();

						if (!isRunning && floorQueue.isEmpty()) {
							return;
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			targetFloor = floorQueue.remove();

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

			changeState(ElevatorState.DOORS_OPEN);
		}
	}

	/**
	 * Add the target and source floors to the floors that the elevator must stop at
	 * @param e
	 */
	public void pushEvent(Event e) {
		Log.log("Elevator received event from Scheduler: " + e.toString());

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
		
		lastEvent = e;
		synchronized(floorQueueLock) {
			floorQueueLock.notifyAll();
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

	/**
	 * Manages state changes in accordance with the Elevator State Machine diagram
	 * @param newState the state to change to
	 */
	private void changeState(ElevatorState newState) {
		currState = newState;

		switch(newState) {
		case MOVING_UP:
			moveToEventFloor(targetFloor);
			break;
		case MOVING_DOWN:
			moveToEventFloor(targetFloor);
			break;

		case DOORS_OPEN:
			onDoorsOpen();
			break;

		case DOORS_CLOSED:
			Log.log("Elevator doors have closed");
			changeState(ElevatorState.STOPPED);
			break;

		case STOPPED:
			if (floorQueue.isEmpty()) {
				scheduler.notifyElevatorStopped(this);
			}
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
	 *
	 * @return the last event received from the scheduler
	 */
	public Event getLastEvent() {
		return this.lastEvent;
	}
}
