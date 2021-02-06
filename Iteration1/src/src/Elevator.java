package src;

import src.adt.*;
import java.lang.Thread;
/**
 * Written for SYSC3303 - Group 6 - Iteration 1 @ Carleton University
 * @author Nikhil Kharbanda
 */

public class Elevator implements Runnable{     //Creates a new elevator
	
	private Scheduler sch;

	/** 
	 * Constructor 
	 * @param sch Scheduler object
	 */
	public Elevator(Scheduler sch) {
		this.sch = sch;
	}
	
	/** 
	 * Gets event from the Scheduler and then sends event back to the Scheduler
	 */
	public void run() {
		boolean running = true;
		while(running) {
			
			Event progress = sch.getEvent();
			
			System.out.println("Elevator: Received " + progress.toString() + " from scheduler...");
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
				
			}
			
			sch.sendEventToFloor(progress);
			System.out.println("Elevator: Sending " + progress.toString() + " to scheduler...");
			
		
		}
	}
}