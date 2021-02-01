package src;

public class Elevator implements Runnable{     //Creates a new elevator
	static int id;    //for additional elevators
	static int CurrentFloor;
	static int nextFloor;
	static int direction = 2;
	
	public Elevator(int floor, int idNum) {
		setCurrentFloor(floor);
		id= idNum;
	}
	
	//Running of the elevator motor
	public void runMotor(int start, int end) {
		int time = 2500;
		if(start == end) {
			System.out.println("Opening Doors...");
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		
		} else { 
			System.out.println("Closing Doors...");
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("motor running...");
			if (direction == 1) {
				for (int i = start; i < end; i++) {
					time = 800;
					try {
						Thread.sleep(time);
					} catch (InterruptedException e) {
						e.printStackTrace();
						System.exit(1);
					}
					
					System.out.println("Elevator traveling at floor " + (i+1));
				}
			} else if (direction == 0) {
				for (int i = start; i > end; i--) {
					time = 800;
					try {
						Thread.sleep(time);
					} catch (InterruptedException e) {
						e.printStackTrace();
						System.exit(1);
					}
					
					System.out.println("Elevator traveling at floor " + (i-1));
				} 
			}
			System.out.println("Opening doors...");
			time = 2500;
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			} 
		}
	}

	public static int getCurrentFloor() {
		return CurrentFloor;
	}

	public static void setCurrentFloor(int floor) {
		CurrentFloor = floor;
	}

	public static int getNextFloor() {
		return nextFloor;
	}

	public static void setNextFloor(int floor) {
		nextFloor = floor;
	}
	
	public void run() {
		Elevator e = new Elevator(1, 1);
	}
}
