import dataview.models.*; 

public class Task4 extends Task{
	/*
	 * The constructor will decide how many inputports and how many outputports and the detailed information of each port.
	 */
	
	public Task4()
	{
		super("Task4", "This is a task that implements multiplication. It has two inputports and one outputport.");
		ins = new InputPort[2];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_int, "This is the first number");
		ins[1] = new InputPort("in1", Port.DATAVIEW_int, "This is the second number");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_int, "This is the output");		
	}
	
	public void run()
	{
		// step 1: read from the input ports
		int i0 = ((Integer)ins[0].read()).intValue();
		int i1 =  ((Integer)ins[1].read()).intValue();
		int o0;
		
				
		// step 2: computation of the function
		System.out.println("The first number is " + i0 );
		System.out.println("The second number is " + i1 );
		o0 = i0*i1;
		
		// step 3: write to the output port
		Integer oo = Integer.valueOf(o0);
		outs[0].write(oo);			
	}
}
