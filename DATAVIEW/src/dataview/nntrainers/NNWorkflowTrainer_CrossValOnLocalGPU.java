package dataview.nntrainers;

import java.io.File;
import dataview.models.NNWorkflow;

import dataview.gpuresourcemanagement.CrossValOnLocalGPU_GPUResourceManagement;

/**
 * The NNWorkfowTrainer_CrossValOnLocalGPU is one child class that extends NNWorkflowTrainer
 * The cross validation will be carried on local GPU in a sequential manner
 * It specifies the execution infrstructual info, forward the NNWorkflow specification, call the corresponding GPUResourceMangement and kick start the execution
 * API_Repo has been tweaked to be working on both webbench and java api
 * @author: Junwen Liu
 * */

public class NNWorkflowTrainer_CrossValOnLocalGPU extends NNWorkflowTrainer{
	
	private String API_Repo = this.fileLocation + File.separator + "TrainerDLLs"+ File.separator;
	
	public NNWorkflowTrainer_CrossValOnLocalGPU(NNWorkflow w, int numOfBatches, int numOfEpochs) {
		super(w, numOfBatches, numOfEpochs);
	}
	
	public NNWorkflowTrainer_CrossValOnLocalGPU(NNWorkflow w, String location, int numOfBatches, int numOfEpochs) {
		super(w, location, numOfBatches, numOfEpochs);
	}
	
	public String train()
	{
		
		String returnValue = "";
		
		CrossValOnLocalGPU_GPUResourceManagement localGPU= new CrossValOnLocalGPU_GPUResourceManagement(API_Repo, specification);
		returnValue = localGPU.execute();
		
		return returnValue;
	}
}
