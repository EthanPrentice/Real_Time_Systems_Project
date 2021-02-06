/**
 * 
 */
package src;

import java.util.LinkedList;
import java.util.Queue;

import src.adt.*;

/**
 * @author noell
 *
 */
public class Scheduler{
	
	private Queue<Event> eventQueue = new LinkedList<Event>();
	private Floor floor;
	private Elevator elevator;
	
	public Scheduler(Floor floor, Elevator elevator) {
		this.floor = floor;
		this.elevator = elevator;
	}
	
	public synchronized void putEventFromFloor(Event event) {
		eventQueue.add(event);
		System.out.println("Scheduler: Recieved event from Floor. Event: " + event.toString());
		notify();
	}
	
	public synchronized Event getEvent() {
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
		
		System.out.println("Scheduler: Sent event to Elevator. Event: " + eventQueue.peek().toString());
		return eventQueue.remove();
	}
	
	public void sendEventToFloor(Event event) {
		System.out.println("Scheduler: Recieved Event From Elevator. Event: " + event.toString());
		System.out.println("Scheduler: Sent Event to Floor. Event: " + event.toString());
		floor.put(event);
	}

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
