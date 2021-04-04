package src;

import src.adt.ElevatorStatus;
import util.Config;

/**
 * Written for SYSC3303 - Group 6 - Iteration 4 @ Carleton University
 * Manages error timers for the Elevator, handling any inferred errors when the timer is triggered
 * 
 * @author Ethan Prentice (101070194)
 */
public class ElevatorErrorHandler implements Runnable {
	
	private long currTimerLength = 0;
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
				while (currTimerLength == 0 || !timerTripped()) {
					if (currTimerLength == 0) {
						wait(currTimerLength);
					}
					else {
						// We must include this in-case the thread is woken up unexpectedly.
						// ie. if we wait for currTimerLength - 1ms and unexpectedly get woken up, we would 
						//   wait for another currTimerLength ms before checking if the timer was tripped
						//   so we would not check if the timer was tripped for nearly twice the timer length
						wait(currTimerLength - (System.currentTimeMillis() - timerStartedMs));
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
	
	
	/**
	 * Depending on the last state of the elevator, handle the inferred error accordingly
	 */
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
	
	
	/**
	 * Updates the most recent status of the Elevator and clears / starts the relevant timers
	 * @param status
	 */
	public synchronized void updateStatus(ElevatorStatus status) {
		lastStatus = status;
		
		switch (lastStatus.getState()) {
		case DOORS_OPEN:
		case DOORS_CLOSED:
			// use shorter timer if config flag is set for zero time
			currTimerLength = Config.USE_ZERO_FLOOR_TIME ? 2000L : Config.DOOR_ERR_TIMER_MS;
			timerStartedMs = System.currentTimeMillis();
			break;
			
		case MOVING_UP:
		case MOVING_DOWN:
			// use shorter timer if config flag is set for zero time
			currTimerLength = Config.USE_ZERO_FLOOR_TIME ? 2000L : Config.FLOOR_ERR_TIMER_MS;
			timerStartedMs = System.currentTimeMillis();
			break;
			
		default: // clear timer
			currTimerLength = 0;
			break;
		}
		
		
		notifyAll();
	}
	
	
	/**
	 * @return whether the time elapsed has been enough to trigger the timer
	 */
	private boolean timerTripped() {
		return System.currentTimeMillis() - timerStartedMs >= currTimerLength; 
	}
	
	
	/**
	 * Stops the error handler thread immediately or after handling any current errors
	 */
	public synchronized void requestStop() {
		stopRequested = true;
		notifyAll();
	}

}
