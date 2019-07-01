package dataview.models;

/**
 * The workflow edge is used to connect between two tasks with the port id
 * 
 *
 *  Log: 5/20/2019.  Changed the workflow model. 
 *  
 *  Changed all the workflow inputs and outputs to allow arbitrary Java objects. Previously, we only allow 
 *  files. Such extension allows to change an existing workflow with another intputs and outputs, which essentially allows to 
 *  call the same workflow with different inputs or parameters easily. 
 *  
 */
public class WorkflowEdge{
	public int edgeType; // 0, input edge; 1: intermediate edge; 2: output edge
	public int winIndex = -1;
	public int woutIndex = -1;
	
	public Task srcTask;
	public int outputPortIndex;
	public Task destTask;
	public int inputPortIndex;
	
	
	/* 
	 * Create a workflowEdge of type 1
	 * 
	 * connect the outputport of a task to the inputport of another task 
	 * 
	 * */
	public WorkflowEdge(Task srcTask, int outputPortIndex, Task destTask, int inputPortIndex)
	{
		this.edgeType = 1;
		
		this.srcTask = srcTask;
		this.outputPortIndex = outputPortIndex;
		this.destTask = destTask;
		this.inputPortIndex = inputPortIndex;
	}
	
	/* connect a file to the inputport of a task */
	public WorkflowEdge(int winIndex, Task destTask, int inputPortIndex){
		this.edgeType = 0;

		this.winIndex = winIndex;
		this.destTask = destTask;
		this.inputPortIndex = inputPortIndex;
	}

	/* 
	 * Create a workflowEdge of type 2
	 * 
	 * connect the outputport of a task to a workflow output 
	 * 
	 * */
	public WorkflowEdge(Task srcTask, int outputPortIndex, int woutIndex)
	{
		this.edgeType = 2;
		
		this.srcTask = srcTask;
		this.outputPortIndex = outputPortIndex;
		this.woutIndex = woutIndex;
	}
	
	
	public JSONObject getWorkflowEdgeSpecification()
	{
		JSONObject obj = new JSONObject();
		
		if(edgeType == 0) {
			obj.put("win",  new JSONValue(winIndex+""));
    	    obj.put("destTaskInstanceID", new JSONValue(destTask.toString()));
            obj.put("inputPortIndex", new JSONValue(""+inputPortIndex));
        }
		else if(edgeType == 1)
		{
			obj.put("srcTaskInstanceID", new JSONValue(srcTask.toString()));
            obj.put("outputPortIndex", new JSONValue(""+outputPortIndex));
       	    obj.put("destTaskInstanceID", new JSONValue(destTask.toString()));
            obj.put("inputPortIndex", new JSONValue(""+inputPortIndex));
		}
        else
		{ 
        	obj.put("srcTaskInstanceID", new JSONValue(""));
            obj.put("outputPortIndex", new JSONValue(""));
			obj.put("wout",  new JSONValue(woutIndex+""));

        }
							
        return obj;
	}	
}