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
import util.Log;

public class SchedulerMessageHandler extends MessageHandler implements Runnable {
	
	private int activeElevators = 0;
	
	private Scheduler scheduler;
	
	private boolean stopRequested = false;
	
	public SchedulerMessageHandler (Scheduler scheduler) {
		super(Scheduler.RECEIVE_PORT);
		this.scheduler = scheduler;
		
		Log.log("Scheduler ready to register clients.", Log.Level.INFO);
	}
	
	
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
				else if (received instanceof StopResponse) {
					int floorPort = scheduler.getFloorPort();
					
					// Elevator was stopped
					if (received.getSrcPort() != floorPort) {
						--activeElevators;
					}
				}
				else if (received instanceof FloorRequest) {
					scheduler.putEventFromFloor((FloorRequest) received);
				}
				else if (received instanceof NoMoreEventsNotify) {
					scheduler.setFloorHasMoreEvents(false);
				}
				else if (received instanceof RegisterFloorRequest) {
					RegisterFloorRequest req = (RegisterFloorRequest) received;
					scheduler.registerFloor(req.getSrcPort(), req.getHasMoreEvents());
				}
				else if (received instanceof MessageAck) {
					continue;
				}
				else {
					send(new MessageAck(false), received.getSrcPort());
					continue;
				}
				
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

	}
	
	public void requestStop() {
		stopRequested = true;
		if (activeElevators <= 0) {
			// All elevators have exited.  This means we can shutdown the Floor as well
			requestFloorStop();
			
			sock.close();
		}
	}
	
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
