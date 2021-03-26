package src.adt.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Used to unregister the elevator with the target
 * This allows the Scheduler to know the Elevator has encountered an error
 */
public class UnregisterElevatorRequest extends Message {

	private char elevatorId;
	
	public UnregisterElevatorRequest(char elevatorId) {
		this(elevatorId, 0);
	}
	
	public UnregisterElevatorRequest(char elevatorId, int srcPort) {
		super(srcPort);
		this.elevatorId = elevatorId;
	}
	
	
	@Override
	public String toString() {
		String format = "UnregisterElevatorRequest(elevatorId=%d, port=%d)";
		return String.format(format, (int) elevatorId, srcPort);
	}
	
	public char getElevatorId() {
		return elevatorId;
	}
	

	@Override
	public byte[] toBytes() {
		ByteArrayOutputStream inStream = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(inStream);
		try {
			dos.write(new byte[]{0, 8});
			dos.writeChar(elevatorId);
			
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
	 * @return A parsed UnregisterElevatorRequest from [bytes].
	 */
	public static UnregisterElevatorRequest parse(byte[] bytes, int srcPort) {		
		ByteBuffer buff = ByteBuffer.wrap(bytes, 2, bytes.length - 2);
		char elevatorId = buff.getChar();
		return new UnregisterElevatorRequest(elevatorId, srcPort);
	}


}
