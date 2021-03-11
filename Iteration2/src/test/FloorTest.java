package test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import src.Elevator;
import src.Floor;
import src.Scheduler;
import src.adt.ButtonDirection;
import src.adt.message.FloorRequest;

/**
 * Tests the floor's abilities to parse data from a text file, receive elevator data from the scheduler and
 * send event data to the scheduler
 * Written for SYSC3303 - Group 6 - Iteration 2 @ Carleton University
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
		floor = new Floor();
		elevator = new Elevator();
		scheduler = new Scheduler(floor, elevator);
		floor.setScheduler(scheduler);
		elevator.setScheduler(scheduler);
		
		schedulerThread = new Thread(scheduler);
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
		
		FloorRequest testEvent = new FloorRequest(LocalTime.parse("14:06:10.0"), 3, ButtonDirection.DOWN, 1); //The expected last parsed Event object
		FloorRequest getEvent = floor.getLastParsed();
		
		assertEquals(getEvent, testEvent);
	}
	
	/**
	 * Tests the elevator data sent to the floor from the scheduler
	 *
	 */
	@Test
	void testFloorReceive() {
		
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
		

		assertEquals(floor.getLastFloor(), 1); //The expected last floor 1
		
	}

	/**
	 * Tests the data passed from the floor to the scheduler
	 * 
	 */
	 @Test
	 void testFloorSend() {
		 floorThread.start();
				
		 // wait for threads to end
		 while(floorThread.isAlive()) {
			 try {
				 Thread.sleep(100L);
				 } catch(InterruptedException e) {
					 fail("Thread interrupted!");
				 	}
		 }
				
				//Expected last event
				FloorRequest testEvent = new FloorRequest(LocalTime.parse("14:06:10.0"), 3, ButtonDirection.DOWN, 1);
				
				FloorRequest getEvent = scheduler.getLastFloorEvent();
				
				//Test the entire scheduler event queue
				assertEquals(getEvent, testEvent);
	}
}
