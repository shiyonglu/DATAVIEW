package dataview.nnexecutors;

import java.io.File;

import dataview.gpuresourcemanagement.LocalGPU_GPUResourceManagement;

public class NNWorkflowExecutor_LocalGPU extends NNWorkflowExecutor_base {
	
	private String API_Repo = this.fileLocation + File.separator + "ExecutorDLLs"+ File.separator;


	public NNWorkflowExecutor_LocalGPU(String inputFileName, String jsonFileName) {
		super(inputFileName, jsonFileName);
		// TODO Auto-generated constructor stub
	}
	
	public NNWorkflowExecutor_LocalGPU(String inputFileName, String jsonFileName, String location) {
		super(inputFileName, jsonFileName, location);
		// TODO Auto-generated constructor stub
	}

	public String Execute()
	{
		String returnValue = "";
		LocalGPU_GPUResourceManagement localGPUExecutor= new LocalGPU_GPUResourceManagement("nnExecutor.dll", API_Repo, specification);
		returnValue = localGPUExecutor.execute();
		return returnValue;
	}

}

