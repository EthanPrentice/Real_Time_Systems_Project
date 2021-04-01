package src.adt.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Used to unregister the elevator with the target
 * This allows the Scheduler to know the Elevator has encountered an error
 */
public class UnregisterElevatorRequest extends Message {

	private char elevatorId;
	private ArrayList<FloorRequest> recoverableReqs;
	
	public UnregisterElevatorRequest(char elevatorId, ArrayList<FloorRequest> recoverableReqs) {
		this(elevatorId, recoverableReqs, 0);
	}
	
	public UnregisterElevatorRequest(char elevatorId, ArrayList<FloorRequest> recoverableReqs, int srcPort) {
		super(srcPort);
		this.elevatorId = elevatorId;
		this.recoverableReqs = recoverableReqs;
	}
	
	
	@Override
	public String toString() {
		String format = "UnregisterElevatorRequest(elevatorId=%d, port=%d)";
		return String.format(format, (int) elevatorId, srcPort);
	}
	
	public char getElevatorId() {
		return elevatorId;
	}
	
	public ArrayList<FloorRequest> getRecoverableRequests() {
		return recoverableReqs;
	}
	

	@Override
	public byte[] toBytes() {
		ByteArrayOutputStream inStream = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(inStream);
		try {
			dos.write(new byte[]{0, 8});
			dos.writeChar(elevatorId);
			
			for (int i = 0; i < recoverableReqs.size(); ++i) {
				dos.write(recoverableReqs.get(i).toBytes());
				
				if (i != recoverableReqs.size() - 1) {
					dos.write((byte) 0);
				}
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
	 * @return A parsed UnregisterElevatorRequest from [bytes].
	 */
	public static UnregisterElevatorRequest parse(byte[] bytes, int srcPort) {
		ByteBuffer buff = ByteBuffer.wrap(bytes, 2, bytes.length - 2);
		char elevatorId = buff.getChar();

		ArrayList<FloorRequest> reqs = new ArrayList<>();
		while (buff.hasRemaining()) {
			ByteBuffer tmpBuff = buff.slice();
			
			if (tmpBuff.getChar() != 0x0001) { // header
				break;
			}
			
			// get floor req length
			while (tmpBuff.get() != 0);
			int reqLength = tmpBuff.position();
			
			// read floor req
			byte[] reqBytes = new byte[reqLength - 1];
			buff.get(reqBytes);
			
			// read separator
			if (buff.hasRemaining()) {
				buff.get();
			}
			
			FloorRequest req = FloorRequest.parse(reqBytes, 0);
			reqs.add(req);
		}
		
		
		return new UnregisterElevatorRequest(elevatorId, reqs, srcPort);
	}


}
