/**
 * 
 */
package src;

import java.util.*;
import java.util.LinkedList;
import java.util.Queue;

import src.adt.*;

/**
 * @author noell
 *
 */
public class Scheduler implements Runnable{
	
	private Queue<Event> eventQueue = new LinkedList<Event>();
	private Floor floor;
	
	public Scheduler(Floor floor) {
		this.floor = floor;
	}
	
	public void putEventFromFloor(Event event) {
		eventQueue.add(event);
		System.out.println("Scheduler: Recieved event from Floor. Event: " + event.toString());
		notify();
	}
	
	public synchronized Event getEvent() {
		while(eventQueue.isEmpty()) {
			wait();
		}
		System.out.println("Scheduler: Sent event to Elevator. Event: " + eventQueue.peek().toString());
		return eventQueue.remove();
	}
	
	public void sendEventToFloor(Event event) {
		System.out.println("Scheduler: Recieved Event From Elevator. Event: " + event.toString());
		floor.put(event);
	}

	@Override
	public void run() {
	}

}
