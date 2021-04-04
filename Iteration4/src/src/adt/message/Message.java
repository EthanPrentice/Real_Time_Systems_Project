package src.adt.message;

import java.nio.ByteBuffer;
import java.util.HashMap;

import util.Log;

/**
 * Written for SYSC3303 - Assignment 2 @ Carleton University
 * Modified for SYSC3303 - Group 6 - Iteration 3
 * @author Ethan Prentice (101070194)
 * 
 * Abstract class that all requests and responses should extend
 */
public abstract class Message {
	
	// stores the parsers for the message type(s) represented by header (key)
	private static HashMap<Character, MessageParser> parserMap = new HashMap<>();
	private static HashMap<Class<? extends Message>, Character> headerMap = new HashMap<>();
	
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
	
	protected char getHeader() {
		synchronized(Message.class) {
			return getHeader(getClass());
		}
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
		ByteBuffer buff = ByteBuffer.wrap(bytes);
		char msgHeader = buff.getChar();
		
		MessageParser parser;
		parser = parserMap.get(msgHeader);
		if (parser != null) {
			try {
				return parser.parse(bytes, srcPort);
			} catch (Exception e) {
				if (throwErr) {
					throw new IllegalArgumentException(e);
				}
				else {
					return UnparsableMessage.parse(bytes, srcPort);
				}
			}
		}
		else if (throwErr) {
			throw new IllegalArgumentException(String.format("Illegal message header! (0x%04X)", (int) msgHeader));
		}
		else {
			return UnparsableMessage.parse(bytes, srcPort);
		}
	}
	
	
	/**
	 * Registers a header with a message parser
	 * @return whether the header could successfully be registered with the factory
	 */
	private synchronized static boolean registerParser(Class<? extends Message> clazz, MessageParser parser) {
		if (headerMap.containsKey(clazz)) {
			return false;
		}
		
		char header = (char) (headerMap.size() + 1);
		headerMap.put(clazz, header);
		parserMap.put(header, parser);
		return true;
	}
	
	/**
	 * Registers all valid message types with the factory
	 * ALL subclasses of 
	 */
	public synchronized static void registerParsers() {
		if (!headerMap.isEmpty()) {
			return;
		}
		
		registerParser(ElevStatusNotify.class, ElevStatusNotify::parse);
		registerParser(FloorRequest.class, FloorRequest::parse);
		registerParser(MessageAck.class, MessageAck::parse);
		registerParser(NoMoreEventsNotify.class, NoMoreEventsNotify::parse);
		registerParser(RegisterFloorRequest.class, RegisterFloorRequest::parse);
		registerParser(StopRequest.class, StopRequest::parse);
		registerParser(StopResponse.class, StopResponse::parse);
		registerParser(UnregisterElevatorRequest.class, UnregisterElevatorRequest::parse);
		registerParser(RegisterElevatorRequest.class, RegisterElevatorRequest::parse);
		registerParser(CompletedFloorRequest.class, CompletedFloorRequest::parse);
		
		Log.log("Registered Message Headers", Log.Level.INFO);
	}
	
	
	protected synchronized static char getHeader(Class<? extends Message> clazz) {
		if (!headerMap.containsKey(clazz)) {
			return 0;
		}
		return headerMap.get(clazz);
	}

	
	private interface MessageParser {
		public Message parse(byte[] bytes, int srcPort);
	}

}
