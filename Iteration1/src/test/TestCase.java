/**
 * 
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import src.*;
import src.adt.*;

/**
 * Tests the Events parsed by the Floor as well as the Events passed between each thread
 * 
 * Written for SYSC3303 - Group 6 - Iteration 1 @ Carleton University
 * @author Nicholas Milani 101075096
 *
 */
class TestCase {

	private Floor floor;
	private Elevator elevator;
	private Scheduler scheduler;
	private Thread floorThread;
	private Thread elevatorThread;
	
	@BeforeEach
	void setup() {
		floor = new Floor();
		elevator = new Elevator();
		scheduler = new Scheduler(floor, elevator);
		floor.setScheduler(scheduler);
		elevator.setScheduler(scheduler);
		
		floorThread = new Thread(floor);
		elevatorThread = new Thread(elevator);
		
	}
	
	/**
	 * Tests the Floor's ability to read in event data and parse it
	 */
	@Test
	void testFloorParsing() {
		
		floorThread.start();
		
		// wait for threads to end
		while(floorThread.isAlive()) {
			try {
				Thread.sleep(100L);
			} catch(InterruptedException e) {
				fail("Thread interrupted!");
			}
		}
		
		Event testEvent = new Event(LocalTime.parse("15:06:10.0"), 1, ButtonDirection.UP, 4); //The expected last parsed Event object
		Event getEvent = floor.getLastParsed();
		
		assertEquals(getEvent, testEvent);
	}
	
	/**
	 * Tests the data passed from the floor to the scheduler
	 */
	@Test
	void testSchedulerReceive() {
		
		floorThread.start();
		
		// wait for threads to end
		while(floorThread.isAlive()) {
			try {
				Thread.sleep(100L);
			} catch(InterruptedException e) {
				fail("Thread interrupted!");
			}
		}
		
		//All three expected test events
		Event testEvent1 = new Event(LocalTime.parse("14:05:15.0"), 2, ButtonDirection.UP, 4);
		Event testEvent2 = new Event(LocalTime.parse("14:06:10.0"), 3, ButtonDirection.DOWN, 1);
		Event testEvent3 = new Event(LocalTime.parse("15:06:10.0"), 1, ButtonDirection.UP, 4);
		
		Event getEvent1 = scheduler.getEvent();
		Event getEvent2 = scheduler.getEvent();
		Event getEvent3 = scheduler.getEvent();
		
		//Test the entire scheduler event queue
		assertEquals(getEvent1, testEvent1);
		assertEquals(getEvent2, testEvent2);
		assertEquals(getEvent3, testEvent3);
	}
	
	/**
	 * Tests the data received by the elevator from the scheduler
	 */
	@Test
	void testElevatorReceive() {
		
		floorThread.start();
		elevatorThread.start();
		
		// wait for threads to end
		while(floorThread.isAlive() || elevatorThread.isAlive()) {
			try {
				Thread.sleep(100L);
			} catch(InterruptedException e) {
				fail("Thread interrupted!");
			}
		}
		
		Event testEvent = new Event(LocalTime.parse("15:06:10.0"), 1, ButtonDirection.UP, 4); //The expected last parsed Event object
		Event getEvent = elevator.getEvent();
		
		assertEquals(getEvent, testEvent);
	}
	
	/**
	 * Tests the elevator data sent to the floor from the scheduler
	 */
	@Test
	void testFloorReceive() {
		
		floorThread.start();
		elevatorThread.start();
		
		// wait for threads to end
		while(floorThread.isAlive() || elevatorThread.isAlive()) {
			try {
				Thread.sleep(100L);
			} catch(InterruptedException e) {
				fail("Thread interrupted!");
			}
		}
		
		Event testEvent = new Event(LocalTime.parse("15:06:10.0"), 1, ButtonDirection.UP, 4); //The expected last received Event object
		Event getEvent = floor.getLastReceived();
		
		assertEquals(getEvent, testEvent);
		
	}
	
	/**
	 * Tests the data sent to the scheduler by the elevator
	 */
	@Test
	void testElevatorSend() {
		floorThread.start();
		elevatorThread.start();
		
		// wait for threads to end
		while(floorThread.isAlive() || elevatorThread.isAlive()) {
			try {
				Thread.sleep(100L);
			} catch(InterruptedException e) {
				fail("Thread interrupted!");
			}
		}
		
		Event testEvent = new Event(LocalTime.parse("15:06:10.0"), 1, ButtonDirection.UP, 4); //The expected last received Event object
		Event getEvent = scheduler.getElevatorEvent();
		
		assertEquals(getEvent, testEvent);
	}
		
}
