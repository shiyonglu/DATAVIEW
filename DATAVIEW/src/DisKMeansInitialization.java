/* 
 * We will assign a random cluster number k (0, ..., K-1) to each point in the input file
 *
 */
import java.util.*;
import dataview.models.*;

public class DisKMeansInitialization extends Task{
	private int K = DisKMeansWorkflow.K;
	private int M = DisKMeansWorkflow.M;

	public DisKMeansInitialization()
	{
		super("DisKMeansInitialization", "Assigning cluster no from 0 at the beginning of of each line");

		ins = new InputPort[1];
		outs = new OutputPort[M];
		ins[0] = new InputPort("in0", Port.DATAVIEW_String, "This is the input file which has no cluster number.");
		for (int i = 0; i < M; i++) {
			outs[i] = new OutputPort("out0", Port.DATAVIEW_String, "this is the output file which has cluster numbers, starting from 0 at the beginning");
		}
	}

	public void run() {
		String input0 = (String) ins[0].read();
		Dataview.debugger.logSuccessfulMessage("here is the input0" + input0);
		
		// Assigning cluster number randomly starting from zero at the beginning of the line.
		String lines[] = input0.split("\\r?\\n");
		Random random = new Random();
		String[] outputs = new String[M];
		int index = 0;
		String output = "";
		int division = lines.length / M;
		int count = 0;
		for (int i = 0; i < lines.length; i++) {
			count++;
			output += random.nextInt(K) + " " + lines[i];
			if (i != lines.length - 1) output += "\n";
			if (count == division || i == lines.length) {
				outputs[index++] = output;
				output = "";
				count = 0;
			}
		}
		
		// writing file
		for (int i = 0; i < outs.length; i++)
			outs[i].write(outputs[i]); 
	}
}
