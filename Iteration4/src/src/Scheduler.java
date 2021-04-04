package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import src.adt.*;
import src.adt.message.FloorRequest;
import src.adt.message.StopRequest;
import src.ui.UIManager;
import util.Log;

/**
 * Receives events from Floor and saves in Queue. Reads events from Queue and sends to elevator. Receives Event from Elevator and sends to Floor
 * @author Baillie Noell 101066676 Group 6
 * @edited Ethan Prentice (101070194)
 */
public class Scheduler implements Runnable {
	
	public final static int RECEIVE_PORT = 23;
	
	// Message handling
	private Thread msgHandlerThread;
	private SchedulerMessageHandler msgHandler;
	
	// Used to wait for Scheduler to initialize and be ready to receive registration events
	// Useful for testing where we run Scheduler as a thread and must wait to run Elevator and Floor threads until
	//   Scheduler's MessageHandler is initialized
	private boolean canReceiveMessages = false;
	private Object canReceiveMessagesLock = new Object();
	
	// Store information sent from the Floor
	private int floorPort = 0;
	private boolean floorHasMoreEvents = true;
	
	// all elevators IDs -> ports
	private HashMap<Character, Integer> elevators = new HashMap<>();
	
	// sets of elevator IDs going in directions - used for faster scheduling based off of direction
	private HashSet<Character> upElevators = new HashSet<>();
	private HashSet<Character> downElevators = new HashSet<>();
	private HashSet<Character> stoppedElevators = new HashSet<>();
	
	// elevator id -> last reported status - used for storing status updates from Elevators to use in scheduling
	private HashMap<Character, ElevatorStatus> elevatorStatuses = new HashMap<>();
	
	// List of events received from the floor and not yet sent to the elevators
	private ArrayList<FloorRequest> eventList = new ArrayList<FloorRequest>();

	// Used for testing
	private FloorRequest elevatorEvent;
	private FloorRequest lastFloorEvent;
	
	// true if scheduler thread should stop when it is safe to do so
	private boolean stopRequested = false;
	
	private UIManager uiManager;
	
	
	/**
	 * Constructor, saves references to the floor and elevator
	 * @param floor
	 * @param elevator
	 */
	public Scheduler() {
		msgHandler = new SchedulerMessageHandler(this);
		msgHandlerThread = new Thread(msgHandler, "Scheduler MsgHandler");
		msgHandlerThread.start();
		uiManager = new UIManager();
	}
	
	
	@Override
	public void run() {
		while (!stopRequested) {
			
			// send as all events to the elevators that can be sent in their current states
			while (eventList.isEmpty() || !sendEventsToElevators()) {				
				try {
					synchronized(this) {
						wait();
					}
					
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
		
		Log.log("EXITING", Log.Level.DEBUG);
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
			|| (status.getState() == ElevatorState.MOVING_UP && status.getFloor() < floorRequest.getSourceFloor() && floorRequest.getDirection() == ButtonDirection.UP)
			|| (status.getState() == ElevatorState.MOVING_DOWN && status.getFloor() > floorRequest.getSourceFloor() && floorRequest.getDirection() == ButtonDirection.DOWN);
	}
	
	/**
	 * @param floorRequest
	 * @param elevators
	 * @return the elevator in [elevators] that is closest to the srcFloor of [floorRequest]
	 */
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
	
	/**
	 * @param floorRequest
	 * @return the closest elevator that can service [floorRequest] in it's current state, as defined by the canSendEventToElevator method.
	 *         in the case where two elevators are the same distance, return the one that will result in the least occupancy throughout the trip
	 */
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
	private synchronized boolean sendEventsToElevators() {
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
	
	
	/**
	 * Ask all elevators to stop when it is safe to do so
	 */
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
		Log.log("Recieved request from Floor. Event: " + floorRequest.toString());
		
		// notify we have events
		notifyAll();
	}

	
	/**
	 * Scheduler receives Event from Elevator and sends to Floor
	 * @param e: elevator that's floor has changed
	 * @param newFloor: e's new floor
	 */
	public void sendEventToFloor(Elevator e, int newFloor) {
		Log.log("Sent floor changed event to Floor. Elevator now on floor: " + newFloor);
	}
	
	
	/**
	 * Recovers the request from an inoperable elevator to reschedule to a different operating one
	 * @param requests : the requests that have been sent to recover
	 * @param recoveredFromId : the id of the elevator these requests were recovered from
	 */
	public synchronized void recoverRequests(ArrayList<FloorRequest> requests, char recoveredFromId) {
		for (FloorRequest req : requests) {
			// If this is the req that caused an error, clear it
			// Only process errors once
			req.clearError();
			
			eventList.add(req);
			lastFloorEvent = req;
			Log.log("Recovered request: " + req.toString() + " from elevator with id=" + (int) recoveredFromId, Log.Level.INFO);
		}		
		
		// notify we have new requests
		notifyAll();
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
	
	/**
	 * Registers the elevator with the scheduler, storing it's port and initial status
	 * @param elevatorId
	 * @param status
	 * @param port
	 */
	public synchronized void registerElevator(char elevatorId, ElevatorStatus status, int port) {
		uiManager.registerElevator(elevatorId, status);
		elevators.put(elevatorId, port);
		stoppedElevators.add(elevatorId);
		elevatorStatuses.put(elevatorId, status);
		Log.log("Elevator with ID=" + (int) elevatorId + " has been registered on port=" + port, Log.Level.INFO);
		notifyAll();
	}
	
	/**
	 * Unregisters the elevator with the scheduler, storing it's port and initial status and rescheduling any recoverable requests
	 * @param elevatorId
	 * @param recoverableRequests
	 * @param port
	 */
	public synchronized void unregisterElevator(char elevatorId, ArrayList<FloorRequest> recoverableRequests, int port) {
		// remove the elevator from all data structures
		elevators.remove(elevatorId, port);
		stoppedElevators.remove(elevatorId);
		upElevators.remove(elevatorId);
		downElevators.remove(elevatorId);
		elevatorStatuses.remove(elevatorId);
		
		uiManager.unregisterElevator(elevatorId);
		
		Log.log("Elevator with ID=" + (int) elevatorId + " has been unregistered on port=" + port, Log.Level.INFO);
		
		// recover the recoverable requests (ie. elevator has not picked people up from the floors yet)
		//    and reschedule them to other working elevators
		recoverRequests(recoverableRequests, elevatorId);
		
		notifyAll();
	}
	
	/**
	 * Registers the floor with the scheduler, storing it's port and ensuring it has more events or else we exit
	 * @param port
	 * @param hasMoreEvents
	 */
	public synchronized void registerFloor(int port, boolean hasMoreEvents) {
		floorPort = port;
		floorHasMoreEvents = hasMoreEvents;
		Log.log("Floor has been registered on port=" + port, Log.Level.INFO);
		notifyAll();
	}
	
	/**
	 * Updates the cached elevator status of [elevatorId] to [status]
	 * Notifies the Scheduler of this change if the Elevator is stopped so that events may be sent to it if they could not before
	 * @param elevatorId
	 * @param status
	 */
	public synchronized void updateElevatorStatus(char elevatorId, ElevatorStatus status) {
		uiManager.updateElevatorStatus(elevatorId, status);
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
	 * @return whether the MessageHandler has been initialized
	 */
	public boolean canReceiveMessages() {
		return canReceiveMessages;
	}
	
	/**
	 * Sets whether the MessageHandler has been initialized
	 */
	public void setCanReceiveMessages(boolean newVal) {
		canReceiveMessages = newVal;
		if (newVal && canReceiveMessagesLock != null) {
			synchronized(canReceiveMessagesLock) {
				canReceiveMessagesLock.notifyAll();
			}
		}
	}
	
	/**
	 * Waits until the Scheduler's MessageHandler has been initialized and clients can register
	 */
	public void waitUntilCanRegister() {
		if (canReceiveMessages) {
			return;
		}
			
		while (!canReceiveMessages()) {
			try {
				synchronized(canReceiveMessagesLock) {
					canReceiveMessagesLock.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
