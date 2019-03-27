package dataview.models;
import java.io.*;
import java.util.*;

/** 
 * This class supports the date type of DATAVIEW_BigFile.
 * 
 * Several methods of this class allow one to convert a file, not necessarily big, to different data types defined in DATAVIEW. 
 *  
 * 
 */
public class DATAVIEW_BigFile {
	private String filename;
	private File   file;

	/** This is the constructor that takes a file name and constructs a DATAVIEW_BigFile object. 
	 * @param filename the name of a a file
	 *
	 */
	public DATAVIEW_BigFile(String filename)
	{
		this.filename  = filename;
		file = new File(filename);
	}
	
	/** Returns a DATAVIEW_MathVector object. 
	 * The method converts the content of a file into a {@link DATAVIEW_MathVector object}.
	 * 
	 * @return a reference to an DATAVIEW_MathVetor object
	 */
	public DATAVIEW_MathVector getMathVector()
	{
		String st = null;
		
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
		    st = br.readLine();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}
					
		return new DATAVIEW_MathVector(st); 
	}
	
	/** Returns a DATAVIEW_MathMatrix object. 
	 * The method converts the content of a file into a {@link DATAVIEW_MathMatrix object}.
	 * 
	 *  @return a reference to an DATAVIEW_MathMatrix object
	 */
	public DATAVIEW_MathMatrix getMathMatrix()
	{
		List<String> lines  = new ArrayList<>(); 
		String st = null;
		BufferedReader br;
		try {	
			br = new BufferedReader(new FileReader(file));
			while((st = br.readLine()) != null) {
				lines.add(st);
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}
		catch (IOException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}
					
		return new DATAVIEW_MathMatrix(lines); 
	}

	/** Returns a String  object. 
	 * The method returns a reference to a string object.
	 *  
	 *  @return a reference to a String object which is the name of a file.
	 */
	public String getFilename()
	{
		return filename;
	}
	
	/** Returns a File  object. 
	 * The method returns a reference to a File object.
	 *  
	 *  @return a reference to a File object
	 */
	public File getFile()
	{
		return file;
	}

	
	/** Returns a DATAVIEW_HashMap object. 
	 * The method converts the content of a file into a {@link DATAVIEW_HashMap object}.
	 *  
	 *  @return a reference to an DATAVIEW_HashMap object
	 */
	DATAVIEW_HashMap getHashMap()
	{
		BufferedReader br = null;
		DATAVIEW_HashMap hm = new DATAVIEW_HashMap();
		String st = null;
		
		try {
			br = new BufferedReader(new FileReader(file));
			while((st = br.readLine()) != null) {
				String [] pair = st.split(":");
				hm.put(pair[0], pair[1]);
			
			} 
			br.close();
		}
		catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Dataview.debugger.logException(e);
		}
				
		return hm;
	}
	
	/** Returns a String object. 
	 * The method converts the content of a file into a {@link String object}.
	 *  
	 *  @return a reference to a String object
	 */
	String getString()
	{
		BufferedReader br = null;
		DATAVIEW_HashMap hm = new DATAVIEW_HashMap();
		String line = null;
		StringBuilder sb = new StringBuilder();
		
		try {
			br = new BufferedReader(new FileReader(file));
			while((line = br.readLine()) != null) {
				sb.append(line+"\n");
			
			} 
			br.close();
		}
		catch (IOException e) {
				e.printStackTrace();
				Dataview.debugger.logException(e);
		}
				
		return sb.toString();
	}

	/** Returns a Integer object. 
	 * The method converts the content of a file into a {@link Integer object}.
	 *  
	 *  @return a reference to a Integer object
	 */
	Integer getInteger()
	{
		BufferedReader br = null;
		String line = null;
		Integer result = null;
		
		try {
			br = new BufferedReader(new FileReader(file));
			line = br.readLine();
			if(line != null) {
				
				result = Integer.valueOf(line); 
			} 
			br.close();
		}
		catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Dataview.debugger.logException(e);
		}
				
		return result;
	}
	

	/** Returns a Double object. 
	 * The method converts the content of a file into a {@link Double object}.
	 *  
	 *  @return a reference to a Double object
	 */
	Double getDouble()
	{
		BufferedReader br = null;
		String line = null;
		Double result = null;
		
		try {
			br = new BufferedReader(new FileReader(file));
			line = br.readLine();
			if(line != null) {
				
				result = Double.valueOf(line); 
			} 
			br.close();
		}
		catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Dataview.debugger.logException(e);
		}
				
		return result;
	}

	
	
	/** Returns a DATAVIEW_Table object. 
	 * The method converts the content of a file into a {@link DATAVIEW_Table object}.
	 *  
	 *  @return a reference to an DATAVIEW_Table object
	 */
	DATAVIEW_Table getTable()
	{
		BufferedReader br = null;
		DATAVIEW_Table tb = null;
		String st = null;
		
		try {
			br = new BufferedReader(new FileReader(file));
			st = br.readLine();
			if(st == null ) {br.close(); return null;}
			
			String [] row = st.split(":");
			tb = new DATAVIEW_Table(row.length);
			tb.appendRow(row); // append the first row
			
			
			while((st = br.readLine()) != null) {
				row  = st.split(":");
				tb.appendRow(row); // append more rows
			
			} 
			br.close();
		}
		catch (IOException e) {
				e.printStackTrace();
				Dataview.debugger.logException(e);
		}
				
		return tb;
	}
	
	/** Returns a String object. 
	 * The method converts the content of a file into a {@link String object}. This method does the same thing as getString().
	 * 
	 *  @return a reference to a String object
	 */
    @Override
    public String toString() 
	{
    	return getString();
	}	
}
 