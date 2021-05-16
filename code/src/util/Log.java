package util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;


/**
 * Written for SYSC3303 - Group 6 - Iteration 3 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 * Used to manage printing to the console, including system time and thread names
 */
public class Log {
	
	private static Level logLevel = Level.INFO;
	
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	public static void setLevel(Level newLevel) {
		logLevel = newLevel;
	}
	
	public static void log(String message) {        
        log(message, Level.DEBUG);
	}
	
	
	public static synchronized void log(String message, Level msgLevel) {
		if (msgLevel.value >= logLevel.value) {
	        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	        
	        String format = "[%s][%s] %s";
	        System.out.println(String.format(format, sdf.format(timestamp), Thread.currentThread().getName(), message));
		}
	}
	
	
	public enum Level {
		VERBOSE(0),
		DEBUG(1),
		INFO(2),
		NONE(3);
		
		public final int value;
		
		private Level(int value) {
			this.value = value;
		}
	}
}
