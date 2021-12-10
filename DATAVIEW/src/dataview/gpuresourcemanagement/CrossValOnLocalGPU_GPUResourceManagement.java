package dataview.gpuresourcemanagement;

/**
 * The CrossValOnLocalGPU_GPUResourceManagement is one of child classes that implement the GPUResourceManagement class
 * This class will implement the execute method and call respective GPU service
 * @author: Junwen Liu
 * */

public class CrossValOnLocalGPU_GPUResourceManagement extends GPUResourceManagement {
	public CrossValOnLocalGPU_GPUResourceManagement(String API_Repo, String specification) {
		super(API_Repo, specification);
	}
	
	public String execute(){
		String returnValue = "";
		returnValue = callLocalGPUService("main_localCrossVal.dll");
		return returnValue;
	}
}
