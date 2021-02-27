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

/**
 * 
 * Tests ALL PATHS of the scheduler state machine
 * (Test each state?)
 * @author Nick
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
		scheduler = new Scheduler(floor, elevator);
		floor.setScheduler(scheduler);
		elevator.setScheduler(scheduler);
		
		schedulerThread = new Thread(scheduler);
		floorThread = new Thread(floor);
		elevatorThread = new Thread(elevator);
		
	}
	
	@Test
	void testEventReceived() {
		
	}
	
	@Test
	void test

}
