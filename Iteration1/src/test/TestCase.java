/**
 * 
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import src.*;
import src.adt.*;

/**
 * 
 * @author Nick
 *
 */
class TestCase {

	/**
	 * Tests the Floor's ability to read in event data, parse it, and send it to the scheduler 
	 */
	@Test
	void testFloorParsing() {
		
		System.out.println("---Floor Parsing Test---");
		
		Floor floor = new Floor();
		Elevator elevator = new Elevator();
		Scheduler scheduler = new Scheduler(floor, elevator);
		floor.setScheduler(scheduler);
		
		Thread floorThread = new Thread(floor);
		
		floorThread.start();
		
		Event event1 = scheduler.getEvent(); //If event queue is empty, calls wait() on main thread
		Event event2 = scheduler.getEvent();
		Event event3= scheduler.getEvent();
				
		//assertTrue(event1.getRequestTime().toString() == "14:05:15");
		assertTrue(event1.getFloorNum() == 2);
		assertTrue(event1.getDirection() == ButtonDirection.UP);
		assertTrue(event1.getCarButton() == 4);
		
		//assertTrue(event2.getRequestTime() == LocalTime.parse("14:06:10.0"));
		assertTrue(event2.getFloorNum() == 3);
		assertTrue(event2.getDirection() == ButtonDirection.DOWN);
		assertTrue(event2.getCarButton() == 1);
		
		//assertTrue(event3.getRequestTime() == LocalTime.parse("15:06:10.0"));
		assertTrue(event3.getFloorNum() == 1);
		assertTrue(event3.getDirection() == ButtonDirection.UP);
		assertTrue(event3.getCarButton() == 4);
		
		//Make sure floor thread doesn't run forever
		assertFalse(floorThread.isAlive());
		
		System.out.println("---Floor parsing test complete---\n");
	}
	
	/**
	 * Tests the system's ability to pass data between threads
	 */
	@Test
	void testDataPassing() {
		
		System.out.println("---System Data Passing Test---");
		
		Floor floor = new Floor();
		Elevator elevator = new Elevator();
		Scheduler scheduler = new Scheduler(floor, elevator);
		floor.setScheduler(scheduler);
		elevator.setScheduler(scheduler);
		
		assertTrue(floor != null);
		assertTrue(elevator != null);
		assertTrue(scheduler != null);
		
		Thread floorThread = new Thread(floor);
		Thread elevatorThread = new Thread(elevator);
		
		floorThread.start();
		elevatorThread.start();
		
		assertTrue(floorThread.isAlive());
		assertTrue(elevatorThread.isAlive());
		
		while(floorThread.isAlive() || elevatorThread.isAlive()); //wait for threads to finish
		
		System.out.println("---System data passing test complete---");
	}
		

}
