package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import src.Elevator;
import src.Floor;
import src.Scheduler;
import src.adt.ButtonDirection;

/**
 * Written for SYSC3303 - Group 6 - Iteration 5 @ Carleton University
 * @author Baillie Noell (101066676)
 * @edit Ethan Prentice (101070194)
 * 
 */
public class Timing {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
	
	
	private Thread floorThread;
	private Thread schedulerThread;
	
	
	public void timeElevators(File inFile, MeasureWriter measureWriter, int elevatorCount) {
		Config.USE_ZERO_FLOOR_TIME = true;
		Config.CLOSE_UI_ON_FINISH = true;
		
		Scheduler scheduler = new Scheduler();
		Floor floor = new Floor(measureWriter);

		// Instantiate threads
		schedulerThread = new Thread(scheduler, "Scheduler");
		floorThread = new Thread(floor, "Floor");
		ArrayList<Thread> elevatorThreads = new ArrayList<>();
		for (int i = 0; i < elevatorCount; ++i) {
			elevatorThreads.add(new Thread(new Elevator()));
		}
		
		floor.setFile(inFile);
		
		schedulerThread.start();
		scheduler.waitUntilCanRegister();
		for (Thread t : elevatorThreads) {
			t.start();
		}
		floorThread.start();
		
		joinThreads();
	}
	
	
	private File getRandomInputFile(int length) {
		File tmpFile;
		PrintWriter pw;
		try {
			tmpFile = File.createTempFile(getTempFilename(), null);
			FileOutputStream oStream = new FileOutputStream(tmpFile);
			pw = new PrintWriter(oStream);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		int b;
		int sec = 0;
		for (int i = 0; i < length; i++) {
			sec += (int) (Math.random()*15 + 1);
			
			LocalTime reqTime = LocalTime.ofSecondOfDay(sec);
			String formattedTime = reqTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
			
			int a = (int) (Math.random()*Config.NUM_FLOORS + 1);
			do {
				b = (int) (Math.random()*Config.NUM_FLOORS + 1);
			} while (a == b);
			
			String dir = (a > b ? ButtonDirection.DOWN : ButtonDirection.UP).name();
			
			pw.println(String.format("%s %d %s %d %d", formattedTime, a, dir, b, 0));
		}
		
		pw.close();
		
		return tmpFile;
	}
	
	
	private void joinThreads() {
		 try {
			schedulerThread.join();
			floorThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	private static String getTempFilename() {
		return sdf.format(new Date()) + "_generated_input_sysc3303_project.txt";
	}
	
	
	public static void main(String[] args) {
		Timing t = new Timing();
		
		File inFile;
		
		int minElevators = 2;
		int maxElevators = 4;
		ArrayList<MeasureWriter> writers = new ArrayList<>();
		boolean overwriteFiles = false;
		for (int i = 0; i <= (maxElevators - minElevators); ++i) {
			String filename = String.format("%d_elevators_timing.csv", minElevators + i);
			
			// if files exist, throw exception
			File file = new File(Config.MEASURE_PATH, filename);
			
			
			if (file.exists()) {
				if (overwriteFiles) {
					file.delete();
				}
				else {
					Scanner scanner = new Scanner(System.in);
					System.out.print("Output files already exist!!  Would you like to overwrite them? (y/n) ");
					String input = scanner.next();
					scanner.close();
					
					if (input.toLowerCase().equals("y")) {
						overwriteFiles = true;
						file.delete();
					} else {
						throw new IllegalStateException("Cannot overwrite files.");
					}
				}
			}
			
			writers.add(new MeasureWriter(filename));
		}
		
		int samples = 30;
		int inputLength = 10;
		
		for (int i = 0; i < samples; ++i) {
			inFile = t.getRandomInputFile(inputLength);
			
			// in-case of error generating input
			if (inFile == null) {
				break;
			}
			
			for (int j = 0; j <= (maxElevators - minElevators); ++j) {
				t.timeElevators(inFile, writers.get(j), minElevators + j);
			}
			
		}
	}

}
