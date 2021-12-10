package dataview.nntrainers;

import dataview.gpuresourcemanagement.SingleNano_GPUResourceManagement;
import dataview.models.NNWorkflow;

/**
 * The NNWorkfowTrainer_SingleNano is one child class that extends NNWorkflowTrainer
 * The regular NNWorkflow train and test will be carry on the one of Nano GPU node in GPU cluster
 * It specifies the execution infrstructual info, forward the NNWorkflow specification, call the corresponding GPUResourceMangement and kick start the execution
 * @author: Junwen Liu
 * */

public class NNWorkflowTrainer_SingleNano extends NNWorkflowTrainer{
	
	public NNWorkflowTrainer_SingleNano(NNWorkflow w, int numOfBatches, int numOfEpochs) {
		super(w, numOfBatches, numOfEpochs);
	}
	
	public String train()
	{
		String returnValue = "";
		
		SingleNano_GPUResourceManagement SingleNano= new SingleNano_GPUResourceManagement(specification);
		returnValue =SingleNano.execute();
		
		return returnValue;
	}
}
