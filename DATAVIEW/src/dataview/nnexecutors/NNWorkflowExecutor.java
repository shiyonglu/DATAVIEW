package dataview.nnexecutors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONException;
import org.json.JSONObject;

public class NNWorkflowExecutor {
	protected JSONObject configuration;
	protected String NNWorkflowExecutor_type;
	public NNWorkflowExecutor_base newClass;
	
	public NNWorkflowExecutor(String inputFileName, String jsonFileName, String location) {
		this.configuration = fetchConfiguration();
		try {
			this.NNWorkflowExecutor_type = configuration.getJSONObject("NNExecutor").getString("type");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		switch(this.NNWorkflowExecutor_type) {
		case "LocalGPU":
			newClass = new NNWorkflowExecutor_LocalGPU(inputFileName, jsonFileName, location);
			break;
		default:
			newClass = new NNWorkflowExecutor_LocalGPU(inputFileName, jsonFileName, location);
			break;
		}
	}
	
	public JSONObject fetchConfiguration() {
		String configure = ""; 
		JSONObject configureJson; 
		String configRepo = System.getProperty("user.dir") + File.separator + "WebContent";
		Path path  = Paths.get(configRepo);  
		try {
			configure = new String(Files.readAllBytes(Paths.get(path+ File.separator+"config.json")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		JSONObject obj = null;
		try {
			obj = new JSONObject(configure);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return obj;
	}
}


