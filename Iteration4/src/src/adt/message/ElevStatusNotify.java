package src.adt.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import src.adt.ElevatorState;
import src.adt.ElevatorStatus;
import util.Config;


/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Sends the elevator status to the target, including ElevatorStatus and currFloor
 */
public class ElevStatusNotify extends Message {
	
	private char elevatorId;
	private ElevatorStatus status;
	
	public ElevStatusNotify(Character elevatorId, ElevatorStatus status) {
		this.elevatorId = elevatorId;
		this.status = status;
	}
	
	public ElevStatusNotify(Character elevatorId, ElevatorStatus status, int srcPort) {
		super(srcPort);
		this.elevatorId = elevatorId;
		this.status = status;
	}
	
	
	@Override
	public String toString() {
		String format = "ElevStatusNotify(elevatorId=%d, status=%s)";
		return String.format(format, (int) elevatorId, status.toString());
	}
	
	
	public char getElevatorId() {
		return elevatorId;
	}
	
	public ElevatorStatus getStatus() {
		return status;
	}
	

	@Override
	public byte[] toBytes() {
		ByteArrayOutputStream inStream = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(inStream);
		try {
			dos.writeChar(getHeader());
			dos.writeByte(status.getState().stateByte);
			dos.writeChar(elevatorId);
			dos.writeInt(status.getFloor());
			
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
	 * @param bytes The byte array to read in the ElevStatusNotify from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed ElevStatusNotify from [bytes].
	 */
	public static ElevStatusNotify parse(byte[] bytes, int srcPort) {
		ByteBuffer buff = ByteBuffer.wrap(bytes);
		buff.getChar(); // header
		
		
		ElevatorState state = ElevatorState.fromByte(buff.get());
		char elevatorId = buff.getChar();
		int currFloor = buff.getInt();
		
		int[] floorOccupancy = new int[Config.NUM_FLOORS];
		for (int i = 0; i < floorOccupancy.length; ++i) {
			floorOccupancy[i] = buff.getInt();
		}
		
		return new ElevStatusNotify(elevatorId, new ElevatorStatus(currFloor, state, floorOccupancy), srcPort);
	}

}
