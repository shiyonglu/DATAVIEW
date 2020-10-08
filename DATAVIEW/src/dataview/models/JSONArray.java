package dataview.models;
import java.util.*;
/**
 *  JSONArray is a list of JSONValues, with some basic methods from ArrayList used in this project.
 */
public class JSONArray 
{
	 private List<JSONValue> array;

	 public JSONArray()
	 {
		 this.array = new ArrayList<JSONValue>();
	 }
	 
	 public JSONValue get(int i)
	 {
	    return array.get(i);
	 }
	 
	 public void add(JSONValue value)
	 {
	    array.add(value);
	 }

	 
	 public boolean isEmpty()
	 {
		 return array.isEmpty();
	 }
	 
	 public int size()
	 {
		 return array.size();
	 }
	 
		@Override
		public String toString() {
			//System.out.println("Get an array:\n"+toString(0));
			return toString(0);
		}
		
	    public String toString(int indent) 
	  	{
	    	String str = "";
	    	
	      	String indentstr="";
	      	for(int i=0; i<indent; i++) indentstr+=" ";
	 
	    	if(array.isEmpty()) return "[]"; // no next line either
		
	    	if(array.get(0).getValueType() == 0) { // each element is a string 
		    	str = indentstr+"[";
		    	String separator = "";
		    	for(JSONValue value: array) {
		    		str=str+separator;
		    		separator=",";
		    		str = str+value.toString();
		    	}			
		    	str = str+"]";
		    	
		    	return str;
	    	}
	    	
	    	// each element is not a string
	    	str = indentstr+"[\n";
	    	String separator = "";
	    	for(JSONValue value: array) {
	    		str=str+separator;
	    		separator=",\n";
	    		str = str+value.toString(indent+4); // indent the value of each array element
	    	}			
	    	str = str+"\n"+indentstr+"]";
	    	
	    	return str;

	  	}
}