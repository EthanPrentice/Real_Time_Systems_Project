package src.adt.message;

import java.nio.charset.StandardCharsets;

/**
 * Written for SYSC3303 - Assignment 2 @ Carleton University
 * Modified for SYSC3303 - Group 6 - Iteration 3
 * @author Ethan Prentice (101070194)
 * 
 * This class should only be used when we need a Message with raw bytes because 
 *   no other message could be parsed and we do not want to throw an error at that point in the program
 *   because it may need to be forwarded elsewhere first.  (ie. assignment spec says always throw error in the server for unparsable messages)
 */
public class UnparsableMessage extends Message {
	
	private byte[] msgContent;
	
	
	private UnparsableMessage(byte[] bytes) {
		this(bytes, 0);
	}
	
	private UnparsableMessage(byte[] bytes, int srcPort) {
		super(srcPort);
		msgContent = bytes;
	}

	
	@Override
	public String toString() {
		String format = "UnparsableMessage(byte_string=%s)";
		return String.format(format, new String(msgContent, StandardCharsets.US_ASCII));
	}
	
	
	/**
	 * @return The UnparsableMessage converted to the expected schema as a byte array
	 */
	@Override
	public byte[] toBytes() {
		return msgContent;
	}
	
	
	/**
	 * @param bytes The byte array to read in the UnparsableMessage from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return An UnparsableMessage containing [bytes]
	 */
	public static UnparsableMessage parse(byte[] bytes, int srcPort) throws IllegalArgumentException {
		return new UnparsableMessage(bytes, srcPort);
	}
	
}
