package src.adt.message;

import java.nio.ByteBuffer;

/**
 * Written for SYSC3303 - Assignment 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * This class should only be used when we need a Message with raw bytes because 
 *   no other message could be parsed and we do not want to throw an error at that point in the program
 *   because it may need to be forwarded elsewhere first.  (ie. assignment spec says always throw error in the server for unparsable messages)
 */
public class ResourceRequest extends Message {
	
	// The type of resource to request from the destination
	// TODO: need to change how we know what resource to request
	//   Possibly use header bytes
	//   Possibly use byte message for more modularity
	//       just a byte[] more effective so we can also encode elevator number
	
	// SO: use 2 bytes for message header
	//     then use 4 bytes for subheader
	//       2 bytes for requested message type
	//       2 bytes for other information, such as elevator number
	private char subHeader;
	private char extraHeader;
	
	public ResourceRequest(char subHeader, char extraHeader) {
		this.subHeader = subHeader;
		this.extraHeader = extraHeader;
	}
	
	public ResourceRequest(char subHeader, char extraHeader, int srcPort) {
		super(srcPort);
		this.subHeader = subHeader;
		this.extraHeader = extraHeader;
	}
	

	@Override
	public byte[] toBytes() {
		return new byte[0];
	}
	
	
	public char getSubHeader() {
		return subHeader;
	}
	
	public char getExtraHeader() {
		return extraHeader;
	}
	
	
	@Override
	public String toString() {
		String format = "ResourceRequest(subHeader=%d, extraHeader=%d)";
		return String.format(format, subHeader, extraHeader);
	}
	
	
	/**
	 * @param bytes The byte array to read in the MessageAck from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed MessageAck from [bytes].
	 */
	public static ResourceRequest parse(byte[] bytes, int srcPort) {		
		ByteBuffer buff = ByteBuffer.allocate(6);
		buff.put(bytes, 2, 4);
		char subHeader = buff.getChar();
		char extraHeader = buff.getChar();
		
		return new ResourceRequest(subHeader, extraHeader);
	}
}
