package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import src.Elevator;
import src.Floor;
import src.Scheduler;
import util.Config;
import util.MeasureWriter;

class TimingTest {
	
	private Floor floor;
	private Elevator elevator1;
	private Elevator elevator2;
	private Scheduler scheduler;
	private Thread floorThread;
	private Thread elevatorThread1;
	private Thread elevatorThread2;
	private Thread schedulerThread;
	private MeasureWriter file;

	@Before
	void setUp() {
		file = new MeasureWriter();
	}
	
	@Test
	void test() {
		for (int i = 0; i < 30; i++) {
			Config.USE_ZERO_FLOOR_TIME = false;
			scheduler = new Scheduler();
			elevator1 = new Elevator();
			elevator2 = new Elevator();
			floor = new Floor(file);

			schedulerThread = new Thread(scheduler, "Scheduler");
			floorThread = new Thread(floor, "Floor");
			elevatorThread1 = new Thread(elevator1, "Elevator 1");
			elevatorThread2 = new Thread(elevator2, "Elevator 2");
			
			//Sleep to give sockets time to close
			try {
				Thread.sleep(100L);
			} catch(InterruptedException e) {
				fail("Thread interrupted!");
			}
			
			floor.setFilePath("res/timing_data.txt");
			
			//Run all components of the system for integration testing
			schedulerThread.start();
			scheduler.waitUntilCanRegister();
			elevatorThread1.start();
			elevatorThread2.start();
			floorThread.start();
			
			// wait for threads to end
			while(floorThread.isAlive() || schedulerThread.isAlive() || elevatorThread1.isAlive() || elevatorThread2.isAlive()) {
				try {
					Thread.sleep(100L);
				} catch(InterruptedException e) {
					fail("Thread interrupted!");
				}
			}
			
		}
	}

}
