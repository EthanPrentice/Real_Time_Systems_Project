package src;

import src.adt.*;
import java.lang.Thread;
/**
 * Written for SYSC3303 - Group 6 - Iteration 1 @ Carleton University
 * @author Nikhil Kharbanda
 */

public class Elevator implements Runnable{     //Creates a new elevator

	private Scheduler scheduler;
	private Event event;

	private boolean isRunning = true;

	/**
	 * Gets event from the Scheduler and then sends event back to the Scheduler
	 */
	public void run() {
		while(isRunning) {
			event = scheduler.getEvent();
			System.out.println("Elevator: Received " + event.toString() + " from scheduler...");

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}

			System.out.println("Elevator: Sending " + event.toString() + " to scheduler...");
			scheduler.sendEventToFloor(event);
		}
	}

	public void setScheduler(Scheduler s) {
		scheduler = s;
	}

	public void stop() {
		isRunning = false;
	}
}
	
	/**
	 * Event getter method
	 * @return the last event object received from the scheduler
	 */
	public Event getEvent() {
		return this.event;
	}
}


//send events to the sch

//receive events from sch

//call like how agent calls chefs
