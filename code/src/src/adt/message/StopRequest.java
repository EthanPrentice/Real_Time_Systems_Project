package src.adt.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Used to notify the target it should stop
 * This allows the system to have a clean exit in the case of emergency stops
 *   or the Floor emulation running out of events in the file
 * These are percolated throughout the system to stop each subsystem when it is safe to do so
 */
public class StopRequest extends Message {
	
	public StopRequest() {
		this(0);
	}
	
	public StopRequest(int srcPort) {
		super(srcPort);
	}
	
	@Override
	public String toString() {
		return "StopRequest()";
	}

	@Override
	public byte[] toBytes() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		try {
			dos.writeChar(getHeader());	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return bos.toByteArray();
	}
	
	
	/**
	 * @param bytes The byte array to read in the StopRequest from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed StopRequest from [bytes].
	 */
	public static StopRequest parse(byte[] bytes, int srcPort) {
		return new StopRequest(srcPort);
	}
}
