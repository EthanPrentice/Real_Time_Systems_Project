package src.adt.message;

public class StopRequest extends Message {
	
	public StopRequest() {
		this(0);
	}
	
	public StopRequest(int srcPort) {
		super(srcPort);
	}
	
	@Override
	public String toString() {
		return "StopRequest()";
	}

	@Override
	public byte[] toBytes() {
		return new byte[] {0x0, 0x04};
	}
	
	/**
	 * @param bytes The byte array to read in the StopRequest from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed StopRequest from [bytes].
	 */
	public static StopRequest parse(byte[] bytes, int srcPort) {
		return new StopRequest(srcPort);
	}

}
