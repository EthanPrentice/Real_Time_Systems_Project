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
 * 
 * @author Nick
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
		while(floorThread.isAlive());
		
		Event testEvent = new Event(LocalTime.parse("15:06:10.0"), 1, ButtonDirection.UP, 4); //The expected last parsed Event object
		Event getEvent = floor.getLastParsed();
		
		assertEquals(getEvent.getRequestTime(), testEvent.getRequestTime());
		assertEquals(getEvent.getFloorNum(), testEvent.getFloorNum());
		assertEquals(getEvent.getDirection(), testEvent.getDirection());
		assertEquals(getEvent.getCarButton(), testEvent.getCarButton());
	}
	
	/**
	 * Tests the data passed from the floor to the scheduler
	 */
	@Test
	void testSchedulerReceive() {
		
		floorThread.start();
		while(floorThread.isAlive());
		
		//All three expected test events
		Event testEvent1 = new Event(LocalTime.parse("14:05:15.0"), 2, ButtonDirection.UP, 4);
		Event testEvent2 = new Event(LocalTime.parse("14:06:10.0"), 3, ButtonDirection.DOWN, 1);
		Event testEvent3 = new Event(LocalTime.parse("15:06:10.0"), 1, ButtonDirection.UP, 4);
		
		Event getEvent1 = scheduler.getEvent();
		Event getEvent2 = scheduler.getEvent();
		Event getEvent3 = scheduler.getEvent();
		
		//Test the entire scheduler event queue
		assertEquals(getEvent1.getRequestTime(), testEvent1.getRequestTime());
		assertEquals(getEvent1.getFloorNum(), testEvent1.getFloorNum());
		assertEquals(getEvent1.getDirection(), testEvent1.getDirection());
		assertEquals(getEvent1.getCarButton(), testEvent1.getCarButton());

		assertEquals(getEvent2.getRequestTime(), testEvent2.getRequestTime());
		assertEquals(getEvent2.getFloorNum(), testEvent2.getFloorNum());
		assertEquals(getEvent2.getDirection(), testEvent2.getDirection());
		assertEquals(getEvent2.getCarButton(), testEvent2.getCarButton());
		
		assertEquals(getEvent3.getRequestTime(), testEvent3.getRequestTime());
		assertEquals(getEvent3.getFloorNum(), testEvent3.getFloorNum());
		assertEquals(getEvent3.getDirection(), testEvent3.getDirection());
		assertEquals(getEvent3.getCarButton(), testEvent3.getCarButton());
	}
	
	/**
	 * Tests the data received by the elevator from the scheduler
	 */
	@Test
	void testElevatorReceive() {
		
		floorThread.start();
		elevatorThread.start();
		while(floorThread.isAlive() || elevatorThread.isAlive());
		
		Event testEvent = new Event(LocalTime.parse("15:06:10.0"), 1, ButtonDirection.UP, 4); //The expected last parsed Event object
		Event getEvent = elevator.getEvent();
		
		assertEquals(getEvent.getRequestTime(), testEvent.getRequestTime());
		assertEquals(getEvent.getFloorNum(), testEvent.getFloorNum());
		assertEquals(getEvent.getDirection(), testEvent.getDirection());
		assertEquals(getEvent.getCarButton(), testEvent.getCarButton());
	}
	
	/**
	 * Tests the elevator data sent to the floor from the scheduler
	 */
	@Test
	void testFloorReceive() {
		
		floorThread.start();
		elevatorThread.start();
		while(floorThread.isAlive() || elevatorThread.isAlive());
		
		Event testEvent = new Event(LocalTime.parse("15:06:10.0"), 1, ButtonDirection.UP, 4); //The expected last received Event object
		Event getEvent = floor.getLastReceived();
		
		assertEquals(getEvent.getRequestTime(), testEvent.getRequestTime());
		assertEquals(getEvent.getFloorNum(), testEvent.getFloorNum());
		assertEquals(getEvent.getDirection(), testEvent.getDirection());
		assertEquals(getEvent.getCarButton(), testEvent.getCarButton());
		
	}
	
	/*
	 * Tests the data sent to the scheduler by the elevator
	 */
	@Test
	void testElevatorSend() {
		floorThread.start();
		elevatorThread.start();
		while(floorThread.isAlive() || elevatorThread.isAlive());
		
		Event testEvent = new Event(LocalTime.parse("15:06:10.0"), 1, ButtonDirection.UP, 4); //The expected last received Event object
		Event getEvent = scheduler.getElevatorEvent();
		
		assertEquals(getEvent.getRequestTime(), testEvent.getRequestTime());
		assertEquals(getEvent.getFloorNum(), testEvent.getFloorNum());
		assertEquals(getEvent.getDirection(), testEvent.getDirection());
		assertEquals(getEvent.getCarButton(), testEvent.getCarButton());
	}
		
}
