package dataview.nntrainers;

/**
 * The NNWorkfowTrainer_CrossValOnGPUCluster is one child class that extends NNWorkflowTrainer
 * The cross validation will be carried on the GPU cluster in a map and reduce manner
 * It specifies the execution infrsstructual info, forward the NNWorkflow specification, call the corresponding GPUResourceMangement and kick start the execution
 * @author: Junwen Liu
 * */

import dataview.gpuresourcemanagement.CrossValOnGPUCluster_GPUResourceManagement;
import dataview.models.NNWorkflow;

public class NNWorkflowTrainer_CrossValOnGPUCluster extends NNWorkflowTrainer{
	
	public NNWorkflowTrainer_CrossValOnGPUCluster(NNWorkflow w, int numOfBatches, int numOfEpochs) {
		super(w, numOfBatches, numOfEpochs);
	}
	
	public String train()
	{
		
		String returnValue = "";
		
		CrossValOnGPUCluster_GPUResourceManagement CrossValOnGPUCluster= new CrossValOnGPUCluster_GPUResourceManagement(specification);
		returnValue = CrossValOnGPUCluster.execute();
		
		return returnValue;
		
	}
}
