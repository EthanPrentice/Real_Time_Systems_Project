package src;

import java.io.IOException;
import java.net.DatagramSocket;

import src.adt.MessageHandler;
import src.adt.message.FloorRequest;
import src.adt.message.Message;
import src.adt.message.MessageAck;
import src.adt.message.RegisterElevatorRequest;
import src.adt.message.StopRequest;
import src.adt.message.StopResponse;
import util.Log;

public class ElevatorMessageHandler extends MessageHandler implements Runnable {
	
	private Elevator elevator;
	
	private boolean stopRequested = false;
	
	
	public ElevatorMessageHandler(Elevator elevator) {
		this.elevator = elevator;
		
		try {
			// Construct & bind DatagramSocket to any available port
			sock = new DatagramSocket();
			Log.log("Opened socket on port: " + sock.getLocalPort());
			
			// Register with the Scheduler so it knows the port of the elevator with this ID
			RegisterElevatorRequest req = new RegisterElevatorRequest((char) getPort(), elevator.getStatus(), getPort());
			send(req);
			// wait for reply so we know we are registered
			MessageAck ack = (MessageAck) receive();
			
			if (ack.getHandledSuccessfully()) {
				Log.log("Successfully registered with the Scheduler", Log.Level.INFO);
			}
			else {
				Log.log("Failed to register with the Scheduler", Log.Level.INFO);
			}
			
			
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
					System.exit(-1);
				}
			}

		}
		
	}

	
	/**
	 * QoL method since we will only be sending to the Scheduler from the Elevators
	 * @param msg the message to be sent to the Scheduler
	 */
	public void send(Message msg) {
		super.send(msg, Scheduler.RECEIVE_PORT);
	}

	
	public void requestStop() {
		if (!stopRequested) {
			stopRequested = true;
			
			// notify Scheduler the Elevator is stopping
			send(new StopResponse());
			
			sock.close();
		}
	}
}
