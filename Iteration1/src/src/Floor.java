package src;

import java.util.*;
import java.io.*;
import java.lang.Runnable;

import src.adt.Event;

/**
 * Written for SYSC3303 - Group 6 - Iteration 1 @ Carleton University
 * @author Ethan Prentice (101070194)
 */
public class Floor implements Runnable {
	
	private Scheduler scheduler;
	boolean hasMoreEvents = false;
	
	
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
	public synchronized void put(Event event) {
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
			
			if (reader.hasNextLine()) {
				hasMoreEvents = true;
			}
			
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				try {
					Event event = Event.parseFromString(line);
					
					// sleep to simulate real time events
					Thread.sleep(1000L); // wait 1000ms, TODO: change this timing to be timing in file
					
					System.out.println("Floor: Sent event to Scheduler. Event: " + event.toString());
					scheduler.putEventFromFloor(event);
					
				} catch (IllegalArgumentException e) {
					System.err.print(e.getMessage());
					e.printStackTrace();
				} catch (InterruptedException e) {
					System.err.print(e.getMessage());
				}
			}
			
		} catch (FileNotFoundException e) {
			System.err.println("Could not read from file.");
			e.printStackTrace();
			
		} finally {
			if (reader != null) {
				reader.close();
			}
			hasMoreEvents = false;
		}
		
	}
	
	/**
	 * @return whether the file being read has events left
	 */
	public boolean hasMoreEvents() {
		return hasMoreEvents;
	}
	
}
