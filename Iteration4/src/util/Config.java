package util;

/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Configures options for the program
 */
public class Config {
	public static boolean USE_ZERO_FLOOR_TIME = false;
	public final static int NUM_FLOORS = 22;
	
	public static boolean EXPORT_MEASUREMENTS = true;
	public static String MEASURE_PATH = "./out/";
	
	
	public final static long FLOOR_TIME_MS = 9_500L;
	public final static long LOAD_TIME_MS = 9_350L;
	
	public final static long FLOOR_ERR_TIMER_MS = (long) (FLOOR_TIME_MS * 1.2);  // shouldn't have that much variance
	public final static long DOOR_ERR_TIMER_MS = (long) (LOAD_TIME_MS * 1.4);    // more variance, higher modifier
	
	public final static boolean SHOW_UI = true;
	public final static boolean UI_RESIZABLE = false;
	public static boolean CLOSE_UI_ON_FINISH = false; // should be true for testing
}
