import java.io.*;
import java.util.HashMap;
import java.util.Map;

import dataview.models.*;


/**
 *  This is the Linear layer which extends NNTask class
 * It includes two class constructor as the web version requires one constructor without any input parameter
 * @author Junwen Liu
 * */

public class Linear extends NNTask{
	
	public Linear ()
	{
		super("LinearLayer", "This will be a linear layer");
		ins = new InputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_String, "This is the input of this layer");
		outs = new OutputPort[1];
		outs[0] = new OutputPort("out0", Port.DATAVIEW_String, "This is the output of this layer");				
	}
	

	public Linear (int in_features, int out_features)
	{
		super("LinearLayer", "This will be a linear layer", in_features, out_features);
		ins = new InputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_String, "This is the input of this layer");
		outs = new OutputPort[1];
		outs[0] = new OutputPort("out0", Port.DATAVIEW_String, "This is the output of this layer");				
	}
	
	
	public void run()
	{
	}
		
}