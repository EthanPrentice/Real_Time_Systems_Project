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
import src.adt.*;

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
		System.out.println("----Same Floor Request----");
		floor.setFilePath("res/same_floor_test.txt");

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
		
		assertEquals(floor.getLastFloor(), 1); //The expected last floor is the floor it started on i.e. floor 1
		assertEquals(elevator.getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed
	}
	
	@Test
	void testLowerFloorRequest() {
		System.out.println("----Lower Floor Request----");
		floor.setFilePath("res/lower_floor_test.txt");
		
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
		
		assertEquals(floor.getLastFloor(), 2);
		assertEquals(elevator.getState(), ElevatorState.STOPPED);
	}
	
	@Test
	void testUpperFloorRequest() {
		System.out.println("----Upper Floor Request----");
		floor.setFilePath("res/upper_floor_test.txt");
		
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
		
		assertEquals(floor.getLastFloor(), 4); //The expected last floor is the floor it started on i.e. floor 1
		assertEquals(elevator.getState(), ElevatorState.STOPPED); //Make sure the elevator is stopped and the doors are closed
	}

}
