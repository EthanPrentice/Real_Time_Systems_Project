package src;

import java.util.*;
import java.io.*;

import src.adt.FloorEvent;

/**
 * Written for SYSC3303 - Group 6 - Iteration 1 @ Carleton University
 * @author Ethan Prentice (101070194)
 */
public class Floor {

	private Queue<FloorEvent> eventList = new LinkedList<FloorEvent>();
	
	/**
	 * Reads in FloorEvents from a formatted input file, notifying any waiting threads on each event parsed
	 * @param file : the file to read the events from
	 */
	public synchronized void readFromFile(File file) {
		Scanner reader = null;
	    try {
	        reader = new Scanner(file);
	        
	        while (reader.hasNextLine()) {
	        	String line = reader.nextLine();
	        	try {
	        		eventList.add(FloorEvent.parseFromString(line));
	        		notifyAll();
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
	
	/**
	 * Returns the oldest stored FloorEvent without removing it from the queue
	 * 
	 * @return the oldest stored FloorEvent
	 */
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
	
	
	/**
	 * Returns the oldest stored FloorEvent and removes it from the queue
	 * 
	 * @return the oldest stored FloorEvent
	 */
	public synchronized FloorEvent pop() {
		while (isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
		
		return eventList.remove();
	}
	
	
	public synchronized boolean isEmpty() {
		return eventList.isEmpty();
	}
	
}
