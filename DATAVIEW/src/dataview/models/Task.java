package dataview.models;

/** 
 *
 * This class defines the task model, with each task has a finite number of input ports and output ports. 
 * Each port will have a predefined data type. 
 * 
 * Subscribe to Youtube DATAVIEW channel for more information:
 * https://www.youtube.com/channel/UCrIEBUmju-NMKFMlFbsKBYw 
 * 
 */
public abstract class Task {
	public String taskName;
	public String taskDescription;
	public InputPort [] ins;
	public OutputPort [] outs;
	
	public Task()
	{
	}
	
	public Task(String taskName, String taskDescription)
	{
		this.taskName = taskName;
		this.taskDescription = taskDescription;
	}
	
    /** Return a reference to an object of the given port type.
     *  
     * @param inputport_index the index of the given input port
     * @return a reference to an object that is read from the given input port 
     */
	public Object read(int inputport_index)
	{
		if(inputport_index >= 0 && inputport_index < ins.length) {
			return ins[inputport_index];	
		}
		else {
			Dataview.debugger.logFalseCondition("inputport_index must be greater than 0 and smaller than "+ins.length+", the number of input ports.", inputport_index >= 0 && inputport_index < ins.length);
			return  null;
		}				
	}

    /** Write an object to a given output port. 
     *  
     * @param outputport_index the index of the given output port
     * @param o  the object that is to be written to the given output port  
     */

	public void write(int outputport_index, Object o)
	{
		if(outputport_index >= 0 && outputport_index < outs.length) {
			outs[outputport_index].write(o);	
		}
		else {
			Dataview.debugger.logFalseCondition("outputport_index must be greater than 0 and smaller than "+outs.length+", the number of output ports.", outputport_index >= 0 && outputport_index < outs.length);
			
		}					
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

		
		return obj;		
	}
	
	public abstract void run();
}
