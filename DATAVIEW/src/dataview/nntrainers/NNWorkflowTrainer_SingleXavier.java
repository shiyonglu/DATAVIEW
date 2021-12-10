package dataview.nntrainers;

import dataview.gpuresourcemanagement.SingleXavier_GPUResourceManagement;
import dataview.models.NNWorkflow;

/**
 * The NNWorkfowTrainer_SingleXavier is one child class that extends NNWorkflowTrainer
 * The regular NNWorkflow train and test will be carry on the one of Xavier GPU node in GPU cluster
 * It specifies the execution infrstructual info, forward the NNWorkflow specification, call the corresponding GPUResourceMangement and kick start the execution
 * @author: Junwen Liu
 * */

public class NNWorkflowTrainer_SingleXavier extends NNWorkflowTrainer{
	
	public NNWorkflowTrainer_SingleXavier(NNWorkflow w, int numOfBatches, int numOfEpochs) {
		super(w, numOfBatches, numOfEpochs);
	}
	
	public String train()
	{
		String returnValue = "";
		
		SingleXavier_GPUResourceManagement SingleXavier= new SingleXavier_GPUResourceManagement(specification);
		returnValue = SingleXavier.execute();
		
		return returnValue;
	}
}
