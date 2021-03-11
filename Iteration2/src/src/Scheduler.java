package src;

import java.util.ArrayList;
import java.util.Iterator;

import src.adt.*;
import src.adt.message.FloorRequest;
import util.Log;

/**
 * Recieves events from Floor and saves in Queue. Reads events from Queue and sends to elevator. Receives Event from Elevator and sends to Floor
 * @author Baillie Noell 101066676 Group 6
 * @edited Ethan Prentice (101070194)
 */
public class Scheduler implements Runnable {
	
	public final static int RECEIVE_PORT = 23;
	
	private Floor floor;
	
	// all elevators
	private ArrayList<Elevator> elevators;
	
	private ArrayList<Elevator> upElevators = new ArrayList<>();
	private ArrayList<Elevator> downElevators = new ArrayList<>();
	private ArrayList<Elevator> stoppedElevators = new ArrayList<>();
	
	
	// List of events received from the floor and not yet sent to the elevators
	private ArrayList<FloorRequest> eventList = new ArrayList<FloorRequest>();

	// Used for testing
	private FloorRequest elevatorEvent;
	private FloorRequest lastFloorEvent;
	
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
		this.stoppedElevators.addAll(elevators);
	}
	
	
	@Override
	public synchronized void run() {
		while (!stopRequested) {
			
			// send as all events to the elevators that can be sent in their current states
			while (eventList.isEmpty() || !sendEventsToElevators()) {				
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
		}
	}
	
	
	/**
	 * Checks whether event can be sent to elevator in it's current state
	 */
	private boolean canSendEventToElevator(Elevator elevator, FloorRequest floorRequest) {
		return elevator.getState() == ElevatorState.STOPPED
			|| (elevator.getState() == ElevatorState.MOVING_UP && elevator.getFloor() <= floorRequest.getSourceFloor() && floorRequest.getDirection() == ButtonDirection.UP)
			|| (elevator.getState() == ElevatorState.MOVING_DOWN && elevator.getFloor() >= floorRequest.getSourceFloor() && floorRequest.getDirection() == ButtonDirection.DOWN);
	}
	
	
	/**
	 * Returns the next event in the event list that can be sent to the elevator
	 * @param elevator
	 * @return the next event that is schedulable to elevator, or null if none exists
	 */
	private FloorRequest getNextEventForElevator(Elevator elevator) {
		Iterator<FloorRequest> iter = eventList.iterator();
		while (iter.hasNext()) {
		   FloorRequest e = iter.next();
		   
		   // If elevator is stopped, or the event source floor is in the direction the elevator is currently moving, send the event to the elevator
		   if (canSendEventToElevator(elevator, e)) {
			   iter.remove();
			   return e;
		   }
		}
		
		return null;
	}
	
	
	private Elevator getClosestElevatorFrom(FloorRequest floorRequest, ArrayList<Elevator> elevators) {
		Elevator closestElevator = null;
		
		for (Elevator elevator : elevators) {
			if (canSendEventToElevator(elevator, floorRequest)) {
				if (floorRequest.getDirection() == ButtonDirection.UP) {
					if (closestElevator == null || closestElevator.getFloor() > elevator.getFloor()) {
						closestElevator = elevator;
						continue;
					}
				}
				else {
					if (closestElevator == null || closestElevator.getFloor() < elevator.getFloor()) {
						closestElevator = elevator;
						continue;
					}
				}

				if (closestElevator.getFloor() == elevator.getFloor()) {
					if (elevator.getMaxOccupancy(floorRequest) < closestElevator.getMaxOccupancy(floorRequest)) {
						closestElevator = elevator;
					}
				}
			}
		}
		
		return closestElevator;
	}
	
	private Elevator getClosestElevator(FloorRequest floorRequest) {
		Elevator closestElevator = null;
		
		if (floorRequest.getDirection() == ButtonDirection.UP) {
			Elevator closestUp = getClosestElevatorFrom(floorRequest, upElevators);
			
			if (upElevators.size() >= Math.ceil(elevators.size() / 2f)) {
				// cannot take anymore stopped elevators to up, or else we would have over half allocated to up
				return closestUp;
			}
			
			Elevator closestStopped = getClosestElevatorFrom(floorRequest, stoppedElevators);
			
			if (closestUp == null && closestStopped == null) {
				return null;
			}
			else if (closestUp == null || closestUp.getFloor() > closestStopped.getFloor()) {
				return closestStopped;
			}
			else if (closestStopped == null || closestUp.getFloor() < closestStopped.getFloor()) {
				return closestUp;
			}
			else if (closestUp.getMaxOccupancy(floorRequest) <= closestStopped.getMaxOccupancy(floorRequest)) {
				return closestUp;
			}
			else {
				return closestStopped;
			}
		}
		else if (floorRequest.getDirection() == ButtonDirection.DOWN) {
			Elevator closestDown = getClosestElevatorFrom(floorRequest, downElevators);
			
			if (downElevators.size() >= Math.ceil(elevators.size() / 2f)) {
				// cannot take anymore stopped elevators to up, or else we would have over half allocated to up
				return closestDown;
			}
			
			Elevator closestStopped = getClosestElevatorFrom(floorRequest, stoppedElevators);
			
			if (closestDown == null && closestStopped == null) {
				return null;
			}
			else if (closestDown == null || closestDown.getFloor() < closestStopped.getFloor()) {
				return closestStopped;
			}
			else if (closestStopped == null || closestDown.getFloor() > closestStopped.getFloor()) {
				return closestDown;
			}
			else if (closestDown.getMaxOccupancy(floorRequest) <= closestStopped.getMaxOccupancy(floorRequest)) {
				return closestDown;
			}
			else {
				return closestStopped;
			}
		}
		
		return closestElevator;
	}
	
	
	/**
	 * Iterates over elevators, assigning events in the event list as evenly as possible across them
	 * Any events that cannot be assigned to elevators in their current state are left in the event list for
	 *   future scheduling
	 *   
	 *   @return true if there were events sent.  false otherwise
	 */
	private boolean sendEventsToElevators() {
		boolean sentAnEvent = false;
		
		Iterator<FloorRequest> iter = eventList.iterator();
		while (iter.hasNext()) {
			FloorRequest e = iter.next();
			Elevator elevator = getClosestElevator(e);
			
			if (elevator == null) {
				continue;
			}
			
			elevator.pushEvent(e);
			iter.remove();
			sentAnEvent = true;
			
			stoppedElevators.remove(elevator);
			
			if (elevator.getState() == ElevatorState.STOPPED) {
				if (e.getDirection() == ButtonDirection.UP) {
					upElevators.add(elevator);
				}
				else {
					downElevators.add(elevator);
				}
			}
		}
		
		return sentAnEvent;
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
	 * @param floorRequest
	 */
	public synchronized void putEventFromFloor(FloorRequest floorRequest) {
		eventList.add(floorRequest);
		lastFloorEvent = floorRequest;
		Log.log("Scheduler: Recieved event from Floor. Event: " + floorRequest.toString());
		
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
	public FloorRequest getElevatorEvent() {
		return this.elevatorEvent;
	}
	
	/**
	 * Returns the last floor event received
	 * @return lastFloorEvent
	 */
	public FloorRequest getLastFloorEvent() {
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
		upElevators.remove(e);
		downElevators.remove(e);
		stoppedElevators.add(e);
		
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
