package src.adt;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import src.adt.message.Message;
import src.adt.message.UnparsableMessage;
import util.Log;

public abstract class MessageHandler {
	
	protected DatagramSocket sock;
	
	protected MessageHandler() {
		this(0);
	}
	
	protected MessageHandler(int sockPort) {
		try {
			// Construct & bind DatagramSocket to any available port
			sock = new DatagramSocket(sockPort);
			Log.log("Opened socket on port: " + sock.getLocalPort());
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	

	/**
	 * Sends the passed in Message to the destPort
	 * 
	 * @param msg The Message to send to the destPort
	 */
	public void send(Message msg, int destPort) {	
		byte msgBytes[] = msg.toBytes();
		
		DatagramPacket sendPacket = null;
		try {
			sendPacket = new DatagramPacket(msgBytes, msgBytes.length, InetAddress.getLocalHost(), destPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		String format = "Sending packet to %s:%d.  (length=%d)";
		Log.log(String.format(format, sendPacket.getAddress(), sendPacket.getPort(), sendPacket.getLength()));
		
		Log.log("Packet contents (str)  : " + msg.toString());
		Log.log("Packet contents (bytes): " + Arrays.toString(msg.toBytes()) + "\n");
		
		try {
			// send request to the scheduler
			sock.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	protected Message receive() throws IOException {
		
		// Construct a DatagramPacket that can receive packets up to Message max size
		byte data[] = new byte[Message.MAX_BYTES];
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);

		// Block until a udp packet is received
		sock.receive(receivePacket);

		// Process the received packet
		String format = "Packet received from %s:%d.  (length=%d)";
		Log.log(String.format(format, receivePacket.getAddress(), receivePacket.getPort(), receivePacket.getLength()));

		// Parse the response from the packet.  If it cannot be parsed, receive an UnparseableMessage
		Message received = Message.fromBytes(data, receivePacket.getPort(), false);
		Log.log("Packet contents (str)  : " + received.toString());
		Log.log("Packet contents (bytes): " + Arrays.toString(received.toBytes()) + "\n");
		
		if (received instanceof UnparsableMessage) {
			throw new IllegalStateException("Received an UnparsableMessage!! " + received.toString());
		}
		return received;
	}
	
	public int getPort() {
		return sock.getLocalPort();
	}
	
}
