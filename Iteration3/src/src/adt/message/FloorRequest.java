package src.adt.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import java.util.*;

import src.adt.ButtonDirection;
import src.adt.ErrorType;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Data class representation of the events read in from the data file
 * Sent to the Elevator to notify it to handle an event initiated by the Floor
 */
public class FloorRequest extends Message {
	private LocalTime reqTime;
	private int srcFloor;
	private ButtonDirection btnDirection;
	private int dstFloor;
	private ErrorType errorType;
	
	
	public FloorRequest(LocalTime reqTime, int srcFloor, ButtonDirection btnDirection, int dstFloor, ErrorType errorType) {
		this(reqTime, srcFloor, btnDirection, dstFloor, errorType, 0);
	}
	
	public FloorRequest(LocalTime reqTime, int srcFloor, ButtonDirection btnDirection, int dstFloor, ErrorType errorType, int srcPort) {
		super(srcPort);
		this.reqTime = reqTime;
		this.srcFloor = srcFloor;
		this.btnDirection = btnDirection;
		this.dstFloor = dstFloor;
		this.errorType = errorType;
	}
	
	
	@Override
	public String toString() {
		return "FloorRequest(" + getDataString() + ")";
	}
	
	
	private String getDataString() {
		StringJoiner sj = new StringJoiner(" ");
		
		sj.add(reqTime.toString());
		sj.add(new Integer(srcFloor).toString());
		sj.add(btnDirection.name());
		sj.add(new Integer(dstFloor).toString());
		sj.add(Byte.toString(errorType.stateByte));
		
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
	
	public ErrorType getErrorType() {
		return errorType;
	}
	// End of getters
	
	
	@Override
	public byte[] toBytes() {
		byte[] msgBytes = getDataString().getBytes();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeByte(0x00);
			dos.writeByte(0x01);
			dos.write(msgBytes);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return bos.toByteArray();
	}
	
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof FloorRequest) {
			FloorRequest e = (FloorRequest) o;
			return reqTime.equals(e.reqTime)
				&& srcFloor == e.srcFloor
				&& btnDirection.equals(e.btnDirection)
				&& dstFloor == e.dstFloor
				&& errorType == e.errorType;
		} else {
			return false;
		}
	}
	
	
	/**
	 * @param s : an input string of the format: "{hh:mm:ss.mmm} {int} {up|down} {int}".
	 * 			: in order these are time, floor, direction, car button
	 * 
	 * @exception IllegalArgumentException if the string cannot be parsed
	 * @return a FloorRequest with the parsed values of s
	 */
	public static FloorRequest parseFromString(String s) throws IllegalArgumentException {	
		String[] args = s.split("\\s+");
		if (args.length != 5) {
			throw new IllegalArgumentException("Input string has inproper formatting.  Must include 5 variables. (" + s + ")");
		}
		
		LocalTime reqTime;
		ButtonDirection btnDirection = ButtonDirection.fromString(args[2]);
		int floorNum;
		int carBtn;
		ErrorType errorType;
		
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
		
		try {
			byte errorByte = Byte.parseByte(args[4]);
			errorType = ErrorType.fromByte(errorByte);
			
			if (errorType == null) {
				throw new IllegalArgumentException("Illegal ErrorType, \"" + errorByte + "\"");
			}
			
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Car button could not be parsed from input string. (" + s + ")");
		}
		
		return new FloorRequest(reqTime, floorNum, btnDirection, carBtn, errorType);
	}
	
	
	/**
	 * @param bytes The byte array to read in the FloorRequest from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed FloorRequest from [bytes].
	 */
	public static FloorRequest parse(byte[] bytes, int srcPort) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (int i = 2; i < bytes.length; ++i) {
			if (bytes[i] == 0) {
				break;
			}
			bos.write(bytes[i]);
		}
		FloorRequest req = FloorRequest.parseFromString(new String(bos.toByteArray()));
		req.setSrcPort(srcPort);
		return req;
	}
	
}
