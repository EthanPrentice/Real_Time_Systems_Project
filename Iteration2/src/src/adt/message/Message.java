package src.adt.message;

/**
 * Written for SYSC3303 - Assignment 2 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Abstract class that all requests and responses should extend
 * Note in a real system, there would be two more abstract classes, Request and Response
 *   but since we only have one of each I've left them out to reduce clutter
 */
public abstract class Message {
	
	public static final int MAX_BYTES = 100;
	
	// local port that the message is sent from
	protected int srcPort = 0;
	
	
	protected Message() { }
	
	protected Message(int srcPort) {
		if (srcPort < 0) {
			throw new IllegalArgumentException("srcPort must be positive.");
		}
		
		this.srcPort = srcPort;
	}
	
	
	// GETTERS / SETTERS
	public int getSrcPort() {
		return srcPort;
	}
	
	public void setSrcPort(int port) {
		srcPort = port;
	}
	// END OF GETTERS / SETTERS
	
	
	/**
	 * @return the expected byte representation of the Message
	 */
	public abstract byte[] toBytes();
	
	
	/**
	 * Factory method to return Message based on the first two bytes of the message
	 * Defaults to throwing error if no message can be parsed from the bytes
	 * @param bytes The bytes to be parsed into a Message object
	 * @return a Message object representing [bytes]
	 */
	public static Message fromBytes(byte[] bytes, int srcPort) throws IllegalArgumentException {
		return fromBytes(bytes, srcPort, true);
	}
	
	
	/**
	 * Factory method to return Message based on the first two bytes of the message
	 * @param bytes The bytes to be parsed into a Message object
	 * @param throwErr specifies whether an error should be thrown if message is unparsable
	 * @return a Message object representing [bytes]
	 */
	public static Message fromBytes(byte[] bytes, int srcPort, boolean throwErr) throws IllegalArgumentException {
		
		// msgHeader is the first two bytes of the byte array
		// these encode which type of Message the byte array is
		char msgHeader = (char) ((bytes[0] << 8) | bytes[1]);
		try {
			switch (msgHeader) {
			
				
			default: // Invalid header
				throw new IllegalArgumentException(String.format("Illegal message header. (0x%04X)", msgHeader));
			}
			
		} catch (IllegalArgumentException e) {
			// If we can't parse the message, either throw an error or return an UnparsableMessage if throwErr=false
			if (throwErr) {
				throw e;
			} else {
				return UnparsableMessage.parse(bytes, srcPort);
			}
		}
	}

}
