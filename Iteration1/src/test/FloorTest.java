package test;

import org.junit.jupiter.api.Test;

import src.*;

class FloorTest {

	@Test
	void testParsing() {
		Floor floor = new Floor();
		
		Thread floorThread = new Thread(floor);
		floorThread.start();
	}

}
