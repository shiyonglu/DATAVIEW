
import java.io.*;

import dataview.models.*;
import dataview.nnexecutors.NNWorkflowExecutor;
import dataview.nnexecutors.NNWorkflowExecutor_LocalGPU;


public class NNExecutor  extends Task{

	protected String fileLocation;
	
	public NNExecutor ()
	{
		super("NNExecutor", "The NNExecutor will reuse the trained models");
		ins = new InputPort[2];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_BigFile, "This is the input JSON file");
		ins[1] = new InputPort("in1", Port.DATAVIEW_BigFile, "This is the input dataset");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_BigFile, "This is the output");		
	}
	
	public NNExecutor (String fileLocation)
	{
		super("NNExecutor", "The NNExecutor will reuse the trained models");
		this.fileLocation = fileLocation;
		ins = new InputPort[2];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_BigFile, "This is the input JSON file");
		ins[1] = new InputPort("in1", Port.DATAVIEW_BigFile, "This is the input dataset");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_BigFile, "This is the output");		
	}
	
	public void run()
	{
	    // step 1: read    
		String inputFileName = ins[0].getFileName();
		String inputDataset = ins[1].getFileName();
		
		NNWorkflowExecutor executor = new NNWorkflowExecutor(inputDataset, inputFileName, fileLocation);
		
		String result = executor.newClass.Execute();
    	
	    // step 3: write to the output
	    String outputFileName = outs[0].getFileName();
		try {
			PrintWriter out = new PrintWriter(outputFileName);
			out.println(result);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

	
	
