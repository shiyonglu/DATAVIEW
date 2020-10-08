package dataview.models;


/* a JSONValue is either a String, or a JSONObject, or an array of JSONValue */
public class JSONValue {
	final static String indent = "    ";
	private String str;
	private JSONObject obj;
	private JSONArray array;
	private int valueType;
	
	public JSONValue(String str) {
		this.str = str;
		this.valueType = 0;
	}
	
	public JSONValue(JSONObject obj){
		this.obj = obj;
		this.valueType = 1;
	}
	
	public JSONValue(JSONArray array){
		this.array = array;
		this.valueType = 2;
	}
	
	public int getValueType()
	{
	   return valueType;
	}
	
	public JSONArray toJSONArray()
	{
		return array;
	}

	public JSONObject toJSONObject()
	{
		return obj;
	}
	
	
	
	public boolean isEmpty() { //either empty string, empty JSONOjbect, or empty JSONArray
		if(valueType == 0 && str != null && str.equalsIgnoreCase("")) return true; 
		if(valueType == 1 && obj !=null && obj.isEmpty()) return true;
		if(valueType == 2 && array != null && array.isEmpty()) return true;
		
		return false;		
	}
	
	@Override
	public String toString() {
		return toString(0);
	}
	
    public String toString(int indent) 
	{
				
		if(valueType == 0)              // it is a string, no indent, no next line
		    return "\""+str+"\"";       // no next line
		else if(valueType ==1)         // it is a JSON object
		{
			return obj.toString(indent);			
		} 
		else if(valueType == 2) { // an array
			return array.toString(indent);
		}		
		else {
			Dataview.debugger.logErrorMessage("Invalid JSONValue type.");
		}
	
		return str;		
	}
	
	
}
