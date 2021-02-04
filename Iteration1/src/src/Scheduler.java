/**
 * 
 */
package src;

import src.adt.*;

/**
 * @author noell
 *
 */
public class Scheduler {
	
	public void put(FloorEvent event) {
		// add to queue & notify this that event has been added to queue
		notify();
	}

}
