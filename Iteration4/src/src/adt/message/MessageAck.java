package src.adt.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Written for SYSC3303 - Assignment 3 @ Carleton University
 * Modified for SYSC3303 - Group 6 - Iteration 3
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

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeChar(getHeader());
			dos.write(successByte);
			dos.write(hasMessageByte);
			
			if (requestedMessage != null) {
				dos.write(requestedMessage.toBytes());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return bos.toByteArray();
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
		ByteBuffer buff = ByteBuffer.wrap(bytes);
		buff.getChar(); // header
		
		if (bytes.length >= 4) {
			
			// Message also came with another Message to return to the sender
			boolean handledSuccessfully = (buff.get() == 1);
			if (buff.get() == 1) { // has message
				byte[] msgReqBytes = new byte[buff.remaining()];
				buff.get(msgReqBytes);
				
				Message requestedMessage = Message.fromBytes(msgReqBytes, srcPort, false);
				
				return new MessageAck(handledSuccessfully, requestedMessage, srcPort);
			}
			
			// No additional Message to contain, send back empty acknowledgement
			return new MessageAck(handledSuccessfully, srcPort);
		}
		return null;
	}
	
}
