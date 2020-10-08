import dataview.models.*;

public class T2 extends Task{
	/*
	 * The constructor will decide how many inputports and how many outputports and the detailed information of each port.
	 */
	
	public T2()
	{
		super("T2", "second task in dummy workflow");
		ins = new InputPort[1];
		outs = new OutputPort[2];
		ins[0] = new InputPort("in0", Port.DATAVIEW_int, "This is the first number");
		//ins[1] = new InputPort("in1", Port.DATAVIEW_int, "This is the second number");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_int, "This is the first output");	
		outs[1] = new OutputPort("out1", Port.DATAVIEW_int, "This is the second output");	
	}
	
	
	
	public void run()
	{
		
	}
}
