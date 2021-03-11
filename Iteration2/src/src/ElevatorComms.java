package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import src.adt.message.FloorRequest;
import src.adt.message.Message;
import src.adt.message.MessageAck;
import src.adt.message.ResourceRequest;
import util.Log;

public class ElevatorComms implements Runnable {
	
	private DatagramSocket sock;
	
	private Elevator elevator;
	
	
	private Queue<Message> messageQueue = new LinkedList<Message>();
	private Queue<Message> responseQueue = new LinkedList<Message>();
	
	
	public ElevatorComms(Elevator elevator) {
		this.elevator = elevator;
		
		try {
			// Construct & bind DatagramSocket to any available port
			sock = new DatagramSocket(0);
			Log.log("Opened socket on port: " + sock.getLocalPort());
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
	}

	@Override
	public void run() {
		while (true) {
			while (messageQueue.isEmpty()) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			// send message and wait for response
			Message msg = messageQueue.remove();
			sendAndAck(msg);
			
			// Have functions to handle response, and send back response to Elevator
			ResourceRequest resReq = new ResourceRequest((char) 0x0001, elevator.getElevatorId());
			MessageAck ack = sendAndAck(resReq);
			Message receivedMessage = ack.getRequestedMessage();
			if (receivedMessage != null && receivedMessage instanceof FloorRequest) {
				responseQueue.add(receivedMessage);
			}
			
		}
		
	}
	
	/**
	 * Send message to Scheduler in this thread
	 * @param msg
	 */
	public synchronized void putMessage(Message msg) {
		messageQueue.add(msg);
		notifyAll();
	}
	
	
	public synchronized Message popResponse() {
		if (!responseQueue.isEmpty()) {
			return responseQueue.remove();
		}
		return null;
	}
	
	
	/**
	 * Sends [msg] to host and waits for an acknowledgement the host has received the message before continuing
	 * @param msg the message to send to the host
	 */
	private MessageAck sendAndAck(Message msg) {
		send(msg);
		return receiveAck();
	}
	

	/**
	 * Sends the passed in Message to the Scheduler's receive port, and prints info to the console
	 * 
	 * @param msg The Message to send to the Scheduler
	 */
	private void send(Message msg) {	
		byte msgBytes[] = msg.toBytes();
		
		DatagramPacket sendPacket = null;
		try {
			sendPacket = new DatagramPacket(msgBytes, msgBytes.length, InetAddress.getLocalHost(), Scheduler.RECEIVE_PORT);
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
	
	
	/**
	 * Receives a MessageAck from [sock] and prints it's data to the console
	 * @return the parsed Message that was received
	 */
	private MessageAck receiveAck() {
		
		// Construct a DatagramPacket that can receive packets up to Message max size
		byte data[] = new byte[Message.MAX_BYTES];
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);

		try {
			// Block until a udp packet is received
			sock.receive(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		// Process the received packet
		String format = "Packet received from %s:%d.  (length=%d)";
		Log.log(String.format(format, receivePacket.getAddress(), receivePacket.getPort(), receivePacket.getLength()));

		// Parse the response from the packet.  If it cannot be parsed, receive an UnparseableMessage
		Message response = Message.fromBytes(data, receivePacket.getPort(), false);
		Log.log("Packet contents (str)  : " + response.toString());
		Log.log("Packet contents (bytes): " + Arrays.toString(response.toBytes()) + "\n");
		
		if (response instanceof MessageAck) {
			return (MessageAck) response;
		}
		else {
			throw new IllegalStateException("Expected a MessageAck but received a " + response.toString());
		}
	}

}
