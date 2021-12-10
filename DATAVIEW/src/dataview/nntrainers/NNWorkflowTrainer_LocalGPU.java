package dataview.nntrainers;

import dataview.models.NNWorkflow;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import dataview.gpuresourcemanagement.LocalGPU_GPUResourceManagement;

/**
 * The NNWorkfowTrainer_LocalGPU is one child class that extends NNWorkflowTrainer
 * The regular NNWorkflow train and test will be carry on the local GPU
 * API_Repo has been tweak to be working on both webbench and java api
 * It specifies the execution infrstructual info, forward the NNWorkflow specification, call the corresponding GPUResourceMangement and kick start the execution
 * @author: Junwen Liu
 * */

public class NNWorkflowTrainer_LocalGPU extends NNWorkflowTrainer{

	private String API_Repo = this.fileLocation + File.separator + "TrainerDLLs"+ File.separator;
	
	public NNWorkflowTrainer_LocalGPU(NNWorkflow w, int numOfBatches, int numOfEpochs) {
		super(w, numOfBatches, numOfEpochs);
	}
	
	public NNWorkflowTrainer_LocalGPU(NNWorkflow w, String location, int numOfBatches, int numOfEpochs) {
		super(w, location, numOfBatches, numOfEpochs);
	}
	
	public String train()
	{
		
		String returnValue = "";
		
		LocalGPU_GPUResourceManagement localGPU= new LocalGPU_GPUResourceManagement("maintest.dll", API_Repo, specification);
		returnValue = localGPU.execute();
		
		return returnValue;
	}
}
