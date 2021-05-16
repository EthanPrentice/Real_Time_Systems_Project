package src.adt.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import src.adt.ElevatorState;
import src.adt.ElevatorStatus;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Used to register the elevator with the target and give it it's initial state
 * This allows the Scheduler to know which ports the Elevators are running on and where
 *   they have started
 */
public class RegisterElevatorRequest extends Message {
	
	private char elevatorId;
	private ElevatorStatus status;
	
	public RegisterElevatorRequest(char elevatorId, ElevatorStatus status) {
		this(elevatorId, status, 0);
	}
	
	public RegisterElevatorRequest(char elevatorId, ElevatorStatus status, int srcPort) {
		super(srcPort);
		this.elevatorId = elevatorId;
		this.status = status;
	}
	
	
	@Override
	public String toString() {
		String format = "RegisterElevatorRequest(elevatorId=%d, port=%d, status=%s)";
		return String.format(format, (int) elevatorId, srcPort, status.toString());
	}
	
	public ElevatorStatus getStatus() {
		return status;
	}
	
	public char getElevatorId() {
		return elevatorId;
	}
	

	@Override
	public byte[] toBytes() {
		ByteArrayOutputStream inStream = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(inStream);
		try {
			dos.writeChar(getHeader());
			dos.writeChar(elevatorId);
			
			dos.writeInt(status.getFloor());
			dos.writeByte(status.getState().stateByte);
			for (int i : status.getFloorOccupancy()) {
				dos.writeInt(i);
			}
			
			return inStream.toByteArray();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	/**
	 * @param bytes The byte array to read in the RegisterElevatorRequest from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed RegisterElevatorRequest from [bytes].
	 */
	public static RegisterElevatorRequest parse(byte[] bytes, int srcPort) {
		ByteBuffer buff = ByteBuffer.wrap(bytes);
		buff.getChar(); // header
		
		char elevatorId = buff.getChar();
		
		int currFloor = buff.getInt();
		ElevatorState state = ElevatorState.fromByte(buff.get());
		
		int[] floorOccupancy = new int[buff.remaining() / 4];
		for (int i = 0; i < floorOccupancy.length; ++i) {
			floorOccupancy[i] = buff.getInt();
		}
		
		ElevatorStatus status = new ElevatorStatus(currFloor, state, floorOccupancy);
		
		return new RegisterElevatorRequest(elevatorId, status, srcPort);
	}
	
}
