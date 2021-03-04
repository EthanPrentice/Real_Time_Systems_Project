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
	private ArrayList<Elevator> elevators;
	
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
	public Scheduler(Floor floor, ArrayList<Elevator> elevators) {
		this.floor = floor;
		this.elevators = elevators;
	}
	
	
	@Override
	public synchronized void run() {
		while (!stopRequested) {
			
			// TODO: change to check if any elevators can be used to send an event to
			while (eventList.isEmpty()) {				
				try {
					wait();
					
					if (stopRequested) {
						stopElevators();
						return;
					}
					
					// if no more events in local queue or coming from the floor, stop elevator thread when possible and stop scheduler thread
					// this should exit the application once elevator threads have stopped
					if (eventList.isEmpty() && !floor.hasMoreEvents()) {
						stopElevators();
						return;
					}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			// send as all events to the elevators that can be sent in their current states
			sendEventsToElevators();
		}
	}
	
	
	/**
	 * Checks whether event can be sent to elevator in it's current state
	 */
	private boolean canSendEventToElevator(Elevator elevator, Event event) {
		return elevator.getState() == ElevatorState.STOPPED
			|| (elevator.getState() == ElevatorState.MOVING_UP && elevator.getFloor() <= event.getSourceFloor() && event.getDirection() == ButtonDirection.UP)
			|| (elevator.getState() == ElevatorState.MOVING_DOWN && elevator.getFloor() >= event.getSourceFloor() && event.getDirection() == ButtonDirection.DOWN);
	}
	
	
	/**
	 * Returns the next event in the event list that can be sent to the elevator
	 * @param elevator
	 * @return the next event that is schedulable to elevator, or null if none exists
	 */
	private Event getNextEventForElevator(Elevator elevator) {
		Iterator<Event> iter = eventList.iterator();
		while (iter.hasNext()) {
		   Event e = iter.next();
		   
		   // If elevator is stopped, or the event source floor is in the direction the elevator is currently moving, send the event to the elevator
		   if (canSendEventToElevator(elevator, e)) {
			   iter.remove();
			   return e;
		   }
		}
		
		return null;
	}
	
	
	/**
	 * Iterates over elevators, assigning events in the event list as evenly as possible across them
	 * Any events that cannot be assigned to elevators in their current state are left in the event list for
	 *   future scheduling
	 */
	private void sendEventsToElevators() {
		// bit i = 0 -> elevator has signaled it can no longer receive any of the queued events
		int elevatorCanReceiveEventFlag = ((1 << elevators.size()) - 1);
		
		while (elevatorCanReceiveEventFlag != 0) {
			for (int i = 0; i < elevators.size(); ++i) {
				
				if ((elevatorCanReceiveEventFlag & (1 << i)) == 0) { // elevator i cannot receive events
					continue;
				}
				
				Event e = getNextEventForElevator(elevators.get(i));
				if (e == null) {
					// set that we cannot send events to elevator i
					elevatorCanReceiveEventFlag &= ~(1 << i);
				}
				else {
					elevators.get(i).pushEvent(e);
				}
			}
		}
		
	}
	
	
	/**
	 * Requests that all elevators stop when they have finished their assigned jobs
	 */
	private void stopElevators() {
		for (Elevator elevator : elevators) {
			elevator.requestStop();
		}
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
		
		ArrayList<Elevator> elevators = new ArrayList<>();
		elevators.add(new Elevator("Elevator 1"));
		elevators.add(new Elevator("Elevator 2"));
		
		Scheduler scheduler = new Scheduler(floor, elevators);
		
		floor.setScheduler(scheduler);
		
		Thread floorThread = new Thread(floor, "Floor");
		Thread schedulerThread = new Thread(scheduler, "Scheduler");
		
		ArrayList<Thread> elevatorThreads = new ArrayList<>();
		for (Elevator elevator : elevators) {
			elevator.setScheduler(scheduler);
			
			Thread elevatorThread = new Thread(elevator, elevator.getName());
			elevatorThreads.add(elevatorThread);
			elevatorThread.start();
		}
		
		schedulerThread.start();
		floorThread.start();
		
	}
	


}
