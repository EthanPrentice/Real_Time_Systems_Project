package src;

import src.adt.*;
import java.lang.Thread;

public class Elevator implements Runnable{     //Creates a new elevator
	
	private Scheduler sch;

	
	public Elevator(Scheduler sch) {
		this.sch = sch;
	}
	
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


//send events to the sch

//receive events from sch

//call like how agent calls chefs