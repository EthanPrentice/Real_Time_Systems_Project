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
 * Tests ALL PATHS of the elevator state machine
 * 
 * @author Nick
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
		floor = new Floor();
		elevator = new Elevator();
		scheduler = new Scheduler(floor, elevator);
		floor.setScheduler(scheduler);
		elevator.setScheduler(scheduler);
		
		floorThread = new Thread(floor);
		elevatorThread = new Thread(elevator);
		schedulerThread = new Thread(scheduler);
		
	}
	
	@Test
	void testSameFloorRequest() {
		
	}
	
	@Test
	void testLowerFloorRequest() {
		
	}
	
	@Test
	void testUpperFloorRequest() {
		
	}

}
