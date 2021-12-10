package dataview.nntrainers;

import dataview.gpuresourcemanagement.CrossValOnSingleXavier_GPUResourceManagement;

import dataview.models.NNWorkflow;

/**
 * The NNWorkfowTrainer_CrossValOnSingleXavier is one child class that extends NNWorkflowTrainer
 * The cross validation will be carry on a single Xavier GPU node in GPU cluster
 * It specifies the execution infrstructual info, forward the NNWorkflow specification, call the corresponding GPUResourceMangement and kick start the execution
 * @author: Junwen Liu
 * */

public class NNWorkflowTrainer_CrossValOnSingleXavier extends NNWorkflowTrainer{
	
	public NNWorkflowTrainer_CrossValOnSingleXavier(NNWorkflow w, int numOfBatches, int numOfEpochs) {
		super(w, numOfBatches, numOfEpochs);
	}
	
	public String train()
	{
		String returnValue = "";
		
		CrossValOnSingleXavier_GPUResourceManagement CrossValOnSingleXavier= new CrossValOnSingleXavier_GPUResourceManagement(specification);
		returnValue = CrossValOnSingleXavier.execute();
		
		return returnValue;
	}
}
