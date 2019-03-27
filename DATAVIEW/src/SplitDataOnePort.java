import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;

public class SplitDataOnePort extends Task{
	/*
	 * The constructor will decide how many inputports and how many outputports and the detailed information of each port.
	 */
	
	public SplitDataOnePort()
	{
		super("SplitDataOnePort", "This is a task that implements partition the dataset. It has one inputports and two outputports.");
		ins = new InputPort[1];
		outs = new OutputPort[2];
		ins[0] = new InputPort("in0", Port.DATAVIEW_String, "This is the first number");
		//ins[1] = new InputPort("2in", "Integer", "This is the second number");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_String, "This is the output");	
		outs[1] = new OutputPort("out1", Port.DATAVIEW_String, "This is the output");
	}
	
	public void run()
	{
		// step 1: read from the input ports
		String input0 = (String) ins[0].read();
		Double b = 0.3;
		
		// step 2: computation of the function
		String content = "@relation dataset\n@attribute age numeric\n"
				+ "@attribute heartRate numeric\n@attribute isObesity {No, Yes}\n@attribute isSmoke {No, Yes}\n"
				+ "@attribute glucose numeric\n@data\n";
		StringBuilder stringBuilderTrain = new StringBuilder();
		StringBuilder stringBuilderTest = new StringBuilder();
		stringBuilderTrain.append(content);
		stringBuilderTest.append(content);
		
		
		String lines[] = input0.split("\\r?\\n");
		
		int numofline = lines.length;
		for(int i = 0; i < numofline; i++) {
			if (i < numofline * b) {
				stringBuilderTrain.append(lines[i] + "\n");
			} else {
				stringBuilderTest.append(lines[i] + "\n");
			}
		}
		
		String output0 = stringBuilderTrain.toString();
		String output1 = stringBuilderTest.toString();
		
		
		
	
		// step 3: write to the output port
		outs[0].write(output0);	
		outs[1].write(output1);
	}
	
}
