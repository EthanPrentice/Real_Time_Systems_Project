package src;

import java.util.LinkedList;
import java.util.Queue;

import src.adt.*;

/**
 * Recieves events from Floor and saves in Queue. Reads events from Queue and sends to elevator. Receives Event from Elevator and sends to Floor
 * @author Baillie Noell 101066676 Group 6
 *
 */
public class Scheduler{
	
	private Queue<Event> eventQueue = new LinkedList<Event>();
	private Floor floor;
	private Elevator elevator;
	private Event elevatorEvent;
	
	/**
	 * Constructor, saves references to the floor and elevator
	 * @param floor
	 * @param elevator
	 */
	public Scheduler(Floor floor, Elevator elevator) {
		this.floor = floor;
		this.elevator = elevator;
	}
	
	/**
	 * Receives event from floor and stores in a queue. 
	 * @param event
	 */
	public synchronized void putEventFromFloor(Event event) {
		eventQueue.add(event);
		System.out.println("Scheduler: Recieved event from Floor. Event: " + event.toString());
		//notify that an event has been added
		notify();
	}
	
	/**
	 * Elevators wait until there is an Event to be done
	 * When there is an event Scheduler sends to Elevator
	 * @return Event top event from queue
	 */
	public synchronized Event getEvent() {
		//if there are no events in the queue, wait
		while(eventQueue.isEmpty()) {		
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
		
		// This is the last event in the scheduler queue, and the floor has no more events.
		if(eventQueue.size() == 1 && !floor.hasMoreEvents()) {
			elevator.stop();
		}
		//remove event from queue and send to elevator
		System.out.println("Scheduler: Sent event to Elevator. Event: " + eventQueue.peek().toString());
		return eventQueue.remove();
	}
	
	/**
	 * Scheduler receives Event from Elevator and sends to Floor
	 * @param event
	 */
	public void sendEventToFloor(Event event) {
		elevatorEvent = event;
		System.out.println("Scheduler: Recieved Event From Elevator. Event: " + event.toString());
		System.out.println("Scheduler: Sent Event to Floor. Event: " + event.toString());
		floor.put(event);
	}
	
	/**
	 * Elevator data getter method
	 * @return last event object received from elevator
	 */
	public Event getElevatorEvent() {
		return this.elevatorEvent;
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
		floorThread.start();
		elevThread.start();
	}
	


}
