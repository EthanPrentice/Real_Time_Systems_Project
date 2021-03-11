package src.adt.message;

import java.nio.ByteBuffer;

import src.adt.ElevatorState;


/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Sends the elevator status to the target, including ElevatorStatus and currFloor
 */
public class ElevStatusRequest extends Message {
	
	private ElevatorState state;
	private int currFloor;
	
	
	public ElevStatusRequest(ElevatorState state, int currFloor) {
		this.state = state;
		this.currFloor = currFloor;
	}
	
	
	@Override
	public String toString() {
		String format = "ElevStatusRequest(state=%s, currFloor=%d)";
		return String.format(format, state.name().toLowerCase(), currFloor);
	}
	

	@Override
	public byte[] toBytes() {
		// 2 header bytes, 1 state byte, 4 currFloor bytes
		byte[] bytes = new byte[2 + 1 + 4];
		
		bytes[0] = 0;
		bytes[1] = 0x01;
		
		bytes[2] = state.stateByte;
		
		for (int i = 0; i < 4; ++i) {
			bytes[i + 3] = (byte) (currFloor >>> (i * 8));
		}
		
		return bytes;
	}
	
	
	/**
	 * @param bytes The byte array to read in the FloorRequest from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed FloorRequest from [bytes].
	 */
	public static ElevStatusRequest parse(byte[] bytes, int srcPort) {
		ElevatorState e = ElevatorState.fromByte(bytes[2]);
		
		ByteBuffer buff = ByteBuffer.allocate(4);
		for (int i = 0; i < 4; ++i) {
			buff.put(bytes[i + 3]);
		}
		
		int currFloor = buff.getInt();
		
		return new ElevStatusRequest(e, currFloor);
	}
	

}
