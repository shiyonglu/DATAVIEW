package dataview.models;
import java.io.*;
import java.util.*;

/** This class supports the date type of DATAVIEW_HashMap, which is similar to a HashMap <String, String> in Java but with additional
 * methods that are DATAVIEW-specific.  
 *  
 * 
 */
public class DATAVIEW_HashMap extends HashMap <String, String>{
	public DATAVIEW_HashMap()
	{
		super();
	}

	/** The constructor constructs a DATAVIEW_HashMap object using a given Java HashMap object.
	 *  
	 * @param hmii a reference to a HasmMap<Integer, Integer> object
	 */
	public DATAVIEW_HashMap(HashMap<Integer, Integer> hmii){
		for(Integer k: hmii.keySet()) {
		   this.put(k.toString(), hmii.get(k).toString());
		}		
	}

	/** Returns a string representation of the DATAVIEW_HashMap object. 
	 *  
	 *  @return a string representation of the DATAVIEW_HashMap object.
	 * 
	 */
		
    @Override
    public String toString() 
	{
		String str = "{";
		String separator = "";
		
		for(String key: this.keySet()) {
			str = str + separator +  key + ":" + this.get(key);
			separator = ", "; 
		}

		str = str + "}";
		return str;
	}		
}
