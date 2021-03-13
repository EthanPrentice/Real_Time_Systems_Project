package src.adt.message;

public class RegisterFloorRequest extends Message {
	
	private boolean hasMoreEvents;
	
	public RegisterFloorRequest(boolean hasMoreEvents) {
		this(hasMoreEvents, 0);
	}
	
	public RegisterFloorRequest(boolean hasMoreEvents, int srcPort) {
		super(srcPort);
		this.hasMoreEvents = hasMoreEvents;
	}

	@Override
	public String toString() {
		String format = "RegisterFloorRequest(port=%d, hasMoreEvents=%b)";
		return String.format(format, srcPort, hasMoreEvents);
	}
	
	public boolean getHasMoreEvents() {
		return hasMoreEvents;
	}
	
	@Override
	public byte[] toBytes() {
		return new byte[] {0, 0x07, (byte) (hasMoreEvents ? 1 : 0)};
	}
	
	
	/**
	 * @param bytes The byte array to read in the RegisterFloorRequest from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed RegisterFloorRequest from [bytes].
	 */
	public static RegisterFloorRequest parse(byte[] bytes, int srcPort) {
		boolean hasMoreEvents = bytes[2] == 1;
		return new RegisterFloorRequest(hasMoreEvents, srcPort);
	}

}
