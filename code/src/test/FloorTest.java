package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import src.Elevator;
import src.Floor;
import src.Scheduler;
import src.adt.message.FloorRequest;
import util.Config;
import util.Log;

/**
 * Tests the floor's abilities to parse data from a text file, receive elevator data from the scheduler and
 * send event data to the scheduler. Implements integration testing
 * to show all components of the system functioning together, implements acceptance testing to show proper system
 * functioning.
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Nicholas Milani 101075096
 *
 */
class FloorTest {

	private Floor floor;
	private Elevator elevator;
	private Scheduler scheduler;
	private Thread floorThread;
	private Thread elevatorThread;
	private Thread schedulerThread;
	
	@BeforeEach
	void setup() {
		Log.setLevel(Log.Level.VERBOSE);
		
		Config.USE_ZERO_FLOOR_TIME = true;
		Config.CLOSE_UI_ON_FINISH = true;
		Config.EXPORT_MEASUREMENTS = false;
		
		scheduler = new Scheduler();
		elevator = new Elevator();
		floor = new Floor();
		floor.setFilePath("test_data_noerror.txt");
		
		schedulerThread = new Thread(scheduler, "Scheduler");
		floorThread = new Thread(floor, "Floor");
		elevatorThread = new Thread(elevator, "Elevator");
	}
	
	
	@AfterEach
	void cleanup() {
		Log.setLevel(Log.Level.VERBOSE);
		
		scheduler.requestStop();
		elevator.forceStop();
	}
	
	
	/**
	 * Tests the Floor's ability to read in event data and parse it
	 */
	@Test
	void testFloorParsing() {
		//Sleep to give sockets time to close
		try {
			Thread.sleep(100L);
		} catch(InterruptedException e) {
			fail("Thread interrupted!");
		}
		System.out.println("----Floor Parsing Test----");
		floor.setFilePath("res/test_data_noerror.txt");
		
		//Run all components of the system for integration testing
		schedulerThread.start();
		scheduler.waitUntilCanRegister();
		elevatorThread.start();
		floorThread.start();
		
		// wait for threads to end
		joinThreads();
		
		FloorRequest testEvent = FloorRequest.parseFromString("00:00:27.0 3 UP 7 0"); //The expected last parsed Event object
		FloorRequest getEvent = floor.getLastParsed();
		
		assertEquals(getEvent, testEvent);
	}
	
	/**
	 * Tests the elevator data sent to the floor from the scheduler
	 *
	 */
	@Test
	void testFloorReceive() {
		//Sleep to give sockets time to close
		try {
			Thread.sleep(100L);
		} catch(InterruptedException e) {
			fail("Thread interrupted!");
		}
		System.out.println("----Floor Receive Request----");
		floor.setFilePath("res/test_data_noerror.txt");
		
		//Run all components of the system for integration testing
		schedulerThread.start();
		scheduler.waitUntilCanRegister();
		elevatorThread.start();
		floorThread.start();
		
		
		// wait for threads to end
		joinThreads();
		

		assertEquals(floor.getLastFloor(), 8); //The expected last floor 5
		
	}

	/**
	 * Tests the data passed from the floor to the scheduler
	 * 
	 */
	 @Test
	 void testFloorSend() {
			//Sleep to give sockets time to close
			try {
				Thread.sleep(100L);
			} catch(InterruptedException e) {
				fail("Thread interrupted!");
			}
		 System.out.println("----Floor Send Test----");
		 floor.setFilePath("res/test_data_noerror.txt");
		 
		//Run all components of the system for integration testing
		 schedulerThread.start();
		 scheduler.waitUntilCanRegister();
		 elevatorThread.start();
		 floorThread.start();
				
		 // wait for threads to end
		 joinThreads();
				
		 //Expected last event
		 FloorRequest testEvent = FloorRequest.parseFromString("00:00:27.0 3 UP 7 0");
				
		 FloorRequest getEvent = scheduler.getLastFloorEvent();
				
		 //Test the entire scheduler event queue
		 assertEquals(getEvent, testEvent);
	}
	 
	 
	private void joinThreads() {
		 try {
			schedulerThread.join();
			floorThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
