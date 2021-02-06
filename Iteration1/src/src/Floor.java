package src;

import java.util.*;
import java.io.*;
import java.lang.Runnable;

import src.adt.Event;

/**
 * Written for SYSC3303 - Group 6 - Iteration 1 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Simulates the real-time generation of Events by reading them from a file and sending them to the scheduler at varying intervals
 */
public class Floor implements Runnable {
	
	private Scheduler scheduler;
	boolean hasMoreEvents = true;
	private Event lastParsed;
	private Event lastReceived;
	
	/** Used to generate the random intervals between events */
	private Random rand = new Random();
	
	
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
		lastReceived = event;
		System.out.println("Floor: Received event from Scheduler. Event: " + event.toString());
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
					lastParsed = Event.parseFromString(line);
					
					// sleep to simulate real time events (between 1 and 3 seconds)
					Long sleepMs = 1000L * (rand.nextInt(3) + 1);
					Thread.sleep(sleepMs); // TODO: change this timing to be timing in file
					
					System.out.println("Floor: Sent event to Scheduler. Event: " + lastParsed.toString());
					
					// Set hasMoreEvents before the last call to scheduler.putEventFromFloor
					// Prevents elevator deadlock in the case Scheduler calls hasMoreEvents() in-between calls to putEventFromFloor and setting hasMoreEvents to false
					if (!reader.hasNextLine()) {
						hasMoreEvents = false;
					}
					
					scheduler.putEventFromFloor(lastParsed);
					
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
		}
		
	}
	
	/**
	 * @return whether the file being read has events left
	 */
	public boolean hasMoreEvents() {
		return hasMoreEvents;
	}
	
	/**
	 * Gets the last event object created by the floor
	 * @return the last event object parsed
	 */
	public Event getLastParsed() {
		return this.lastParsed;
	}
	
	/**
	 * Gets the last event object received from the scheduler
	 * @return
	 */
	public Event getLastReceived() {
		return this.lastReceived;
	}
	
}
