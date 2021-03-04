package util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;


/**
 * Used to manage printing to the console, including system time and thread names
 * @author Ethan Prentice (101070194)
 */
public class Log {
	
	private static Level logLevel = Level.VERBOSE;
	
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	public static void setLevel(Level newLevel) {
		logLevel = newLevel;
	}
	
	public static void log(String message) {        
        log(message, Level.DEBUG);
	}
	
	
	public static void log(String message, Level msgLevel) {
		if (msgLevel.value <= logLevel.value) {
	        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	        
	        String format = "[%s][Thread=%s] %s";
	        System.out.println(String.format(format, sdf.format(timestamp), Thread.currentThread().getName(), message));
		}
	}
	
	
	public enum Level {
		VERBOSE(0),
		DEBUG(2),
		INFO(1);
		
		public final int value;
		
		private Level(int value) {
			this.value = value;
		}
	}
}
