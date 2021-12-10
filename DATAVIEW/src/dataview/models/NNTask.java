package dataview.models;

/**
 * This class defines the NNtask model, with each task has one input port and one output ports. 
 * @author: Junwen Liu
 */
public abstract class NNTask extends Task {
	
	public NNTask(String taskName, String taskDescription) {
		super(taskName, taskDescription);
	}
	
	public NNTask(String taskName, String taskDescription, int in_features, int out_features)
	{
		super(taskName, taskDescription);
		this.in_features = in_features;
		this.out_features = out_features;
	}
	
  
	public JSONObject getTaskSpecification()
	{
		JSONObject obj = new JSONObject();
		obj.put("taskName", new JSONValue(taskName));
		obj.put("tastDescription", new JSONValue(taskDescription));

		// add all input ports
		JSONArray inputlib = new JSONArray();
		for(int i=0; i< ins.length; i++) {
			JSONObject inp = ins[i].getPortSpecification();
			inputlib.add(new JSONValue(inp));
		}
		obj.put("inputPorts", new JSONValue(inputlib));
		
		JSONArray outputlib = new JSONArray();
		for(int i=0; i< outs.length; i++) {
			JSONObject outp = outs[i].getPortSpecification();
			outputlib.add(new JSONValue(outp));
		}
		obj.put("outputPorts", new JSONValue(outputlib));

		//add in_features and out_features
		obj.put("inFeatures", new JSONValue(String.valueOf(in_features)));
		obj.put("outFeatures", new JSONValue(String.valueOf(out_features)));
		
		
		
		return obj;		
	}
}
