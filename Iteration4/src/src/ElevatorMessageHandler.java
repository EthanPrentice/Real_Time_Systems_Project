package src;

import java.io.IOException;

import src.adt.MessageHandler;
import src.adt.message.FloorRequest;
import src.adt.message.Message;
import src.adt.message.MessageAck;
import src.adt.message.RegisterElevatorRequest;
import src.adt.message.StopRequest;
import src.adt.message.StopResponse;
import src.adt.message.UnregisterElevatorRequest;
import util.Log;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Manages how to handle events that are sent to the Elevator
 * It also acts as a way to send messages to the Scheduler from the Elevator
 */
public class ElevatorMessageHandler extends MessageHandler {
	
	private Elevator elevator;
	
	private boolean stopRequested = false;
	
	
	public ElevatorMessageHandler(Elevator elevator) {
		this.elevator = elevator;
	}
	
	public void init() {
		try {
			// Construct & bind DatagramSocket to any available port
			Log.log("Opened socket on port: " + sock.getLocalPort());
			
			register(true);			
			
		} catch (IOException e) {
			if (!stopRequested) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	
	/**
	 * Repeatedly receives and handles messages until it is stopped
	 */
	@Override
	public void run() {
		init();
		
		while (!stopRequested) {
			try {
				Message received = receive();
				
				// Received from the scheduler to notify the elevator to handle an event
				if (received instanceof FloorRequest) {
					elevator.pushEvent((FloorRequest) received);
				}
				// Received from the scheduler when the elevator will receive no more event, and to shut down
				// Request the elevator to stop when it is safe to do so
				if (received instanceof StopRequest) {
					elevator.requestStop();
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
					Log.log("EXITING", Log.Level.DEBUG);
					System.exit(-1);
				}
			}

		}
		Log.log("EXITING", Log.Level.DEBUG);
	}

	
	/**
	 * QoL method since we will only be sending to the Scheduler from the Elevators
	 * @param msg the message to be sent to the Scheduler
	 */
	public void send(Message msg) {
		super.send(msg, Scheduler.RECEIVE_PORT);
	}
	
	
	private void register(boolean waitForAck) throws IOException {
		// Register with the Scheduler so it knows the port of the elevator with this ID
		RegisterElevatorRequest req = new RegisterElevatorRequest((char) getPort(), elevator.getStatus(), getPort());
		send(req);
		
		// wait for reply so we know we are registered
		if (waitForAck) {
			MessageAck ack = (MessageAck) receive();
		
			if (ack.getHandledSuccessfully()) {
				Log.log("Successfully registered with the Scheduler", Log.Level.INFO);
			}
			else {
				Log.log("Failed to register with the Scheduler", Log.Level.INFO);
				System.exit(-1);
			}
		}
	}
	
	public void register() throws IOException {
		register(false);
	}
	
	private void unregister(boolean waitForAck) throws IOException {
		// Register with the Scheduler so it knows the port of the elevator with this ID
		UnregisterElevatorRequest req = new UnregisterElevatorRequest((char) getPort(), elevator.getRecoverableRequests(), getPort());
		send(req);
		Log.log(req.toString(), Log.Level.INFO);
		
		// wait for reply so we know we are unregistered
		if (waitForAck) {
			MessageAck ack = (MessageAck) receive();
			
			if (ack.getHandledSuccessfully()) {
				Log.log("Successfully unregistered with the Scheduler", Log.Level.INFO);
			}
			else {
				Log.log("Failed to unregister with the Scheduler", Log.Level.INFO);
			}
		}
	}
	
	public void unregister() throws IOException {
		unregister(false);
	}

	
	public void requestStop() {
		if (!stopRequested) {
			stopRequested = true;
			
			// notify Scheduler the Elevator is stopping
			send(new StopResponse());
			
			sock.close();
		}
	}
	
	/** 
	 * forces the handler to stop - should be used after unregistering the elevator
	 * does not send a stop response to the scheduler since the scheduler did not initialize this stop
	 */
	public void forceStop() {
		if (!stopRequested) {
			stopRequested = true;
			sock.close();
		}
	}
}
