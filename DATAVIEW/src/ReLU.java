import java.io.*;
import java.util.HashMap;
import java.util.Map;

import dataview.models.*;


/**
 *  This is the RelLU layer which extends NNTask class
 * @author Junwen Liu
 * */
public class ReLU extends NNTask{
	
	public ReLU ()
	{
		super("ReLU", "This will be a ReLU Layer");
		ins = new InputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_String, "This is the input of this layer");
		outs = new OutputPort[1];
		outs[0] = new OutputPort("out0", Port.DATAVIEW_String, "This is the output of this layer");				
	}
	
	
	public void run()
	{
	}
		
}