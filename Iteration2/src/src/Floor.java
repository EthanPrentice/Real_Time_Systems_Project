package src;

import java.util.*;

import src.adt.ElevatorStatus;
import src.adt.message.FloorRequest;
import src.adt.message.NoMoreEventsNotify;

import java.io.*;
import java.lang.Runnable;

import util.Log;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Simulates the real-time generation of Events by reading them from a file and sending them to the scheduler at varying intervals
 */
public class Floor implements Runnable {
	
	private Thread msgHandlerThread;
	private FloorMessageHandler msgHandler;
	
	boolean hasMoreEvents = true;
	private FloorRequest lastParsed;
	private int lastFloor;
	private String filePath;
	
	/** Used to generate the random intervals between events */
	private Random rand = new Random();
	
	
	public Floor() {
		msgHandler = new FloorMessageHandler(this);
		msgHandlerThread = new Thread(msgHandler, "Floor MsgHandler");
		msgHandlerThread.start();
	}
	
	
	@Override
	public void run() {
		// hard code file location for now
		if(filePath == null) {
			File file = new File("res/test_data.txt");
			readFromFile(file);
		}
		
		else {
			File file = new File(filePath);
			readFromFile(file);
		}
		
		try {
			msgHandlerThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Receives floor change and prints it
	 * @param event
	 */
	public synchronized void onElevatorFloorChanged(Elevator e, int floor) {
		lastFloor = floor;
		Log.log("Floor: Received event from Scheduler. Elevator now on floor: " + floor);
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
					lastParsed = FloorRequest.parseFromString(line);
					
					// sleep to simulate real time events (between 1 and 3 seconds)
					Long sleepMs = 1000L * (rand.nextInt(3) + 1);
					sleepMs = 1000L;
					Thread.sleep(sleepMs); // TODO: change this timing to be timing in file
					
					Log.log("Floor: Sent event to Scheduler. Event: " + lastParsed.toString(), Log.Level.INFO);
					
					// Set hasMoreEvents before the last call to scheduler.putEventFromFloor
					// Prevents elevator deadlock in the case Scheduler calls hasMoreEvents() in-between calls to putEventFromFloor and setting hasMoreEvents to false
					if (!reader.hasNextLine()) {
						hasMoreEvents = false;
					}
					
					msgHandler.send(lastParsed);
					
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
		
		// No more events to read - notify the scheduler.
		msgHandler.send(new NoMoreEventsNotify());
	}
	
	
	public void receiveElevatorStatus(char elevatorId, ElevatorStatus status) {
		String format;
		
		switch (status.getState()) {
		case MOVING_UP:
		case MOVING_DOWN:
			format = "Notified that Elevator %d has arrived at floor %d";
			break;
			
		case STOPPED:
			format = "Notified that Elevator %d has stopped at floor %d";
			break;
			
		case DOORS_OPEN:
			format = "Notified that Elevator %d has opened it's doors on floor %d";
			break;
			
		case DOORS_CLOSED:
			format = "Notified that Elevator %d has closed it's doors on floor %d";
			break;
			
		default:
			format = "Unknown state.  Elevator ID=%d, floor=%d";
			break;
		}
		
		Log.log(String.format(format, (int) elevatorId, status.getFloor()), Log.Level.INFO);
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
	public FloorRequest getLastParsed() {
		return this.lastParsed;
	}
	
	/**
	 * Gets the last event object received from the scheduler
	 * @return
	 */
	public int getLastFloor() {
		return this.lastFloor;
	}
	
	public void setFilePath(String path) {
		this.filePath = path;
	}
	
	
	public static void main(String[] args) {
		Log.setLevel(Log.Level.INFO);
		Thread.currentThread().setName("Floor");
		
		Floor floor = new Floor();
		floor.run();
	}
	
}
