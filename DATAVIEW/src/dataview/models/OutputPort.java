package dataview.models;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 * The OutputPort class is extends from Port and has its own write method to a file.
 */
public class OutputPort extends Port{

	public OutputPort(String portname, int porttype, String description)
	{
		super(portname, porttype, description);
	}
	
	public void write(Object o)
	{
	    BufferedWriter writer;
			
		// write the string representation of the object to the output file
		try {
			writer = new BufferedWriter(new FileWriter(location));
			writer.write(o.toString());	     			
		    writer.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} // end of write()
} // end of class
