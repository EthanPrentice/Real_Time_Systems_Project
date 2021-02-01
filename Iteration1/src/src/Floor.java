package src;

import java.util.*;

/**
 * Written for SYSC3303 - Group 6 - Iteration 1 @ Carleton University
 * @author Ethan Prentice (101070194)
 */
public class Floor {

	private Stack<FloorEvent> eventStack = new Stack();
	
	
	public synchronized void put(FloorEvent[] newFoods) {
		while (!isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
		
		for (int i = 0; i < newFoods.length; ++i) {
			if (newFoods[i] == null) {
				throw new IllegalArgumentException("Cannot have a null food!!");
			}
		}
		
		System.arraycopy(newFoods, 0, foods, 0, FOOD_COUNT);
		notifyAll();
	}
	
	
	public synchronized Food[] peek() {
		while (isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
		return Arrays.copyOf(foods, FOOD_COUNT);
	}
	
	
	public synchronized Food[] take() {
		while (isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
		
		Food[] res = Arrays.copyOf(foods, FOOD_COUNT);
		clear();
		return res;
	}
	
	
	public synchronized boolean isEmpty() {
		return foods[0] == null; // if first food is null, all are null
	}
	
	
	private synchronized void clear() {
		for (int i = 0; i < foods.length; ++i) {
			foods[i] = null;
		}
		notifyAll();
	}
	
}
