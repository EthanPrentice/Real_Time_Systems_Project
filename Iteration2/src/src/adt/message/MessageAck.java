package src.adt.message;

/**
 * Written for SYSC3303 - Assignment 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Used to notify the sender that the message was received, and whether or not is was successfully parsed and handled
 * It can also be used to send other messages back as acknowledgments 
 *
 */
public class MessageAck extends Message {
	
	// indicates whether or not the message was handled successfully
	private boolean handledSuccessfully;
	
	// the message to send back in the ack
	// null if only a reply is needed, without any response data
	private Message requestedMessage;
	
	
	public MessageAck(boolean handledSuccessfully, int srcPort) {
		this(handledSuccessfully, null, srcPort);
	}
	
	public MessageAck(boolean handledSuccessfully) {
		this(handledSuccessfully, null);
	}
	
	public MessageAck(boolean handledSuccessfully, Message requestedMessage, int srcPort) {
		super(srcPort);
		this.handledSuccessfully = handledSuccessfully;
		this.requestedMessage = requestedMessage;
	}
	
	public MessageAck(boolean handledSuccessfully, Message requestedMessage) {
		this.handledSuccessfully = handledSuccessfully;
		this.requestedMessage = requestedMessage;
	}
	

	@Override
	public byte[] toBytes() {
		byte successByte = (byte) (handledSuccessfully ? 1 : 0);
		byte hasMessageByte = (byte) ((requestedMessage != null) ? 1 : 0);
		
		if (requestedMessage == null) {
			return new byte[] {0x0F, 0x0F, successByte, hasMessageByte};
		}
		
		byte[] reqMsgBytes = requestedMessage.toBytes();
		byte[] msgBytes = new byte[4 + reqMsgBytes.length];
		
		msgBytes[0] = 0x0F;
		msgBytes[1] = 0x0F;
		msgBytes[2] = successByte;
		msgBytes[3] = hasMessageByte;
		
		for (int i = 4; i < msgBytes.length; ++i) {
			msgBytes[i] = reqMsgBytes[i - 4];
		}
		
		return msgBytes;
	}
	
	
	public boolean getHandledSuccessfully() {
		return handledSuccessfully;
	}
	
	
	public Message getRequestedMessage() {
		return requestedMessage;
	}
	
	
	@Override
	public String toString() {
		String format = "MessageAck(success=%b, msg=%s)";
		return String.format(format, handledSuccessfully, requestedMessage);
	}
	
	
	/**
	 * @param bytes The byte array to read in the MessageAck from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed MessageAck from [bytes].
	 */
	public static MessageAck parse(byte[] bytes, int srcPort) {
		
		if (bytes.length >= 4 && bytes[0] == 0xF && bytes[1] == 0xF) {
			
			// Message also came with another Message to return to the sender
			if (bytes[3] == 1) {
				byte[] msgReqBytes = new byte[bytes.length - 4];
				System.arraycopy(bytes, 4, msgReqBytes, 0, msgReqBytes.length);
				
				Message requestedMessage = Message.fromBytes(msgReqBytes, srcPort, false);
				
				return new MessageAck(bytes[2] == 1, requestedMessage, srcPort);
			}
			
			// No additional Message to contain, send back empty acknowledgement
			return new MessageAck(bytes[2] == 1, srcPort);
		}
		return null;
	}

}
