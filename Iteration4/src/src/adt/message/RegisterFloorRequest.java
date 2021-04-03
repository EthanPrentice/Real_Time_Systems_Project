package src.adt.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Used to register the floor with the target
 * This allows the Scheduler to know which port the Floor is running on to send it
 *   status updates originating from the Elevators
 */
public class RegisterFloorRequest extends Message {	
	
	private boolean hasMoreEvents;
	
	public RegisterFloorRequest(boolean hasMoreEvents) {
		this(hasMoreEvents, 0);
	}
	
	public RegisterFloorRequest(boolean hasMoreEvents, int srcPort) {
		super(srcPort);
		this.hasMoreEvents = hasMoreEvents;
	}

	@Override
	public String toString() {
		String format = "RegisterFloorRequest(port=%d, hasMoreEvents=%b)";
		return String.format(format, srcPort, hasMoreEvents);
	}
	
	public boolean getHasMoreEvents() {
		return hasMoreEvents;
	}
	
	@Override
	public byte[] toBytes() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		try {
			dos.writeChar(getHeader());	
			dos.write((hasMoreEvents ? 1 : 0));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return bos.toByteArray();
	}

	
	/**
	 * @param bytes The byte array to read in the RegisterFloorRequest from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed RegisterFloorRequest from [bytes].
	 */
	public static RegisterFloorRequest parse(byte[] bytes, int srcPort) {
		boolean hasMoreEvents = bytes[2] == 1;
		return new RegisterFloorRequest(hasMoreEvents, srcPort);
	}
	
}
