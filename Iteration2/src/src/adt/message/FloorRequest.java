package src.adt.message;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import java.util.*;

import src.adt.ButtonDirection;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Data class representation of the events read in from the data file
 */
public class FloorRequest extends Message {
	private LocalTime reqTime;
	private int srcFloor;
	private ButtonDirection btnDirection;
	private int dstFloor;
	
	
	public FloorRequest(LocalTime reqTime, int srcFloor, ButtonDirection btnDirection, int dstFloor) {
		this.reqTime = reqTime;
		this.srcFloor = srcFloor;
		this.btnDirection = btnDirection;
		this.dstFloor = dstFloor;
	}
	
	
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner(" ");
		
		sj.add(reqTime.toString());
		sj.add(new Integer(srcFloor).toString());
		sj.add(btnDirection.name());
		sj.add(new Integer(dstFloor).toString());
		
		return sj.toString();
	}
	
	
	// Getters
	public LocalTime getRequestTime() {
		return reqTime;
	}
	
	public int getSourceFloor() {
		return srcFloor;
	}
	
	public ButtonDirection getDirection() {
		return btnDirection;
	}
	
	public int getDestFloor() {
		return dstFloor;
	}
	// End of getters
	
	
	@Override
	public byte[] toBytes() {
		byte[] msgBytes = toString().getBytes();
		
		byte[] bytes = new byte[msgBytes.length + 2];
		
		bytes[0] = 0;
		bytes[1] = 0x01;
		
		System.arraycopy(msgBytes, 0, bytes, 2, msgBytes.length);
		
		return bytes;
	}
	
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof FloorRequest) {
			FloorRequest e = (FloorRequest) o;
			return reqTime.equals(e.reqTime)
				&& srcFloor == e.srcFloor
				&& btnDirection.equals(e.btnDirection)
				&& dstFloor == e.dstFloor;
		} else {
			return false;
		}
	}
	
	
	/**
	 * @param s : an input string of the format: "{hh:mm:ss.mmm} {int} {up|down} {int}".
	 * 			: in order these are time, floor, direction, car button
	 * 
	 * @exception IllegalArgumentException if the string cannot be parsed
	 * @return a FloorEvent with the parsed values of s
	 */
	public static FloorRequest parseFromString(String s) throws IllegalArgumentException {	
		String[] args = s.split("\\s+");
		if (args.length != 4) {
			throw new IllegalArgumentException("Input string has inproper formatting.  Must include 4 variables. (" + s + ")");
		}
		
		LocalTime reqTime;
		ButtonDirection btnDirection = ButtonDirection.fromString(args[2]);
		int floorNum;
		int carBtn;
		
		// Ensure that event is properly parsed.  Else throw IllegalArgumentException.
		try {
			reqTime = LocalTime.parse(args[0]);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Time could not be parsed from input string. (" + s + ")");
		}
		
		try {
			floorNum = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Floor number could not be parsed from input string. (" + s + ")");
		}
		
		if (btnDirection == null) {
			throw new IllegalArgumentException("Button direction could not be parsed from input string. (" + s + ")");
		}
		
		try {
			 carBtn = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Car button could not be parsed from input string. (" + s + ")");
		}
		
		return new FloorRequest(reqTime, floorNum, btnDirection, carBtn);
	}
	
	
	/**
	 * @param bytes The byte array to read in the FloorRequest from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed FloorRequest from [bytes].
	 */
	public static FloorRequest parse(byte[] bytes, int srcPort) {
		byte[] msgBytes = new byte[bytes.length-2];
		System.arraycopy(bytes, 2, msgBytes, 0, msgBytes.length);
		return FloorRequest.parseFromString(new String(msgBytes));
	}
	
}
