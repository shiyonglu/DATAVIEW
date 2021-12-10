package dataview.nntrainers;

import dataview.gpuresourcemanagement.CrossValOnSingleNano_GPUResourceManagement;

import dataview.models.NNWorkflow;

/**
 * The NNWorkfowTrainer_CrossValOnSingleNano is one child class that extends NNWorkflowTrainer
 * The cross validation will be carry on a single Nano GPU node in GPU cluster
 * It specifies the execution infrstructual info, forward the NNWorkflow specification, call the corresponding GPUResourceMangement and kick start the execution
 * @author: Junwen Liu
 * */

public class NNWorkflowTrainer_CrossValOnSingleNano extends NNWorkflowTrainer{
	
	public NNWorkflowTrainer_CrossValOnSingleNano(NNWorkflow w, int numOfBatches, int numOfEpochs) {
		super(w, numOfBatches, numOfEpochs);
	}
	
	public String train()
	{
		String returnValue = "";
		
		CrossValOnSingleNano_GPUResourceManagement CrossValOnSingleNano= new CrossValOnSingleNano_GPUResourceManagement(specification);
		returnValue = CrossValOnSingleNano.execute();
		
		return returnValue;
	}
}
