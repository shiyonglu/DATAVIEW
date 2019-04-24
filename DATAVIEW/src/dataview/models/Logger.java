package dataview.models;
import java.text.*;
import java.util.*;
import java.io.*;

/**
 * 
 * This logger class used to log information based on the level
 *
 */

public class Logger {
	public final static int LOGALL = 100; // log everything under 100
	public final static int LOGNO = -1; // log everyting under -1, 0 is the most serious one
	// the file name of the log file, note that we will only append records to a log file, we will never delete anything from a log
	private String logfilename;  
	private int currentlevel;
	private boolean displayon = false;
	
	public Logger(String logfilename)
	{
		this.logfilename = logfilename;
		this.currentlevel = Logger.LOGALL; 
	}
	
	public int getLevel()
	{
		return currentlevel;
	}
	
	public void setDisplay(boolean on)
	{
		this.displayon = on;
	}
	
	
	public void setLevel(int newlevel)
	{
		this.currentlevel = newlevel;
	}
	
	
	/* level 0 is most serious, 1 is less serious, we will only  
	 * record messages whose seriousness is smaller or equal to the current level. 
	 */
	public boolean append(int level, String message) {
		FileWriter fw = null;
		BufferedWriter bw = null;
		String logmsg = null;
		
		
		if (level > currentlevel) return false;  // will not log
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		Date now = new Date();
		logmsg = dateFormat.format(now);
		
		File file = new File(logfilename);
			
		try {
			 if(!file.exists()) file.createNewFile();
			
			 fw = new FileWriter(file.getAbsoluteFile(), true);
			 bw = new BufferedWriter(fw);
			
			logmsg = logmsg + ", ";
			logmsg = logmsg+ "Level: "+level+", "+message+"\n";
			bw.write(logmsg);
			bw.close();
			fw.close();
			
			if(displayon) {
				System.out.println(logmsg);
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		
		return true;
	}
	
}
