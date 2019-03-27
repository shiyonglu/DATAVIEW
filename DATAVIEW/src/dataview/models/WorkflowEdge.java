package dataview.models;

/**
 * The workflow edge is used to connect between two tasks with the port id
 * 
 *
 */
public class WorkflowEdge{
	public String srcFilename;
	public String destFilename;
	public Task srcTask;
	public int outputPortIndex;
	public Task destTask;
	public int inputPortIndex;
	
	
	/* connect the outputport of a task to the inputport of another task */
	public WorkflowEdge(Task srcTask, int outputPortIndex, Task destTask, int inputPortIndex)
	{
		this.srcTask = srcTask;
		this.outputPortIndex = outputPortIndex;
		this.destTask = destTask;
		this.inputPortIndex = inputPortIndex;
		this.srcFilename = null;
		this.destFilename = null;
	}
	
	/* connect a file to the inputport of a task */
	public WorkflowEdge(String srcFilename, Task destTask, int inputPortIndex){
		this.srcTask = null;
		this.outputPortIndex = -1;
		this.destTask = destTask;
		this.inputPortIndex = inputPortIndex;
		this.srcFilename = srcFilename;
		this.destFilename = null;
	}

	/* connect the outputport of a task to a file */
	public WorkflowEdge(Task srcTask, int outputPortIndex, String destFilename)
	{
		this.srcTask = srcTask;
		this.outputPortIndex = outputPortIndex;
		this.destTask = null;
		this.inputPortIndex = -1;
		this.srcFilename = null;
		this.destFilename = destFilename;
	}
	
	
	public JSONObject getWorkflowEdgeSpecification()
	{
		JSONObject obj = new JSONObject();
		
		if(srcFilename != null) obj.put("srcFilename",  new JSONValue(srcFilename));
		else obj.put("srcFilename",  new JSONValue(""));
        
		if(srcTask !=null) { 
        	obj.put("srcTaskInstanceID", new JSONValue(srcTask.toString()));
            obj.put("outputPortIndex", new JSONValue(""+outputPortIndex));
        }
		else
		{ 
        	obj.put("srcTaskInstanceID", new JSONValue(""));
            obj.put("outputPortIndex", new JSONValue(""));
        }
			
			
        if(destFilename != null) obj.put("destFilename",  new JSONValue(destFilename));
        else obj.put("destFilename",  new JSONValue(""));
        
        if(destTask !=null) {
        	obj.put("destTaskInstanceID", new JSONValue(destTask.toString()));
            obj.put("inputPortIndex", new JSONValue(""+inputPortIndex));
        }
        else {
        	obj.put("destTaskInstanceID", new JSONValue(""));
            obj.put("inputPortIndex", new JSONValue(""));
        }
		
        return obj;
	}	
}