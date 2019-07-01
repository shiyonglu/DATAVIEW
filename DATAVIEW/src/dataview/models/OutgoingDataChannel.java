
package dataview.models;

/**
 * The OutgoingDataChannel will define the data flowing between the current task and the child 
 * task based with the port connection. 
 * 
 * Each OutgoingDataChannel specifies the index of the OutputPort of the current TaskSchedule object, and 
 * the child task object, and the IP that is assigned to the child task, and the destination file name, or 
 * the index of InputPort of the child TaskSchedule that this OutgoingDataChannel connects to. 
 * 
 * 
 *  Log: 5/20/2019.  Changed the workflow model. 
 *  
 *  Changed all the workflow inputs and outputs to allow arbitrary Java objects. Previously, we only allow 
 *  files. Such extension allows to change an existing workflow with another intputs and outputs, which essentiallys allows to 
 *  call the same workflow with different inputs or parameters easily. 
 
 * 
 */
public class OutgoingDataChannel {

		public int myOutputPortIndex;        // from an outputPort of this task
		
		public Task destTask;                // to an outputPort of a child task
		public int inputPortIndex;           // to an outputPort of a child task
		public String destIP;                // the IP of the VM that the child task is assigned to

		public int woutIndex;                // or to a workflow output
		
		
		/* connect the output port of a task to the inputport of another task */
		public OutgoingDataChannel(int myOutputPortIndex, Task destTask, int inputPortIndex)
		{
			this.myOutputPortIndex = myOutputPortIndex;  // from
			
			
			this.destTask = destTask;  // to
			this.inputPortIndex = inputPortIndex; // to
			this.woutIndex = -1;  // NA
		}
		
		/* Connect the ouput port of a task to an output file*/
		public OutgoingDataChannel(int myOutputPortIndex, int woutIndex){
			this.myOutputPortIndex = myOutputPortIndex;  // from
			
			
			this.woutIndex = woutIndex;   // to
			
			this.destTask = null;   // NA
			this.inputPortIndex = -1;  // NA
		}	
		
		public void setDestIP(String destIP)
		{
		    this.destIP = destIP;
		}
		
		public Task getDestTask()
		{
			return destTask;
		}
		
		public int getInputPortIndex()
		{
			return inputPortIndex;
		}
		
		public JSONObject getSpecification()
		{
	    	JSONObject obj = new JSONObject();
	    	
	  		obj.put("myOutputPortIndex", new JSONValue(myOutputPortIndex+""));
	  		 

	    	if(destTask != null){
	    		obj.put("destTask", new JSONValue(destTask.toString()));
	    		obj.put("destIP", new JSONValue(destIP));
	    	}
	    	else{
	    		obj.put("destTask", new JSONValue(""));
	    		obj.put("destIP", new JSONValue(""));
	    	}
	    		


	    	if(inputPortIndex != -1)
	    		obj.put("inputPortIndex", new JSONValue(inputPortIndex+""));
	    	else
	    		obj.put("inputPortIndex", new JSONValue("-1"));

	    	
	    	if(woutIndex != -1)
	    		obj.put("wout", new JSONValue(""+woutIndex));
	    	else
	    		obj.put("wout", new JSONValue(""));
	    	
	    	
	    	return obj;
		}				
	}