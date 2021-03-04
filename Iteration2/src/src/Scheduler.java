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
	
	private Floor floor;
	private Elevator elevator;
	
	// List of events received from the floor and not yet sent to the elevators
	private ArrayList<Event> eventList = new ArrayList<Event>();

	// Used for testing
	private Event elevatorEvent;
	private Event lastFloorEvent;
	
	// true if scheduler thread should stop when it is safe to do so
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
						elevator.requestStop();
						return;
					}
					
					// if no more events in local queue or coming from the floor, stop elevator thread when possible and stop scheduler thread
					// this should exit the application once elevator threads have stopped
					if (eventList.isEmpty() && !floor.hasMoreEvents()) {
						elevator.requestStop();
						return;
					}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			// send as all events to the elevators that can be sent in their current states
			sendEventsToElevator();
		}
	}
	
	
	/**
	 * Checks whether an elevator, e, can a request in it's current state
	 */
	private boolean canSendEventToElevator(Elevator elevator) {
		Iterator<Event> iter = eventList.iterator();
		while (iter.hasNext()) {
		   Event e = iter.next();
		   
		   // If elevator is stopped, or the event source floor is in the direction the elevator is currently moving, return true
		   if (canSendEventToElevator(elevator, e)) {
			   return true;
		   }
		}
		
		return false;
	}
	
	
	/**
	 * Checks whether event can be sent to elevator
	 */
	private boolean canSendEventToElevator(Elevator elevator, Event event) {
		return elevator.getState() == ElevatorState.STOPPED
			|| (elevator.getState() == ElevatorState.MOVING_UP && elevator.getFloor() <= event.getSourceFloor() && event.getDirection() == ButtonDirection.UP)
			|| (elevator.getState() == ElevatorState.MOVING_DOWN && elevator.getFloor() >= event.getSourceFloor() && event.getDirection() == ButtonDirection.DOWN);
	}
	
	
	/**
	 * Receives event from floor and stores in a queue. 
	 * @param event
	 */
	public synchronized void putEventFromFloor(Event event) {
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
		   
		   // If elevator is stopped, or the event source floor is in the direction the elevator is currently moving, send the event to the elevator
		   if (canSendEventToElevator(elevator, e)) {
			   elevator.pushEvent(e);
			   iter.remove();
		   }
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
	}
	
	
	/**
	 * When the elevator stops, notify the scheduler it can send another event
	 * @param e
	 */
	public synchronized void notifyElevatorStopped(Elevator e) {
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
		// set to INFO for demo.  Use verbose / debug for testing
		Log.setLevel(Log.Level.INFO);
		
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
