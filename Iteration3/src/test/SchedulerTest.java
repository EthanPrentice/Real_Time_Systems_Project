/**
 *
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import src.Elevator;
import src.Floor;
import src.Scheduler;
import src.adt.ElevatorState;
import src.adt.message.FloorRequest;
import util.Config;

/**
 * Tests all functions and paths of the scheduler state machine
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
		Config.USE_ZERO_FLOOR_TIME = true;
		scheduler = new Scheduler();
		elevator = new Elevator();
		floor = new Floor();

		schedulerThread = new Thread(scheduler, "Scheduler");
		floorThread = new Thread(floor, "Floor");
		elevatorThread = new Thread(elevator, "Elevator 1");
	}

	/**
	 * Test the event data received from the floor
	 */
	@Test
	void testFloorEventReceived() {
		System.out.println("----Scheduler Receive Test----");
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
		System.out.println("----Scheduler Send Test----");
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
		FloorRequest testEvent = FloorRequest.parseFromString("00:00:33.0 2 UP 5");
		FloorRequest getEvent = elevator.getLastEvent();

		//Test the entire scheduler event queue
		assertEquals(getEvent, testEvent);
	}

	/**
	 * Test scheduling algorithm for multiple elevators
	 */
	@Test
	void testMultipleElevators() {
		System.out.println("----Multiple Elevator Scheduling Test----");
		floor.setFilePath("res/multiple_elevator_test.txt");

		//Create a second elevator
		Elevator elevator2 = new Elevator();
		Thread elevator2Thread = new Thread(elevator2, "Elevator 2");

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
}