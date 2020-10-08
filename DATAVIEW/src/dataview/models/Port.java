package dataview.models;


public class Port {
    /* port types that are supported by DATAVIEW */	
	public static final int DATAVIEW_int = 0;
	public static final  int DATAVIEW_double = 1;
	public static final  int DATAVIEW_String = 2;
	public static final  int DATAVIEW_BigFile = 3;   // we  support big data computation
	public static final  int DATAVIEW_MathVector = 4; 
	public static final  int DATAVIEW_MathMatrix = 5; 
	public static final  int DATAVIEW_HashMap = 6; // Key and value pairs, both of them of type String
	public static final  int DATAVIEW_Table = 7; // a table of elements of type String */
	
	
	
	public String portname;
	public int porttype;
	public String description;
	public String location; // this points to the file that contains the data for this inputport
	
	public Port()
	{
	}
	
	
	public Port(String portname, int porttype, String description)
	{
		this.portname = portname;
		this.porttype = porttype;
		this.description = description;		
	}
	
	/*
	 * setLocation is called by a workflow executor, who knows the path and file name that contains the data for this input port. 
	 * This file is the result of the execution of an upstream task or it is the input file of a workflow. 
	 * This file can be also moved from another virtual machine if the upstream task is executed in a different VM. 
	 * The task executor will determine the life cycle of this file: when to create it, and when to delete it to save 
	 * storage of the VM.
	 */
	public void setLocation(String pathfile)
	{
		this.location = pathfile;
	}
	

	public String getFileName()
	{
		System.out.println( "----"+this.location);
		return this.location;
	}
	
	public int getPortType()
	{
		return porttype;
	}
	
	public String getPortTypeName()
	{
		switch(porttype)
		{
			case 0: return "DATAVIEW_int";
			case 1: return "DATAVIEW_double";
			case 2: return "DATAVIEW_String";
			case 3: return "DATAVIEW_BigFile";
			case 4: return "DATAVIEW_MathVector";
			case 5: return "DATAVIEW_MathMatrix";
			case 6: return "DATAVIEW_MathMatrix";
			case 7: return "DATAVIEW_Table";
			default: return "DATAVIEW_int";
		}
					
	}

	
	
	public JSONObject getPortSpecification()
	{
		JSONObject obj = new JSONObject();

		
		obj.put("portname",  new JSONValue(portname));
		obj.put("porttype",  new JSONValue(getPortTypeName()));
		obj.put("description",  new JSONValue(description));
				
		return obj;
	}
}
