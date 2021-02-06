/**
 * 
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import src.*;
import src.adt.*;

/**
 * @author Nick
 *
 */
class SchedulerTest {

	@Test
	void schedulerPutGetTest() {
		
		Floor floor = new Floor();
		Elevator elevator = new Elevator();
		Scheduler scheduler = new Scheduler(floor, elevator);
		
		Event putEvent = new Event(LocalTime.parse("00:00:00.0"), 1, ButtonDirection.UP, 2);
		
		//Test putEvent
		scheduler.putEventFromFloor(putEvent);
		 
		Event getEvent = scheduler.getEvent();
		
		assertTrue(putEvent.getRequestTime() == getEvent.getRequestTime());
		assertTrue(putEvent.getFloorNum() == getEvent.getFloorNum());
		assertTrue(putEvent.getDirection() == getEvent.getDirection());
		assertTrue(putEvent.getCarButton() == getEvent.getCarButton());
	}

	@Test
	void sendEventToFloorTest() {
		Floor floor = new Floor();
		Elevator elevator = new Elevator();
		Scheduler scheduler = new Scheduler(floor, elevator);
		Event event = new Event(LocalTime.parse("00:00:00.0"), 1, ButtonDirection.UP, 2);
		
		scheduler.sendEventToFloor(event);
		
	}
}
