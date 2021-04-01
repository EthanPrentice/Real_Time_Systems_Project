package src;

import src.adt.ElevatorStatus;
import util.Config;

public class ElevatorErrorHandler implements Runnable {
	
	private static final long DOOR_TIMER = 20_000L;
	private static final long FLOOR_TIMER = 15_000L;
	
	private long currTimerLength = -1;
	private long timerStartedMs = 0;
	
	private boolean stopRequested = false;
	
	private Elevator elevator;
	private ElevatorStatus lastStatus;
	
	
	public ElevatorErrorHandler(Elevator elevator) {
		this.elevator = elevator;
		lastStatus = elevator.getStatus();
	}

	
	@Override
	public synchronized void run() {
		while (!stopRequested) {
			try {
				while (currTimerLength == -1 || !timerTripped()) {
					if (currTimerLength == -1) {
						wait();
					} else {
						wait(currTimerLength);
					}
					
					if (stopRequested) {
						return;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (timerTripped()) {
				handleError();
			}
		}
	}
	
	
	private void handleError() {
		switch (lastStatus.getState()) {
		case DOORS_OPEN:
		case DOORS_CLOSED:
			elevator.handleDoorError();
			break;
			
		case MOVING_UP:
		case MOVING_DOWN:
			elevator.handleUnexpectedStopError();
			break;
			
		default: // do nothing
			break;
		}
	}
	
	
	public synchronized void updateStatus(ElevatorStatus status) {
		lastStatus = status;
		
		switch (lastStatus.getState()) {
		case DOORS_OPEN:
		case DOORS_CLOSED:
			currTimerLength = Config.USE_ZERO_FLOOR_TIME ? 2000L : DOOR_TIMER;
			timerStartedMs = System.currentTimeMillis();
			break;
			
		case MOVING_UP:
		case MOVING_DOWN:
			currTimerLength = Config.USE_ZERO_FLOOR_TIME ? 2000L : FLOOR_TIMER;
			timerStartedMs = System.currentTimeMillis();
			break;
			
		default: // clear timer
			currTimerLength = -1;
			break;
		}
		
		
		notifyAll();
	}
	
	
	private boolean timerTripped() {
		return System.currentTimeMillis() - timerStartedMs >= currTimerLength; 
	}
	
	public synchronized void requestStop() {
		stopRequested = true;
		notifyAll();
	}

}
