package dataview.gpuresourcemanagement;

/**
 * The LocalGPU_GPUResourceManagement is one of child classes that implement the GPUResourceManagement class
 * This class will implement the execute method and call respective services
 * @author: Junwen Liu
 * */

public class LocalGPU_GPUResourceManagement extends GPUResourceManagement {
	protected String APIName;
	
	public LocalGPU_GPUResourceManagement(String APIName, String API_Repo, String specification) {
		super(API_Repo, specification);
		this.APIName = APIName;
	}
	
	public String execute(){
		String returnValue = "";
		returnValue = callLocalGPUService(APIName);
		return returnValue;
	}
}
