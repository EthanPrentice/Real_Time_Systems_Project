package src;

import java.util.*;

/**
 * Written for SYSC3303 - Group 6 - Iteration 1 @ Carleton University
 * @author Ethan Prentice (101070194)
 */
public class Floor {

	private Queue<FloorEvent> eventList = new LinkedList<FloorEvent>();
	
	
	public synchronized void put(FloorEvent[] events) {
		while (!isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
		
		for (int i = 0; i < events.length; ++i) {
			if (events[i] == null) {
				throw new IllegalArgumentException("Cannot have a null event!!");
			}
			eventList.add(events[i]);
		}
		
		notifyAll();
	}
	
	
	public synchronized FloorEvent peek() {
		while (isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
		return eventList.peek();
	}
	
	
	public synchronized FloorEvent pop() {
		while (isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
		
		clear();
		return eventList.remove();
	}
	
	
	public synchronized boolean isEmpty() {
		return eventList.isEmpty();
	}
	
	
	private synchronized void clear() {
		eventList.clear();
		notifyAll();
	}
	
}
