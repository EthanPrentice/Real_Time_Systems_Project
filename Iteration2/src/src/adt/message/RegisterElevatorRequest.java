package src.adt.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import src.adt.ElevatorState;
import src.adt.ElevatorStatus;


public class RegisterElevatorRequest extends Message {
	
	private char elevatorId;
	private ElevatorStatus status;
	
	public RegisterElevatorRequest(char elevatorId, ElevatorStatus status) {
		this(elevatorId, status, 0);
	}
	
	public RegisterElevatorRequest(char elevatorId, ElevatorStatus status, int srcPort) {
		super(srcPort);
		this.elevatorId = elevatorId;
		this.status = status;
	}
	
	
	@Override
	public String toString() {
		String format = "RegisterElevatorRequest(elevatorId=%d, port=%d)";
		return String.format(format, (int) elevatorId, srcPort);
	}
	
	public ElevatorStatus getStatus() {
		return status;
	}
	
	public char getElevatorId() {
		return elevatorId;
	}
	

	@Override
	public byte[] toBytes() {
		ByteArrayOutputStream inStream = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(inStream);
		try {
			dos.write(new byte[]{0, 3});
			dos.writeChar(elevatorId);
			
			dos.writeInt(status.getFloor());
			dos.writeByte(status.getState().stateByte);
			for (int i : status.getFloorOccupancy()) {
				dos.writeInt(i);
			}
			
			
			return inStream.toByteArray();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	/**
	 * @param bytes The byte array to read in the RegisterElevatorRequest from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed RegisterElevatorRequest from [bytes].
	 */
	public static RegisterElevatorRequest parse(byte[] bytes, int srcPort) {		
		ByteBuffer buff = ByteBuffer.wrap(bytes, 2, bytes.length - 2);
		char elevatorId = buff.getChar();
		
		int currFloor = buff.getInt();
		ElevatorState state = ElevatorState.fromByte(buff.get());
		
		int[] floorOccupancy = new int[buff.remaining() / 4];
		for (int i = 0; i < floorOccupancy.length; ++i) {
			floorOccupancy[i] = buff.getInt();
		}
		
		ElevatorStatus status = new ElevatorStatus(currFloor, state, floorOccupancy);
		
		return new RegisterElevatorRequest(elevatorId, status, srcPort);
	}

}
