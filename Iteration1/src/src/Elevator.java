package src;

import src.adt.*;
import java.lang.Thread;

public class Elevator implements Runnable{     //Creates a new elevator
	
	private Scheduler scheduler;
	
	private boolean isRunning = true;
	
	public void run() {
		while(isRunning) {
			Event progress = scheduler.getEvent();
			System.out.println("Elevator: Received " + progress.toString() + " from scheduler...");
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
			
			System.out.println("Elevator: Sending " + progress.toString() + " to scheduler...");
			scheduler.sendEventToFloor(progress);
		}
	}
	
	public void setScheduler(Scheduler s) {
		scheduler = s;
	}
	
	public void stop() {
		isRunning = false;
	}
}


//send events to the sch

//receive events from sch

//call like how agent calls chefs