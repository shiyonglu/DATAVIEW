package dataview.models;


/**
 * 
 * The InputPort class is extends from Port and has its own read method, which return an object of the corresponding type.
 *
 */
public class InputPort extends Port{

	public InputPort(String portname, int porttype, String description)
	{
		super(portname, porttype, description);
	}
	


	
	public Object read() {

		 DATAVIEW_BigFile f = new DATAVIEW_BigFile(this.getFileName());
		 		 
		 	      
	     if(porttype ==  Port.DATAVIEW_int)
	    	 return f.getInteger();
	    
	     if(porttype ==  Port.DATAVIEW_double)
	    	 return  f.getDouble();
	     
	     if(porttype == Port.DATAVIEW_String)
	    	  return f.getString();	     
	     	     
	     if(porttype == Port.DATAVIEW_HashMap)
				return f.getHashMap();						

	     if(porttype == Port.DATAVIEW_MathVector)
				return f.getMathVector();
				
	     if(porttype == Port.DATAVIEW_Table)
				return f.getTable();			    							    	 
	     
	     if(porttype == Port.DATAVIEW_BigFile)
	    	 return f;
	    
	     if(porttype == Port.DATAVIEW_MathMatrix)
	    	 return f.getMathMatrix();
	        	 
	     Dataview.debugger.logErrorMessage("Inputport type: " + porttype + " is not yet supported in this DATAVIEW release.");
	     return null;
	}
}
