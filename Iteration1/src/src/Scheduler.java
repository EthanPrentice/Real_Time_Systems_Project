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
	
	public Scheduler(Floor floor) {
		this.floor = floor;
	}
	
	public synchronized void putEventFromFloor(Event event) {
		eventQueue.add(event);
		System.out.println("Scheduler: Recieved event from Floor. Event: " + event.toString());
		notify();
	}
	
	public synchronized Event getEvent() {
		while(eventQueue.isEmpty()) {
			if(!floor.hasMoreEvents()) {
				System.exit(0);
			}
			
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
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
		Scheduler scheduler = new Scheduler(floor);
		floor.setScheduler(scheduler);
		Elevator elevator = new Elevator(scheduler);
		
		Thread floorThread = new Thread(floor);
		Thread elevThread = new Thread(elevator);
		floorThread.start();
		elevThread.start();
	}

}
