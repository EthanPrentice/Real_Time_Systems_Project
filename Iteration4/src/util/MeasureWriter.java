package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringJoiner;

import src.adt.message.FloorRequest;

/**
 * Written for SYSC3303 - Group 6 - Iteration 5 @ Carleton University
 * @author Ethan Prentice (101070194)
 * 
 */
public class MeasureWriter {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
	
	private FileOutputStream oStream;
	private PrintWriter pw;
	
	public MeasureWriter() {
		try {
			File file = new File(Config.MEASURE_PATH, getFilename());
			if (!file.exists()) {
				file.createNewFile();
			}
			else {
				throw new IOException("File already exists!! (" + file.getAbsolutePath() + ")");
			}
			
			oStream = new FileOutputStream(file);
			pw = new PrintWriter(oStream);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		writeHeaders();
	}
	
	
	public void writeTiming(char elevatorId, FloorRequest req, long timeMs) {
		StringJoiner sj = new StringJoiner(",");
		sj.add(Integer.toString((int) elevatorId));
		sj.add(Integer.toString(req.getSourceFloor()));
		sj.add(Integer.toString(req.getDestFloor()));
		sj.add(Long.toString(timeMs));
		
		pw.println(sj.toString());
	}
	
	
	public void flush() {
		pw.flush();
	}
	
	
	public void close() {
		pw.close();
	}
	
	
	private void writeHeaders() {
		StringJoiner sj = new StringJoiner(",");
		sj.add("Elevator ID");
		sj.add("Src Floor");
		sj.add("Dst Floor");
		sj.add("Completion Time (ms)");
		
		pw.println(sj.toString());
	}
	
	private String getFilename() {
		return sdf.format(new Date()) + "_measure.csv";
	}
}
