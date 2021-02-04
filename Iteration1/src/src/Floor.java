package src;

import java.util.*;
import java.io.*;
import java.lang.Runnable;

import src.adt.FloorEvent;

/**
 * Written for SYSC3303 - Group 6 - Iteration 1 @ Carleton University
 * @author Ethan Prentice (101070194)
 */
public class Floor implements Runnable {
	
	private Scheduler scheduler;	
	
	@Override
	public void run() {
		// hard code file location for now
		File file = new File("res/test_data.txt");
		readFromFile(file);
	}
	
	
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	
	/**
	 * Receives event and prints it
	 * @param event
	 */
	public synchronized void put(FloorEvent event) {
		System.out.println("Floor: Received event. Event: " + event.toString());
	}
	
	
	/**
	 * Reads in FloorEvents from a formatted input file, notifying any waiting threads on each event parsed
	 * @param file : the file to read the events from
	 */
	private void readFromFile(File file) {
		Scanner reader = null;
		try {
			reader = new Scanner(file);
			
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				try {
					FloorEvent event = FloorEvent.parseFromString(line);
					try {
						// sleep to simulate real time events
						Thread.sleep(1000L); // wait 1000ms, TODO: change this timing to be timing in file
					} catch (InterruptedException e) {
						System.err.print(e.getMessage());
					}
					System.out.println("Floor: Sent event to Scheduler. Event: " + event.toString());
					scheduler.put(event);
				} catch (IllegalArgumentException e) {
					System.err.print(e.getMessage());
					e.printStackTrace();
				}
			}
			
		} catch (FileNotFoundException e) {
			System.err.println("Could not read from file.");
			e.printStackTrace();
			
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		
	}
	
}
