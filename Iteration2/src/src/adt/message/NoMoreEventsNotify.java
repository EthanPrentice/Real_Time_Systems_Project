package src.adt.message;

public class NoMoreEventsNotify extends Message {

	public NoMoreEventsNotify() {
		this(0);
	}
	
	public NoMoreEventsNotify(int srcPort) {
		super(srcPort);
	}
	
	@Override
	public String toString() {
		return "NoMoreEventsNotify()";
	}
	
	
	@Override
	public byte[] toBytes() {
		return new byte[] {0, 0x06};
	}

	/**
	 * @param bytes The byte array to read in the NoMoreEventsNotify from
	 * @throws IllegalArgumentException if the request cannot be parsed from the bytes
	 * 
	 * @return A parsed NoMoreEventsNotify from [bytes].
	 */
	public static NoMoreEventsNotify parse(byte[] bytes, int srcPort) {
		return new NoMoreEventsNotify(srcPort);
	}
	
}
