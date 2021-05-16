package src.adt;

/**
 * Used to manage the elevator errors to inject into some systems
 * @author Ethan Prentice (101070194)
 */
public enum ErrorType {
	NO_ERROR(0),
	UNEXPECTED_STOP(1),
	DOORS_ERROR(2);
	
	public final byte stateByte;
	
	private ErrorType(byte b) {
		stateByte = b;
	}
	
	private ErrorType(int b) {
		stateByte = (byte) b;
	}
	
	public static ErrorType fromByte(byte b) {
		for (ErrorType e : ErrorType.values()) {
			if (e.stateByte == b) {
				return e;
			}
		}
		return null;
	}
}
