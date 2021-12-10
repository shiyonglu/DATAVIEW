package dataview.gpuresourcemanagement;

import org.json.JSONException;

/**
 * The CrossValOnGPUCluster_GPUResourceManagement is one of child classes that implement the GPUResourceManagement class
 * This class will implement the execute method and call respective service
 * @author: Junwen Liu
 * */

public class CrossValOnGPUCluster_GPUResourceManagement extends GPUResourceManagement {
	public CrossValOnGPUCluster_GPUResourceManagement(String specification) {
		super(specification);
	}
	
	public String execute(){
		String returnValue = "";
		String IPAddress = "";
		try {
			IPAddress = configuration.getJSONObject("GPUCluster").getString("masterNode");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		returnValue = callGPUClusterService(IPAddress, "cd /home/jetson/Documents/crossVal_cudaNN_new && time mpiexec --hostfile clusterfile5 ./cudaMPI ");
		return returnValue;
	}
}
