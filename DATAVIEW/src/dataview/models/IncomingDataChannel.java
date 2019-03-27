package dataview.models;

/**
 * The incomingDataChannel will define the data flowing between the parent task and current 
 * task based on the port connection. 
 * 
 *
 */
public class IncomingDataChannel {
		public String srcFilename;        // from 1
		public Task srcTask;   // from 2
		public int outputPortIndex;       // from 2
		public int myInputPortIndex;       // to
		
		
		/* connect the output port of the parent task to the input port of the current task */
		public IncomingDataChannel(Task srcTask, int outputPortIndex, int myInputPortIndex)
		{
			this.srcTask = srcTask;                    // from 2      
			this.outputPortIndex = outputPortIndex;          // from 2
			
			this.myInputPortIndex = myInputPortIndex;            // to
			
			this.srcFilename = null;                         // NA
		}
		
		/* connect a file to the input port of a task */
		public IncomingDataChannel(String srcFilename,  int myInputPortIndex){
			this.srcFilename = srcFilename;                 // from 1
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
	    	if(srcFilename != null)
	    		obj.put("srcFilename", new JSONValue(srcFilename));
	    	else
	    		obj.put("srcFilename", new JSONValue(""));
	    	
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