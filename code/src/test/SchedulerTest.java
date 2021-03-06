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
import src.adt.ElevatorState;
import src.adt.message.FloorRequest;
import util.Config;
import util.Log;

/**
 * Tests all functions and paths of the scheduler state machine. Implements integration testing
 * to show all components of the system functioning together, implements acceptance testing to show proper system
 * functioning, even in the event of a fault.
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Nicholas Milani 101075096
 *
 */
class SchedulerTest {

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
		elevatorThread = new Thread(elevator, "Elevator 1");
	}
	
	
	@AfterEach
	void cleanup() {
		Log.setLevel(Log.Level.VERBOSE);
		
		scheduler.requestStop();
		elevator.forceStop();
	}
	

	/**
	 * Test the event data received from the floor
	 */
	@Test
	void testFloorEventReceived() {
		//Sleep to give sockets time to close
		try {
			Thread.sleep(100L);
		} catch(InterruptedException e) {
			fail("Thread interrupted!");
		}
		
		System.out.println("----Scheduler Receive Test----");
		floor.setFilePath("res/test_data_noerror.txt");
		
		//Run all components of the system for integration testing
		schedulerThread.start();
		scheduler.waitUntilCanRegister();
		elevatorThread.start();
		floorThread.start();

		// wait for threads to end
		while(floorThread.isAlive() || schedulerThread.isAlive() || elevatorThread.isAlive()) {
			try {
				Thread.sleep(100L);
			} catch(InterruptedException e) {
				fail("Thread interrupted!");
			}
		}
		FloorRequest getEvent = scheduler.getLastFloorEvent();
		FloorRequest floorEvent = floor.getLastParsed();

		assertEquals(getEvent, floorEvent);
	}

	/**
	 * Test the event data sent to one elevator
	 */
	@Test
	void testSendEvent() {
		//Sleep to give sockets time to close
		try {
			Thread.sleep(100L);
		} catch(InterruptedException e) {
			fail("Thread interrupted!");
		}
		
		System.out.println("----Scheduler Send Test----");
		floor.setFilePath("res/test_data_noerror.txt");
		
		//Run all components of the system for integration testing
		schedulerThread.start();
		scheduler.waitUntilCanRegister();
		floorThread.start();
		elevatorThread.start();

		 // wait for threads to end
		while(floorThread.isAlive() || schedulerThread.isAlive() || elevatorThread.isAlive()) {
			try {
				Thread.sleep(100L);
			} catch(InterruptedException e) {
				fail("Thread interrupted!");
			}
		}

		//Expected last event
		FloorRequest testEvent = FloorRequest.parseFromString("00:00:27.0 3 UP 7 0");
		FloorRequest getEvent = elevator.getLastEvent();

		//Test the entire scheduler event queue
		assertEquals(getEvent, testEvent);
	}

	/**
	 * Test scheduling algorithm for multiple elevators
	 */
	@Test
	void testMultipleElevators() {
		//Sleep to give sockets time to close
		try {
			Thread.sleep(100L);
		} catch(InterruptedException e) {
			fail("Thread interrupted!");
		}
		
		System.out.println("----Multiple Elevator Scheduling Test----");
		floor.setFilePath("res/multiple_elevator_test.txt");

		//Create a second elevator
		Elevator elevator2 = new Elevator();
		Thread elevator2Thread = new Thread(elevator2, "Elevator 2");

		//Run all components of the system for integration testing (multiple elevators)
		schedulerThread.start();
		scheduler.waitUntilCanRegister();
		elevatorThread.start();
		elevator2Thread.start();
		floorThread.start();

		// wait for threads to end
		while(floorThread.isAlive() || schedulerThread.isAlive() || elevatorThread.isAlive() || elevator2Thread.isAlive()) {
			try {
				Thread.sleep(100L);
			} catch(InterruptedException e) {
				fail("Thread interrupted!");
			}
		}

		assertTrue((elevator.getStatus().getFloor() == 7 && elevator2.getStatus().getFloor() == 2) || (elevator.getStatus().getFloor() == 2 && elevator2.getStatus().getFloor() == 7));
		assertEquals(elevator.getStatus().getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed
		assertEquals(elevator2.getStatus().getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed
	}

	/**
	 * Acceptance testing to test that the scheduler still completes requests if one elevator encounters a fatal error
	 */
	@Test
	void testFatalError() {
		//Sleep to give sockets time to close
		try {
			Thread.sleep(100L);
		} catch(InterruptedException e) {
			fail("Thread interrupted!");
		}
		
		System.out.println("----Fatal Error Receive----");
		floor.setFilePath("res/fatal_error_test.txt");
		
		Elevator elevator2 = new Elevator();
		Thread elevator2Thread = new Thread(elevator2, "Elevator 2");
		
		//Run all components of the system for integration testing (multiple elevators)
		schedulerThread.start();
		scheduler.waitUntilCanRegister();
		elevatorThread.start();
		elevator2Thread.start();
		floorThread.start();

		// wait for threads to end
		while(floorThread.isAlive() || schedulerThread.isAlive() || elevatorThread.isAlive() || elevator2Thread.isAlive()) {
			try {
				Thread.sleep(100L);
			} catch(InterruptedException e) {
				fail("Thread interrupted!");
			}
		}
		
		//Make sure that at least 1 elevator finishes all requests and ends up on floor 1
		assertTrue(elevator.getStatus().getFloor() == 1 || elevator2.getStatus().getFloor() == 1);
		assertEquals(elevator.getStatus().getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed
		assertEquals(elevator2.getStatus().getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed
		
	}
}


