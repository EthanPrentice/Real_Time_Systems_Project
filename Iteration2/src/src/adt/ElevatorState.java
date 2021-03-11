package src.adt;

/**
 * Used to manage the elevator states
 * @author Ethan Prentice (101070194)
 */
public enum ElevatorState {
	MOVING_UP(0),
	MOVING_DOWN(1),
	STOPPED(2),
	DOORS_OPEN(3),
	DOORS_CLOSED(4);
	
	public final byte stateByte;
	
	private ElevatorState(byte b) {
		stateByte = b;
	}
	
	private ElevatorState(int b) {
		stateByte = (byte) b;
	}
	
	public static ElevatorState fromByte(byte b) {
		for (ElevatorState e : ElevatorState.values()) {
			if (e.stateByte == b) {
				return e;
			}
		}
		return null;
	}
}
