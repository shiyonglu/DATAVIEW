import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;

public class T1 extends Task{
	/*
	 * The constructor will decide how many inputports and how many outputports and the detailed information of each port.
	 */
	
	public T1()
	{
		super("T1", "This is a task that implements add. It has one inputport and three outputports. It simplies add 0 to the first number "
				+ "and then copy the result to the three outputports.");
		ins = new InputPort[1];
		outs = new OutputPort[3];
		ins[0] = new InputPort("in0", Port.DATAVIEW_int, "This is the first number");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_int, "This is the first output");
		outs[1] = new OutputPort("out1", Port.DATAVIEW_int, "This is the second output");	
		outs[2] = new OutputPort("out2", Port.DATAVIEW_int, "This is the third output");	
	}
	
	
	
	public void run()
	{
		// step 1: read from the input ports
		int i0 = ((Integer)ins[0].read()).intValue();
		int i1 = 0;
		int o0;
		
				
		// step 2: computation of the function
		System.out.println("The first number is " + i0 );
		System.out.println("The second number is " + i1 );
		o0 = i0+i1;
		
		// step 3: write to the output port
		Integer oo = Integer.valueOf(o0);
		// outs[0].write(oo);
		write(0, oo);
		System.out.println("The first port number is finished");
		write(1,oo);
		System.out.println("The second port number is finished");
		write(2,oo);
		System.out.println("The third port number is finished");
	}
}
