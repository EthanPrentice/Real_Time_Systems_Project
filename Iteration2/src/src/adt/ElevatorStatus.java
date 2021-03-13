package src.adt;

import java.util.Arrays;

import src.adt.message.FloorRequest;

public class ElevatorStatus {
	
	private int currFloor;
	private ElevatorState state;
	private int[] floorOccupancy;
	
	
	public ElevatorStatus(int currFloor, ElevatorState state, int[] floorOccupancy) {
		this.currFloor = currFloor;
		this.state = state;
		this.floorOccupancy = floorOccupancy;
	}
	
	
	@Override
	public String toString() {
		String format = "ElevatorStatus(floor=%d, state=%s, floorOcc=%s)";
		return String.format(format, currFloor, state.name().toLowerCase(), Arrays.toString(floorOccupancy));
	}
	
	
	private int getMaxOccupancy(int minFloor, int maxFloor) {
		int occupancy = 0;
		for (int i = minFloor; i <= maxFloor; ++i) {
			occupancy = Integer.max(occupancy, floorOccupancy[i]);
		}
		return occupancy;
	}
	
	public int getMaxOccupancy(FloorRequest e) {
		if (e.getDirection() == ButtonDirection.UP) {
			return getMaxOccupancy(e.getSourceFloor(), e.getDestFloor());
		}
		else {
			return getMaxOccupancy(e.getDestFloor(), e.getSourceFloor());
		}
	}
	
	
	public int[] getFloorOccupancy() {
		return floorOccupancy;
	}
	
	
	public int getFloor() {
		return currFloor;
	}
	
	public void setFloor(int newFloor) {
		currFloor = newFloor;
	}
	
	public ElevatorState getState() {
		return state;
	}
	
	public void setState(ElevatorState newState) {
		state = newState;
	}

}
