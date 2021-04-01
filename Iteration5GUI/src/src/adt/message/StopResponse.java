package src.adt.message;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Used to notify the target that the current subsystem stopped safely
 * This is not always sent directly after a StopRequest since it may not be safe
 *   for the subsystem to stop when it receives the initial StopRequest
 */
public class StopResponse extends Message {
	
	public StopResponse() {
		this(0);
	}
	
	public StopResponse(int srcPort) {
		super(srcPort);
	}
	
	@Override
	public String toString() {
		return "StopResponse()";
	}

	@Override
	public byte[] toBytes() {
		return new byte[] {0x0, 0x05};
	}
	
	/**
	 * @param bytes The byte array to read in the StopRequest from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed StopRequest from [bytes].
	 */
	public static StopResponse parse(byte[] bytes, int srcPort) {
		return new StopResponse(srcPort);
	}

}
