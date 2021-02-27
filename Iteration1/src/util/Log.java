package util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;


/**
 * Used to manage printing to the console, including system time and thread names
 * @author Ethan Prentice (101070194)
 */
public class Log {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	public static void log(String message) {        
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        
        String format = "[%s][Thread=%s] %s";
        System.out.println(String.format(format, sdf.format(timestamp), Thread.currentThread().getName(), message));
	}
}
