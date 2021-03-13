package src;

import java.io.IOException;

import src.adt.MessageHandler;
import src.adt.message.ElevStatusNotify;
import src.adt.message.Message;
import src.adt.message.MessageAck;
import src.adt.message.RegisterElevatorRequest;
import src.adt.message.RegisterFloorRequest;
import src.adt.message.StopRequest;
import src.adt.message.StopResponse;
import util.Log;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Manages how to handle events that are sent to the Floor
 * It also acts as a way to send messages to the Scheduler from the Floor
 */
public class FloorMessageHandler extends MessageHandler {
	
	private Floor floor;
	
	private boolean stopRequested = false;
	
	
	public FloorMessageHandler(Floor floor) {
		this.floor = floor;
		
		// Register the Floor with the Scheduler so it knows which port to forward requests
		//   to for the Floor to receive them
		send(new RegisterFloorRequest(floor.hasMoreEvents(), getPort()));
	}

	
	/**
	 * Repeatedly receives and handles messages until a stop is requested and there are no more
	 *   events to process
	 */
	@Override
	public void run() {
		while (!stopRequested || floor.hasMoreEvents()) {
			try {
				Message received = receive();
				
				// sent from elevator to register port with scheduler
				if (received instanceof RegisterElevatorRequest) {
					RegisterElevatorRequest req = (RegisterElevatorRequest) received;
					String format = "Notified that Elevator %d has been registered with system and is on floor %d";
					Log.log(String.format(format, (int) req.getElevatorId(), req.getStatus().getFloor()), Log.Level.INFO);
				}
				// sent on state changes & floor changes by the elevators to the scheduler
				else if (received instanceof ElevStatusNotify) {
					ElevStatusNotify req = (ElevStatusNotify) received;					
					floor.receiveElevatorStatus(req.getElevatorId(), req.getStatus());
				}
				// sent by the scheduler to notify the Floor that all Elevators have exited
				// and that the MessageHandler can close since it will not receive any more messages
				else if (received instanceof StopRequest) {
					requestStop();
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
		
		// Notify the Scheduler that the Floor has successfully stopped
		send(new StopResponse());
		
		sock.close();
	}
	
	
	/**
	 * QoL method since we will only be sending to the Scheduler from the Floor
	 * @param msg the message to be sent to the Scheduler
	 */
	public void send(Message msg) {
		send(msg, Scheduler.RECEIVE_PORT);
	}
	
	
	private void requestStop() {
		stopRequested = true;
	}

}
