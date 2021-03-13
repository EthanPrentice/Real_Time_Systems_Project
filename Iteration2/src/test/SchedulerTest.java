/**
 * 
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import src.Elevator;
import src.Floor;
import src.Scheduler;
import src.adt.ButtonDirection;
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
		
		//Expected last event
		FloorRequest testEvent = new FloorRequest(LocalTime.parse("14:06:10.0"), 3, ButtonDirection.DOWN, 1);
		
		FloorRequest getEvent = scheduler.getLastFloorEvent();
		
		assertEquals(getEvent, testEvent);
	}
	
	/**
	 * Test the event data sent to one elevator
	 */
	@Test
	void testSendEvent() {
		System.out.println("----Scheduler Send Test----");
		schedulerThread.start();
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
			FloorRequest testEvent = new FloorRequest(LocalTime.parse("14:06:10.0"), 3, ButtonDirection.DOWN, 1);
			
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
		
		//One elevator must end up on floor 7 and one must end up on floor 2
		if(elevator.getFloor() == 7) assertEquals(elevator2.getFloor(), 2);
		else if(elevator.getFloor() == 2) assertEquals(elevator2.getFloor(), 7);
		else fail("Elevator(s) stopped at wrong floor");
		
		assertEquals(elevator.getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed
		assertEquals(elevator2.getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed
	}
	
	
}
