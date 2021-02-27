package src;

import java.util.ArrayList;
import java.util.Iterator;

import src.adt.*;
import util.Log;

/**
 * Recieves events from Floor and saves in Queue. Reads events from Queue and sends to elevator. Receives Event from Elevator and sends to Floor
 * @author Baillie Noell 101066676 Group 6
 * @edited Ethan Prentice (101070194)
 */
public class Scheduler implements Runnable {
	
	private ArrayList<Event> eventList = new ArrayList<Event>();
	private Floor floor;
	private Elevator elevator;
	private Event elevatorEvent;
	private Event lastFloorEvent;
	
	private int upEventCount = 0;
	private int downEventCount = 0;
	
	private boolean stopRequested = false;
	
	/**
	 * Constructor, saves references to the floor and elevator
	 * @param floor
	 * @param elevator
	 */
	public Scheduler(Floor floor, Elevator elevator) {
		this.floor = floor;
		this.elevator = elevator;
	}
	
	
	@Override
	public synchronized void run() {
		while (!stopRequested) {
			
			// TODO: change to check if any elevators can be used to send an event to
			while (eventList.isEmpty() || !canSendEventToElevator(elevator)) {				
				try {
					wait();
					
					if (stopRequested) {
						return;
					}
					
					if (eventList.isEmpty() && !floor.hasMoreEvents()) {
						elevator.stop();
						return;
					}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			sendEventsToElevator();
		}
	}
	
	
	/**
	 * Checks whether an elevator, e, can a request in it's current state
	 */
	private boolean canSendEventToElevator(Elevator e) {
		switch(e.getState()) {
		case STOPPED: return true;
		case MOVING_DOWN: return downEventCount > 0;
		case MOVING_UP: return upEventCount > 0;
		default:
			return false;
		}
	}
	
	
	/**
	 * Receives event from floor and stores in a queue. 
	 * @param event
	 */
	public synchronized void putEventFromFloor(Event event) {
		if (event.getDirection() == ButtonDirection.UP) {
			++upEventCount;
		}
		else {
			++downEventCount;
		}
		
		eventList.add(event);
		lastFloorEvent = event;
		Log.log("Scheduler: Recieved event from Floor. Event: " + event.toString());
		
		// notify we have events
		notifyAll();
		

	}
	
	/**
	 * Sends the event to any appropriate elevators
	 */
	private void sendEventsToElevator() {
		Iterator<Event> iter = eventList.iterator();
		while (iter.hasNext()) {
		   Event e = iter.next();
		   
		   elevator.pushEvent(e);
		   
			if (e.getDirection() == ButtonDirection.UP) {
				--upEventCount;
			}
			else {
				--downEventCount;
			}
		   
		   iter.remove();
		}
	}	
	
	/**
	 * Scheduler receives Event from Elevator and sends to Floor
	 * @param e: elevator that's floor has changed
	 * @param newFloor: e's new floor
	 */
	public void sendEventToFloor(Elevator e, int newFloor) {
		Log.log("Scheduler: Sent floor changed event to Floor. Elevator now on floor: " + newFloor);
		floor.onElevatorFloorChanged(e, newFloor);
	}
	
	/**
	 * Elevator data getter method
	 * @return last event object received from elevator
	 */
	public Event getElevatorEvent() {
		return this.elevatorEvent;
	}
	
	/**
	 * Returns the last floor event received
	 * @return lastFloorEvent
	 */
	public Event getLastFloorEvent() {
		return this.lastFloorEvent;
	}
	/**
	 * Called by the elevator to notify there was a floor change
	 * @param e
	 * @param newFloor
	 */
	public synchronized void notifyElevatorFloorChange(Elevator e, int newFloor) {
		Log.log("Scheduler: Recieved floor changed event from Elevator. Elevator now on floor: " + newFloor);
		sendEventToFloor(e, newFloor);
		
		notifyAll();
	}
	
	
	/**
	 * Stops the runnable from looping, and exits run() after the current request has been executed
	 */
	public void requestStop() {
		stopRequested = true;
		notifyAll();
	}
	
	
	/**
	 * Main Thread, creates Scheduler and starts Floor and Elevator Threads
	 * @param args
	 */
	public static void main(String[] args) {
		Floor floor = new Floor();
		Elevator elevator = new Elevator();
		Scheduler scheduler = new Scheduler(floor, elevator);
		
		floor.setScheduler(scheduler);
		elevator.setScheduler(scheduler);
		
		Thread floorThread = new Thread(floor, "Floor");
		Thread elevThread = new Thread(elevator, "Elevator");
		Thread schedulerThread = new Thread(scheduler, "Scheduler");
		
		schedulerThread.start();
		floorThread.start();
		elevThread.start();
	}
	


}
