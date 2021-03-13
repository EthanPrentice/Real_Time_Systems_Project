package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import src.adt.*;
import src.adt.message.FloorRequest;
import src.adt.message.StopRequest;
import util.Log;

/**
 * Receives events from Floor and saves in Queue. Reads events from Queue and sends to elevator. Receives Event from Elevator and sends to Floor
 * @author Baillie Noell 101066676 Group 6
 * @edited Ethan Prentice (101070194)
 */
public class Scheduler implements Runnable {
	
	public final static int RECEIVE_PORT = 23;
	
	private Thread msgHandlerThread;
	private SchedulerMessageHandler msgHandler;
	
	private int floorPort = 0;
	private boolean floorHasMoreEvents = true;
	
	// all elevators IDs -> ports
	private HashMap<Character, Integer> elevators = new HashMap<>();
	
	// sets of elevator IDs going in directions
	private HashSet<Character> upElevators = new HashSet<>();
	private HashSet<Character> downElevators = new HashSet<>();
	private HashSet<Character> stoppedElevators = new HashSet<>();
	
	// elevator id -> last reported status
	private HashMap<Character, ElevatorStatus> elevatorStatuses = new HashMap<>();
	
	
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
	public Scheduler() {		
		msgHandler = new SchedulerMessageHandler(this);
		msgHandlerThread = new Thread(msgHandler, "Scheduler MsgHandler");
		msgHandlerThread.start();
	}
	
	
	@Override
	public synchronized void run() {
		while (!stopRequested) {
			
			// send as all events to the elevators that can be sent in their current states
			while (eventList.isEmpty() || !sendEventsToElevators()) {				
				try {
					wait();
					
					// if no more events in local queue or coming from the floor, stop elevator thread when possible and stop scheduler thread
					// this should exit the application once elevator threads have stopped
					if (eventList.isEmpty() && !floorHasMoreEvents) {
						stopElevators();
						msgHandler.requestStop();
						
						// Exit once the message handler has stopped
						try {
							msgHandlerThread.join();
							return;
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
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
	private boolean canSendEventToElevator(Character elevatorId, FloorRequest floorRequest) {
		ElevatorStatus status = elevatorStatuses.get(elevatorId);
		if (status == null) {
			return false;
		}
		
		return status.getState() == ElevatorState.STOPPED
			|| (status.getState() == ElevatorState.MOVING_UP && status.getFloor() <= floorRequest.getSourceFloor() && floorRequest.getDirection() == ButtonDirection.UP)
			|| (status.getState() == ElevatorState.MOVING_DOWN && status.getFloor() >= floorRequest.getSourceFloor() && floorRequest.getDirection() == ButtonDirection.DOWN);
	}
	
	
	private Character getClosestElevatorFrom(FloorRequest floorRequest, HashSet<Character> elevators) {
		Character closestElevator = null;
		ElevatorStatus closestStatus = null;
		
		for (Character elevatorId : elevators) {
			if (canSendEventToElevator(elevatorId, floorRequest)) {
				ElevatorStatus currStatus = elevatorStatuses.get(elevatorId);
				if (currStatus == null) {
					continue;
				}
				
				if (floorRequest.getDirection() == ButtonDirection.UP) {
					if (closestElevator == null || closestStatus.getFloor() > currStatus.getFloor()) {
						closestElevator = elevatorId;
						closestStatus = elevatorStatuses.get(elevatorId);
						continue;
					}
				}
				else {
					if (closestElevator == null || closestStatus.getFloor() < currStatus.getFloor()) {
						closestElevator = elevatorId;
						closestStatus = elevatorStatuses.get(elevatorId);
						continue;
					}
				}

				if (closestStatus.getFloor() == currStatus.getFloor()) {
					if (currStatus.getMaxOccupancy(floorRequest) < closestStatus.getMaxOccupancy(floorRequest)) {
						closestElevator = elevatorId;
						closestStatus = elevatorStatuses.get(elevatorId);
					}
				}
			}
		}
		
		return closestElevator;
	}
	
	private Character getClosestElevator(FloorRequest floorRequest) {
		Character closestElevator = null;
		
		if (floorRequest.getDirection() == ButtonDirection.UP) {
			Character closestUp = getClosestElevatorFrom(floorRequest, upElevators);
			ElevatorStatus upStatus = null;
			if (closestUp != null) {
				upStatus = elevatorStatuses.get(closestUp);
			}
			 
			
			if (upElevators.size() >= Math.ceil(elevators.size() / 2f)) {
				// cannot take anymore stopped elevators to up, or else we would have over half allocated to up
				return closestUp;
			}
			
			Character closestStopped = getClosestElevatorFrom(floorRequest, stoppedElevators);
			ElevatorStatus stoppedStatus = null;
			if (closestStopped != null) {
				 stoppedStatus = elevatorStatuses.get(closestStopped);
			}
			
			if (closestUp == null && closestStopped == null) {
				return null;
			}
			else if (closestUp == null || (closestStopped != null && upStatus.getFloor() > stoppedStatus.getFloor())) {
				return closestStopped;
			}
			else if (closestStopped == null || upStatus.getFloor() < stoppedStatus.getFloor()) {
				return closestUp;
			}
			else if (upStatus.getMaxOccupancy(floorRequest) <= stoppedStatus.getMaxOccupancy(floorRequest)) {
				return closestUp;
			}
			else {
				return closestStopped;
			}
		}
		else if (floorRequest.getDirection() == ButtonDirection.DOWN) {
			Character closestDown = getClosestElevatorFrom(floorRequest, downElevators);
			ElevatorStatus downStatus = null;
			if (closestDown != null) {
				downStatus = elevatorStatuses.get(closestDown);
			}
			
			if (downElevators.size() >= Math.ceil(elevators.size() / 2f)) {
				// cannot take anymore stopped elevators to up, or else we would have over half allocated to up
			}
			
			Character closestStopped = getClosestElevatorFrom(floorRequest, stoppedElevators);
			ElevatorStatus stoppedStatus = null;
			if (closestStopped != null) {
				 stoppedStatus = elevatorStatuses.get(closestStopped);
			}
			
			if (closestDown == null && closestStopped == null) {
				return null;
			}
			else if (closestDown == null || (closestStopped != null && downStatus.getFloor() < stoppedStatus.getFloor())) {
				return closestStopped;
			}
			else if (closestStopped == null || downStatus.getFloor() > stoppedStatus.getFloor()) {
				return closestDown;
			}
			else if (downStatus.getMaxOccupancy(floorRequest) <= stoppedStatus.getMaxOccupancy(floorRequest)) {
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
			FloorRequest req = iter.next();
			Character elevatorId = getClosestElevator(req);
			
			if (elevatorId == null) {
				continue;
			}
			
			Log.log("Sent " + req.toString() + " to Elevator with id=" + (int) elevatorId, Log.Level.INFO);
			msgHandler.send(req, elevators.get(elevatorId));
			
			iter.remove();
			sentAnEvent = true;
			
			stoppedElevators.remove(elevatorId);
			
			if (elevatorStatuses.get(elevatorId).getState() == ElevatorState.STOPPED) {
				if (req.getDirection() == ButtonDirection.UP) {
					upElevators.add(elevatorId);
				}
				else {
					downElevators.add(elevatorId);
				}
			}
		}
		
		return sentAnEvent;
	}
	
	
	private void stopElevators() {
		for (int port : elevators.values()) {
			msgHandler.send(new StopRequest(), port);
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
	
	public int getFloorPort() {
		return floorPort;
	}
	
	public void setFloorHasMoreEvents(boolean newVal) {
		floorHasMoreEvents = newVal;
	}
	
	
	public synchronized void registerElevator(char elevatorId, ElevatorStatus status, int port) {
		elevators.put(elevatorId, port);
		stoppedElevators.add(elevatorId);
		elevatorStatuses.put(elevatorId, status);
		Log.log("Elevator with ID=" + (int) elevatorId + " has been registered on port=" + port, Log.Level.INFO);
		notifyAll();
	}
	
	
	public synchronized void registerFloor(int port, boolean hasMoreEvents) {
		floorPort = port;
		floorHasMoreEvents = hasMoreEvents;
		Log.log("Floor has been registered on port=" + port, Log.Level.INFO);
		notifyAll();
	}
	
	
	public synchronized void updateElevatorStatus(char elevatorId, ElevatorStatus status) {
		elevatorStatuses.put(elevatorId, status);
		Log.log("Elevator with ID=" + (int) elevatorId + " has updated status=" + status.toString(), Log.Level.VERBOSE);
		if (status.getState() == ElevatorState.STOPPED) {
			upElevators.remove(elevatorId);
			downElevators.remove(elevatorId);
			stoppedElevators.add(elevatorId);
			
			notifyAll();
		}
	}
	
	
	
	
	/**
	 * Main Thread, creates Scheduler and starts Floor and Elevator Threads
	 * @param args
	 */
	public static void main(String[] args) {
		// set to INFO for demo.  Use verbose / debug for testing
		Log.setLevel(Log.Level.INFO);
		Thread.currentThread().setName("Scheduler");
		
		Scheduler scheduler = new Scheduler();
		scheduler.run();
	}
	


}
