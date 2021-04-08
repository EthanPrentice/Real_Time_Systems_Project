package src.adt;

import java.util.Arrays;

import src.adt.message.FloorRequest;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Data class to store the elevator's status, including it's floor, state, and expected occupancy per floor
 */
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
		for (int i = minFloor - 1; i < maxFloor; ++i) {
			occupancy = Integer.max(occupancy, floorOccupancy[i]);
		}
		return occupancy;
	}
	
	/**
	 * 
	 * @param req
	 * @return
	 */
	public int getMaxOccupancy(FloorRequest req) {
		if (req.getDirection() == ButtonDirection.UP) {
			return getMaxOccupancy(req.getSourceFloor(), req.getDestFloor());
		}
		else {
			return getMaxOccupancy(req.getDestFloor(), req.getSourceFloor());
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
