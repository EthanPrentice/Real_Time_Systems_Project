package src.adt.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Used to register the elevator with the target and give it it's initial state
 * This allows the Scheduler to know which ports the Elevators are running on and where
 *   they have started
 */
public class CompletedFloorRequest extends Message {
	
	private char elevatorId;
	private FloorRequest req;
	
	public CompletedFloorRequest(char elevatorId, FloorRequest req) {
		this(elevatorId, req, 0);
	}
	
	public CompletedFloorRequest(char elevatorId, FloorRequest req, int srcPort) {
		super(srcPort);
		this.elevatorId = elevatorId;
		this.req = req;
	}
	
	
	@Override
	public String toString() {
		String format = "CompletedFloorRequest(elevatorId=%d, req=%s)";
		return String.format(format, (int) elevatorId, req.toString());
	}
	
	public FloorRequest getFloorRequest() {
		return req;
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
			
			dos.write(req.toBytes());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return inStream.toByteArray();
	}
	
	
	/**
	 * @param bytes The byte array to read in the RegisterElevatorRequest from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed RegisterElevatorRequest from [bytes].
	 */
	public static CompletedFloorRequest parse(byte[] bytes, int srcPort) {
		ByteBuffer buff = ByteBuffer.wrap(bytes);
		buff.getChar(); // header
		
		char elevatorId = buff.getChar();
		
		byte[] floorReqBytes = new byte[buff.remaining()];
		buff.get(floorReqBytes);
		
		FloorRequest req = FloorRequest.parse(floorReqBytes, 0);
		
		return new CompletedFloorRequest(elevatorId, req, srcPort);
	}
	
}
