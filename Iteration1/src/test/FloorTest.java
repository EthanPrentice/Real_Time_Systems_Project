package test;

import org.junit.jupiter.api.Test;

import java.io.File;

import src.*;

class FloorTest {

	@Test
	void testParsing() {
		File file = new File("res/test_data.txt");
		Floor floor = new Floor();
		
		Runnable r = new Runnable() {
			@Override
			public void run(){
				while (!floor.isEmpty()) {
					System.out.println(floor.pop());
				}
			}
		};
		
		Thread thread = new Thread(r);
		thread.start();
		
		floor.readFromFile(file);
	}

}
