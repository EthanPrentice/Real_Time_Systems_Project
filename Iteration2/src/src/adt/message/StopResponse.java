package src.adt.message;

public class StopResponse extends Message {
	
	public StopResponse() {
		this(0);
	}
	
	public StopResponse(int srcPort) {
		super(srcPort);
	}
	
	@Override
	public String toString() {
		return "StopResponse()";
	}

	@Override
	public byte[] toBytes() {
		return new byte[] {0x0, 0x05};
	}
	
	/**
	 * @param bytes The byte array to read in the StopRequest from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed StopRequest from [bytes].
	 */
	public static StopResponse parse(byte[] bytes, int srcPort) {
		return new StopResponse(srcPort);
	}

}
