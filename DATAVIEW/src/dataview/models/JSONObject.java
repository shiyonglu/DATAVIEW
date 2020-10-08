package dataview.models;
import java.util.*;


public class JSONObject extends  LinkedHashMap<String, JSONValue>
{
    public JSONObject()
    {
       super();
    }
    
    
      
	@Override
	public String toString() {
		return toString(0);
	}
	
	
    public String toString(int indent) 
  	{
    	String str = "";
    	
      	String indentstr="";
      	for(int i=0; i<indent; i++) indentstr+=" ";

		str = str+indentstr+"{";
		JSONValue value = null;
		String separator="";
		for(String key: this.keySet()) {
			str=str+separator+"\n";
			separator=",";
		    value = this.get(key);
		    if(value.getValueType() == 0)
		    	str = str+indentstr+"    \""+key+"\": " + value.toString(indent+8); // indent the value
		    else if(value.getValueType() !=0  && value.isEmpty())
		    	str = str+indentstr+"    \""+key+"\": " + value.toString(indent+8); // indent the value
		    else 
		    	str = str+indentstr+"    \""+key+"\":\n" + value.toString(indent+8); // next line
		}
		str = str+"\n"+indentstr+"}";		
        return str;		
  	} 
    
   
    
   
}
