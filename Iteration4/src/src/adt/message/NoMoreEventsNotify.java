package src.adt.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Sent by the Floor to notify the target that it has run out of events
 * Usually this would not apply in a real-time system, but since the Floor
 *   is emulating real events we know that we will not receive any more at a certain point
 *   
 * In the case of a true real-time system, this would be useful to signal an emergency shut-down
 * 
 */
public class NoMoreEventsNotify extends Message {

	public NoMoreEventsNotify() {
		this(0);
	}
	
	public NoMoreEventsNotify(int srcPort) {
		super(srcPort);
	}
	
	@Override
	public String toString() {
		return "NoMoreEventsNotify()";
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
	 * @param bytes The byte array to read in the NoMoreEventsNotify from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed NoMoreEventsNotify from [bytes].
	 */
	public static NoMoreEventsNotify parse(byte[] bytes, int srcPort) {
		return new NoMoreEventsNotify(srcPort);
	}
}
