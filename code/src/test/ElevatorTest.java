/**
 * 
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import src.Elevator;
import src.Floor;
import src.Scheduler;
import src.adt.*;
import util.Config;
import util.Log;

/**
 * Tests all possible paths for the Elevator state machine using varying test data. Implements integration testing
 * to show all components of the system functioning together, implements acceptance testing to show proper system
 * functioning, even in the event of a fault.
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Nicholas Milani 101075096
 *
 */
class ElevatorTest {

	private Floor floor;
	private Elevator elevator;
	private Scheduler scheduler;
	private Thread floorThread;
	private Thread elevatorThread;
	private Thread schedulerThread;
	
	@BeforeEach
	void setup() {
		Log.setLevel(Log.Level.VERBOSE);
		
		Config.CLOSE_UI_ON_FINISH = true;
		Config.USE_ZERO_FLOOR_TIME = true;
		Config.EXPORT_MEASUREMENTS = false;
		
		scheduler = new Scheduler();
		elevator = new Elevator();
		floor = new Floor();
		floor.setFilePath("test_data_noerror.txt");
		
		floorThread = new Thread(floor, "Floor");
		elevatorThread = new Thread(elevator, "Elevator");
		schedulerThread = new Thread(scheduler, "Scheduler");
	}
	
	
	@AfterEach
	void cleanup() {
		Log.setLevel(Log.Level.VERBOSE);
		
		scheduler.requestStop();
		elevator.forceStop();
	}
	
	
	/**
	 * Tests the elevator's final position and state after a request to the same floor
	 */
	@Test
	void testSameFloorRequest() {
		//Sleep to give sockets time to close
		try {
			Thread.sleep(100L);
		} catch(InterruptedException e) {
			fail("Thread interrupted!");
		}
		System.out.println("----Same Floor Request----");
		floor.setFilePath("res/same_floor_test.txt");

		assertEquals(elevator.getStatus().getFloor(), 1); //Make sure the elevator starts on the ground floor
		assertEquals(elevator.getStatus().getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed
		
		//Run all components of the system for integration testing
		schedulerThread.start();
		scheduler.waitUntilCanRegister();
		floorThread.start();
		elevatorThread.start();
		
		joinThreads();
		
		assertEquals(elevator.getStatus().getFloor(), 1); //The expected last floor is the floor it started on i.e. floor 1
		assertEquals(elevator.getStatus().getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed
	}
	
	/**
	 * Tests the elevator's final position and state after a request to a lower floor
	 */
	@Test
	void testLowerFloorRequest() {
		System.out.println("----Lower Floor Request----");
		floor.setFilePath("res/lower_floor_test.txt");
		
		assertEquals(elevator.getStatus().getFloor(), 1); //Make sure the elevator starts on the ground floor
		assertEquals(elevator.getStatus().getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed

		//Run all components of the system for integration testing
		schedulerThread.start();
		scheduler.waitUntilCanRegister();
		floorThread.start();
		elevatorThread.start();
		
		// wait for threads to end
		joinThreads();
		
		assertEquals(elevator.getStatus().getFloor(), 2); //The expected last floor for the elevator to be stopped at
		assertEquals(elevator.getStatus().getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed
	}
	
	/**
	 * Tests the elevator's final position and state after a request to an upper floor
	 */
	@Test
	void testUpperFloorRequest() {
		System.out.println("----Upper Floor Request----");
		floor.setFilePath("res/upper_floor_test.txt");

		assertEquals(elevator.getStatus().getFloor(), 1); //Make sure the elevator starts on the ground floor
		assertEquals(elevator.getStatus().getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed
		
		//Run all components of the system for integration testing
		schedulerThread.start();
		scheduler.waitUntilCanRegister();
		elevatorThread.start();
		floorThread.start();
		
		//wait for threads to end
		joinThreads();		
		
		assertEquals(elevator.getStatus().getFloor(), 4); //The expected last floor for the elevator to be stopped at
		assertEquals(elevator.getStatus().getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed
	}

	/**
	 * Acceptance testing to test recoverable error handling. Here, an elevator encounters a fault,
	 * recovers and then finishes all requests
	 */
	@Test
	void testRecoverableError() {
		System.out.println("----Recoverable Error Test----");
		floor.setFilePath("res/recoverable_error_test.txt");
		
		assertEquals(elevator.getStatus().getFloor(), 1); //Make sure the elevator starts on the ground floor
		assertEquals(elevator.getStatus().getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed
		
		//Run all components of the system for integration testing
		schedulerThread.start();
		scheduler.waitUntilCanRegister();
		elevatorThread.start();
		floorThread.start();
		 
		// wait for threads to end
		joinThreads();
			
		assertEquals(elevator.getStatus().getFloor(), 8); //The expected last floor for the elevator to be stopped at
		assertEquals(elevator.getStatus().getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed
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
