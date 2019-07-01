
package dataview.models;

/**
 * The incomingDataChannel will define the data flowing between the parent task and current 
 * task based on the port connection. 
 * 
 *  Each IncomingDataChannel specifies the index of the inputPort and the name of the source file or the name of the parent task
 *  and the index of the OutputPort of the parent task that the data channel connects to. 
 *  
 *  Log: 5/20/2019.  Changed the workflow model. 
 *  
 *  Changed all the workflow inputs and outputs to allow arbitrary Java objects. Previously, we only allow 
 *  files. Such extension allows to change an existing workflow with another intputs and outputs, which essentiallys allows to 
 *  call the same workflow with different inputs or parameters easily. 
 
 */
public class IncomingDataChannel {
		public int winIndex;        // from a workflow input
		public Task srcTask;   // or an outputPort of a parent task
		public int outputPortIndex;       // or from an outputPort of a parent task
		public int myInputPortIndex;       // to an inpurtPort of this task
		
		
		/* connect the output port of the parent task to the input port of the current task */
		public IncomingDataChannel(Task srcTask, int outputPortIndex, int myInputPortIndex)
		{
			this.srcTask = srcTask;                    // from
			this.outputPortIndex = outputPortIndex;    // from
			
			this.myInputPortIndex = myInputPortIndex;  // to       
			
			this.winIndex = -1;                         // NA
		}
		
		/* connect a workflow input to the input port of a task */
		public IncomingDataChannel(int winIndex,  int myInputPortIndex){
			this.winIndex = winIndex;                 // from 
			this.myInputPortIndex = myInputPortIndex;          // to
			this.srcTask = null;                        // NA			
			this.outputPortIndex = -1;                     // NA			
		}
		
		public Task getSrcTask()
		{
			return srcTask;
		}
		
		public int getOutputPortIndex()
		{
			return outputPortIndex;
		}

		public JSONObject getSpecification()
		{
	    	JSONObject obj = new JSONObject();
	    	if(winIndex !=-1 )
	    		obj.put("win", new JSONValue(winIndex+""));
	    	else
	    		obj.put("win", new JSONValue(""));
	    		//obj.put("srcFilename", new JSONValue(""));
	    	
	    	if(srcTask != null)
	    		obj.put("srcTask", new JSONValue(srcTask.toString()));
	    	else
	    		obj.put("srcTask", new JSONValue(""));
           	    	
	    	if(outputPortIndex != -1)
	    		obj.put("outputPortIndex", new JSONValue(outputPortIndex+""));
	    	else
	    		obj.put("outputPorIndex", new JSONValue("-1"));
	    	
	   		obj.put("myInputPortIndex", new JSONValue(myInputPortIndex+""));
	    	
	    	return obj;
		}		
	}