package dataview.gpuresourcemanagement;

import org.json.JSONException;

/**
 * The SingleXavier_GPUResourceManagement is one of child classes that implement the GPUResourceManagement class
 * This class will implement the execute method and call respective services
 * @author: Junwen Liu
 * */

public class SingleXavier_GPUResourceManagement extends GPUResourceManagement {
	public SingleXavier_GPUResourceManagement(String specification) {
		super(specification);
	}
	
	public String execute(){
		String returnValue = "";
		String IPAddress = "";
		try {
			IPAddress = configuration.getJSONObject("GPUCluster").getString("singleXavier");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		returnValue = callGPUClusterService(IPAddress, "cd /home/jetson/Documents/cudaNN_SingleNode && time ./cudaMPI ");
		return returnValue;
	}
}
