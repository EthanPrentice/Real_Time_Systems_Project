package src;

import src.adt.*;
import java.lang.Thread;
/**
 * Written for SYSC3303 - Group 6 - Iteration 1 @ Carleton University
 * Creates and runs the elevator sub-system as a thread. Here, the elevator thread gets and displays an event from scheduler, then sends the event back to scheduler
 * @author Nikhil Kharbanda 101012041
 */


public class Elevator implements Runnable{     

	private Scheduler scheduler;
	private Event event;

	private boolean isRunning = true;

	/**
	 * Runs the thread. Gets event from the Scheduler and then sends event back to the Scheduler
	 */
	public void run() {
		while(isRunning) {         //while elevator is running
			event = scheduler.getEvent();
			System.out.println("Elevator: Recieved Event From Scheduler. Event: " + event.toString());    //gets an event from scheduler and prints event out

			try {
				Thread.sleep(1000);           //sleep for 1 second
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}

			System.out.println("Elevator: Sent Event to Scheduler. Event: " + event.toString());          //sends an event to scheduler and prints event being sent
			scheduler.sendEventToFloor(event);
		}
	}

	/**
	 * Setter method for scheduler
	 * @param s
	 */
	public void setScheduler(Scheduler s) {
		scheduler = s;
	}

	/**
	 * sets the isRunning value to false. Simulates the elevator not running
	 */
	public void stop() {
		isRunning = false;
	}
	
	/**
	 * Event getter method
	 * @return the last event object received from the scheduler
	 */
	public Event getEvent() {
		return this.event;
	}
}


