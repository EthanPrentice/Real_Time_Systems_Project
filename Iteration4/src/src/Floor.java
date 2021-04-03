package src;

import java.util.*;

import src.adt.ElevatorStatus;
import src.adt.message.FloorRequest;
import src.adt.message.NoMoreEventsNotify;

import java.io.*;
import java.lang.Runnable;
import java.time.Duration;

import util.Config;
import util.Log;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Simulates the real-time generation of Events by reading them from a file and sending them to the scheduler at varying intervals
 */
public class Floor implements Runnable {
	
	// Message handling
	private Thread msgHandlerThread;
	private FloorMessageHandler msgHandler;
	
	boolean hasMoreEvents = true;
	private FloorRequest lastParsed;
	private int lastFloor;
	
	// path to read events from
	private String filePath;
	
	private PriorityQueue<FloorRequest> requestQueue;
	private Comparator<FloorRequest> requestTimeComparator = new Comparator<FloorRequest>() {
        @Override
        public int compare(FloorRequest o1, FloorRequest o2) {
            return o1.getRequestTime().compareTo(o2.getRequestTime());
        }
    };
	
	public Floor() {
		requestQueue = new PriorityQueue<FloorRequest>(requestTimeComparator);
	}
	
	
	public void init() {
		msgHandler = new FloorMessageHandler(this);
		msgHandlerThread = new Thread(msgHandler, "Floor MsgHandler");
		msgHandlerThread.start();
	}
	
	
	@Override
	public void run() {
		init();
		
		// hard code file location for now
		if (filePath == null) {
			File file = new File("res/test_data.txt");
			readFromFile(file);
		}
		else {
			File file = new File(filePath);
			readFromFile(file);
		}
		
		// send events read from file to the Scheduler
		sendRequestsToScheduler();
		
		
		try {
			msgHandlerThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.log("EXITING", Log.Level.DEBUG);
	}
	
	
	/**
	 * Receives floor change and prints it
	 * @param event
	 */
	public synchronized void onElevatorFloorChanged(Elevator e, int floor) {
		Log.log("Floor: Received event from Scheduler. Elevator now on floor: " + floor);
	}
	
	
	/**
	 * Sends the events read into [requestQueue] to the Scheduler using their
	 *   supplied time deltas if applicable
	 */
	private void sendRequestsToScheduler() {
		FloorRequest prevReq = null;
		FloorRequest currReq;
		
		while (!requestQueue.isEmpty()) {
			currReq = requestQueue.remove();
			
			// wait for the amount of time between requests unless Config flag is set to not do so
			if (prevReq != null) {
				Duration duration = Duration.between(prevReq.getRequestTime(), currReq.getRequestTime());
				
				try {
					if (!Config.USE_ZERO_FLOOR_TIME) {
						// sleep for the duration between requests
						Thread.sleep(duration.toMillis());
					}
					else {
						Thread.sleep(50L);
					}
				} catch (InterruptedException e) {
					System.err.print(e.getMessage());
				}
				
				msgHandler.send(currReq);
				Log.log("Floor: Sent event to Scheduler. Event: " + currReq.toString(), Log.Level.INFO);
			}
			else {
				msgHandler.send(currReq);
				Log.log("Floor: Sent event to Scheduler. Event: " + currReq.toString(), Log.Level.INFO);
			}
			
			prevReq = currReq;
		}
		
		hasMoreEvents = false;
		msgHandler.send(new NoMoreEventsNotify());
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
					requestQueue.add(lastParsed);
					
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
			
		} catch (FileNotFoundException e) {
			Log.log("Could not read from file.", Log.Level.INFO);
			e.printStackTrace();
			
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	
	/**
	 * Receive and handle updates to elevator statuses when they reach a floor
	 * @param elevatorId
	 * @param status
	 */
	public void receiveElevatorStatus(char elevatorId, ElevatorStatus status) {
		String format;
		lastFloor = status.getFloor();
		
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
		Log.setLevel(Log.Level.VERBOSE);
		Thread.currentThread().setName("Floor");
		
		Floor floor = new Floor();
		floor.run();
	}
	
}
