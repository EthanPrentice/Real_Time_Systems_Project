package src;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalTime;

import util.Config;
import util.MeasureWriter;

public class Timing {
	
	private Floor floor;
	private Elevator elevator1;
	private Elevator elevator2;
	private Elevator elevator3;
	private Elevator elevator4;
	private Scheduler scheduler;
	private Thread floorThread;
	private Thread elevatorThread1;
	private Thread elevatorThread2;
	private Thread elevatorThread3;
	private Thread elevatorThread4;
	private Thread schedulerThread;
	private MeasureWriter file;
	private String data;
	
	public Timing() {
		
	}
	
	public void run2(String data){
			file = new MeasureWriter();
			this.data = data;
			Config.USE_ZERO_FLOOR_TIME = false;
			scheduler = new Scheduler();
			elevator1 = new Elevator();
			elevator2 = new Elevator();
			floor = new Floor(file);

			schedulerThread = new Thread(scheduler, "Scheduler");
			elevatorThread1 = new Thread(elevator1, "Elevator 1");
			elevatorThread2 = new Thread(elevator2, "Elevator 2");
			floorThread = new Thread(floor, "Floor");
			
			floor.setFilePath(data);
			
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
	
	
	public void run3(String data){
		file = new MeasureWriter();
		this.data = data;
		Config.USE_ZERO_FLOOR_TIME = false;
		scheduler = new Scheduler();
		elevator1 = new Elevator();
		elevator2 = new Elevator();
		elevator3 = new Elevator();
		floor = new Floor(file);

		schedulerThread = new Thread(scheduler, "Scheduler");
		elevatorThread1 = new Thread(elevator1, "Elevator 1");
		elevatorThread2 = new Thread(elevator2, "Elevator 2");
		elevatorThread3 = new Thread(elevator3, "Elevator 3");
		floorThread = new Thread(floor, "Floor");
		
		floor.setFilePath(data);
		
		//Run all components of the system for integration testing
		schedulerThread.start();
		scheduler.waitUntilCanRegister();
		elevatorThread1.start();
		elevatorThread2.start();
		elevatorThread3.start();
		floorThread.start();
		
		// wait for threads to end
		while(floorThread.isAlive() || schedulerThread.isAlive() || elevatorThread1.isAlive() || elevatorThread2.isAlive() || elevatorThread3.isAlive()) {
			try {
				Thread.sleep(100L);
			} catch(InterruptedException e) {
				fail("Thread interrupted!");
			}
		}
	}
	
	public void run4(String data){
		file = new MeasureWriter();
		this.data = data;
		Config.USE_ZERO_FLOOR_TIME = false;
		scheduler = new Scheduler();
		elevator1 = new Elevator();
		elevator2 = new Elevator();
		elevator3 = new Elevator();
		elevator4 = new Elevator();
		floor = new Floor(file);

		schedulerThread = new Thread(scheduler, "Scheduler");
		elevatorThread1 = new Thread(elevator1, "Elevator 1");
		elevatorThread2 = new Thread(elevator2, "Elevator 2");
		elevatorThread3 = new Thread(elevator3, "Elevator 3");
		elevatorThread4 = new Thread(elevator4, "Elevator 4");
		floorThread = new Thread(floor, "Floor");
		
		floor.setFilePath(data);
		
		//Run all components of the system for integration testing
		schedulerThread.start();
		scheduler.waitUntilCanRegister();
		elevatorThread1.start();
		elevatorThread2.start();
		elevatorThread3.start();
		elevatorThread4.start();
		floorThread.start();
		
		// wait for threads to end
		while(floorThread.isAlive() || schedulerThread.isAlive() || elevatorThread1.isAlive() || elevatorThread2.isAlive() || elevatorThread3.isAlive() || elevatorThread4.isAlive()) {
			try {
				Thread.sleep(100L);
			} catch(InterruptedException e) {
				fail("Thread interrupted!");
			}
		}
	}
	public void makeRandom() {
		int b;
		String dir = "";
		int sec = 0;
		for (int i = 0; i < 10; i++) {
			sec = sec + (int)Math.floor(Math.random()*(15-1+1)+1);
			
			int h = sec / 3600;
			int secleft = sec - h * 3600;
			int m = secleft / 60;
			int seconds = secleft - m * 60;
			
			String formattedTime = "";
			
			if (h<10)
				formattedTime += "0";
			formattedTime += h + ":";
			
			if (m < 10)
				formattedTime += "0";
			formattedTime += m + ":";
			
			if (seconds < 10)
				formattedTime += "0";
			formattedTime += seconds;
			
			int a = (int)Math.floor(Math.random()*(22-1+1)+1);
			do {
				b = (int)Math.floor(Math.random()*(22-1+1)+1);
			}while (a == b);
			
			if (a > b) {
				dir = "Down";
			}
			else if (a < b) {
				dir = "Up";
			}
			System.out.println(formattedTime + " " + a + " " + dir + " " + b + " " + 0);
		}
		
	}
	
	public static void main(String[] args) {
		Timing t = new Timing();
		 t.run4("res/test_data_22_floors.txt");
	}

}
