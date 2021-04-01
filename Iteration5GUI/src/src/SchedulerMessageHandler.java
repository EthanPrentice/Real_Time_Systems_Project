package src;

import java.io.IOException;

import src.adt.MessageHandler;
import src.adt.message.ElevStatusNotify;
import src.adt.message.FloorRequest;
import src.adt.message.Message;
import src.adt.message.MessageAck;
import src.adt.message.NoMoreEventsNotify;
import src.adt.message.RegisterElevatorRequest;
import src.adt.message.RegisterFloorRequest;
import src.adt.message.StopRequest;
import src.adt.message.StopResponse;
import src.adt.message.UnregisterElevatorRequest;
import util.Log;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Manages how to handle events that are sent to the Scheduler
 * It also acts as a way to send messages to the Scheduler's clients. (Elevators & Floor)
 */
public class SchedulerMessageHandler extends MessageHandler {
	
	// Number of elevators currently registered with the Scheduler
	private int activeElevators = 0;
	
	private Scheduler scheduler;
	
	private boolean stopRequested = false;
	
	public SchedulerMessageHandler (Scheduler scheduler) {
		super(Scheduler.RECEIVE_PORT);
		this.scheduler = scheduler;
		
		Log.log("Scheduler ready to register clients.", Log.Level.INFO);
		scheduler.setCanReceiveMessages(true);
	}
	
	
	/**
	 * Repeatedly receives and handles messages until a stop is requested and there are no more
	 *   active elevators that could send messages to the Scheduler
	 */
	@Override
	public void run() {
		while (!stopRequested || activeElevators > 0) {
			try {
				Message received = receive();
				
				// sent from elevator to register port with scheduler
				if (received instanceof RegisterElevatorRequest) {
					RegisterElevatorRequest req = (RegisterElevatorRequest) received;
					scheduler.registerElevator(req.getElevatorId(), req.getStatus(), req.getSrcPort());
					++activeElevators;
				}
				// sent from elevator to unregister port with scheduler
				else if (received instanceof UnregisterElevatorRequest) {
					UnregisterElevatorRequest req = (UnregisterElevatorRequest) received;
					scheduler.unregisterElevator(req.getElevatorId(), req.getRecoverableRequests(), req.getSrcPort());
					--activeElevators;
				}
				// sent on state changes & floor changes by the elevators to the scheduler
				else if (received instanceof ElevStatusNotify) {
					ElevStatusNotify req = (ElevStatusNotify) received;
					scheduler.updateElevatorStatus(req.getElevatorId(), req.getStatus());
					
					// send the status to the Floor (must be registered first)
					int floorPort = scheduler.getFloorPort();
					if (floorPort != 0) {
						send(req, floorPort);
					}
				}
				// sent from the Elevators or Floor to notify the Scheduler they have exited
				else if (received instanceof StopResponse) {
					int floorPort = scheduler.getFloorPort();
					
					// Elevator was stopped
					if (received.getSrcPort() != floorPort) {
						--activeElevators;
					}
				}
				// sent from the Floor to notify the Scheduler of an event that must be scheduled to an Elevator
				else if (received instanceof FloorRequest) {
					scheduler.putEventFromFloor((FloorRequest) received);
				}
				// sent from the Floor when it knows it has no more events and the Scheduler should be aware
				//   so it can exit if needed
				else if (received instanceof NoMoreEventsNotify) {
					scheduler.setFloorHasMoreEvents(false);
				}
				// sent from the Floor so the Scheduler knows which port the Floor is on
				else if (received instanceof RegisterFloorRequest) {
					RegisterFloorRequest req = (RegisterFloorRequest) received;
					scheduler.registerFloor(req.getSrcPort(), req.getHasMoreEvents());
				}
				else if (received instanceof MessageAck) {
					continue;
				}
				// unrecognized message, send MessageAck failure
				else {
					send(new MessageAck(false), received.getSrcPort());
					continue;
				}
				
				// message handled correctly, send MessageAck success
				send(new MessageAck(true), received.getSrcPort());
				
			} catch (IOException e) {
				if (!stopRequested) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}
		
		// All elevators have exited.  This means we can shutdown the Floor as well
		if (!sock.isClosed()) {
			requestFloorStop();
			sock.close();
		}
		
		Log.log("EXITING", Log.Level.DEBUG);
	}
	
	public void requestStop() {
		stopRequested = true;
		if (activeElevators <= 0) {
			// All elevators have exited.  This means we can shutdown the Floor as well
			requestFloorStop();
			
			sock.close();
		}
	}
	
	/**
	 * Requests that the Floor stop
	 * This is called when the Scheduler knows it will not be sending any more info to the Floor
	 */
	private void requestFloorStop() {
		int floorPort = scheduler.getFloorPort();
		if (floorPort != 0) {
			send(new StopRequest(), floorPort);
			// wait for response to know that it shutdown correctly
			try {
				receive();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
