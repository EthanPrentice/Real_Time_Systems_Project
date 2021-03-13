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
import src.adt.message.FloorRequest;

/**
 * Tests the scheduler's ability to send and receive data to and from the elevator and floor systems
 * Written for SYSC3303 - Group 6 - Iteration 2 @ Carleton University
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
		floor = new Floor();
		elevator = new Elevator();
		scheduler = new Scheduler();
		
		schedulerThread = new Thread(scheduler);
		floorThread = new Thread(floor);
		elevatorThread = new Thread(elevator);
		
	}
	
	/**
	 * Test the event data received from the floor
	 */
	@Test
	void testFloorEventReceived() {
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
	 * Test the event data sent to the elevator
	 */
	@Test
	void testSendEvent() {
		
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
	
	
	
	
}
