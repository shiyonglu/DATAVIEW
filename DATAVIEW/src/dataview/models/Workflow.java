package dataview.models;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** We only consider DAG workflows in this implementation. We have explored more sophisticated 
 * dataflow constructs in the VIEW system, see Xubo Fei and Shiyong Lu, "A Dataflow-Based Scientific Workflow Composition Framework", 
 * IEEE Transactions on Services Computing (TSC), 5(1), pp.45-58, 2012, impact factor: 1.47.
 * Future developers can use the ideas there to implement more sophisticated dataflow constructs
 * based on an extension of the DAG model.
 * 
 *  Log: 5/20/2019.  Changed the workflow model. 
 *  
 *  Changed all the workflow inputs and outputs to allow arbitrary Java objects. Previously, we only allow 
 *  files. Such extension allows to change an existing workflow with another intputs and outputs, which essentially allows to 
 *  call the same workflow with different inputs or parameters easily. 
 
 * 
 * 
 */
public class Workflow {
	// The following fields are public interface, unlikely to change
	public String workflowName;
	public  String workflowDescription;
	public Object [] wins;         // an array of workflow inputs
	public Object [] wouts;         // an array of workflow outputs
	
	// the following fields are private, subject to change during implementation
	private List<Task> myTasks;
	private List<WorkflowEdge> myEdges;
	private List<Stage> myStages;
	
	public Workflow(String workflowName, String workflowDescription)
	{
		//System.out.println("000000000000000000");
		this.workflowName = workflowName;
		this.workflowDescription = workflowDescription;
		myTasks = new ArrayList<Task>();
		myEdges = new ArrayList<WorkflowEdge>();
		myStages = new ArrayList<Stage>();
	}
	
	public int getNumOfNodes()
	{
		return wins.length+myTasks.size()+wouts.length;
	}
	
	public int getNumOfWorkflowInputs()
	{
		return wins.length;
	}

	public int getNumOfWorkflowOutputs()
	{
		return wouts.length;
	}

	public int getNumOfTasks()
	{
		return myTasks.size();
	}

	public Object getWorkflowOutput(int i) {
		return this.wouts[i];
	}
	
	public Object getWorkflowInput(int i) {
		return this.wins[i];
	}

	
	public Task getTask(int i) {
		return this.myTasks.get(i);
	}

	public List<WorkflowEdge> getEdges()
	{
		return myEdges;
	}
	

	
	public int getIndexOfTask(Task t)
	{
		for(int i= 0; i <myTasks.size(); i++)
			if(myTasks.get(i).equals(t)) return i;
		
		return -1;
	}
	
	
	

	public void addEdge(Task srcTask, int outputPortIndex, Task destTask, int inputPortIndex)
	{
		//System.out.println("11111111111111");
		myEdges.add(new WorkflowEdge(srcTask, outputPortIndex, destTask, inputPortIndex));
	}
	
	public void addEdge(Task srcTask,Task destTask)
	{
		//System.out.println("11111111111111");
		myEdges.add(new WorkflowEdge(srcTask, 0, destTask, 0));
	}
	
	
	public void addEdge(int winIndex, Task destTask, int inputPortIndex) {
		//System.out.println("22222222222222");
		myEdges.add(new WorkflowEdge(winIndex, destTask, inputPortIndex));
	}
	
	public void addEdge(int winIndex, Task destTask) {
		//System.out.println("22222222222222");
		myEdges.add(new WorkflowEdge(winIndex, destTask, 0));
	}

	
	public void addEdge(Task srcTask, int outputPortIndex, int woutIndex) {
		//System.out.println("33333333333333333333");
		myEdges.add(new WorkflowEdge(srcTask, outputPortIndex, woutIndex));
	}

	public void addEdge(Task srcTask, int woutIndex) {
		myEdges.add(new WorkflowEdge(srcTask, 0, woutIndex));
	}


	
	public void run() 
	{
		// GlobalSchedule gsc = AlphaWorkflowPlanner();
		// WorkflowExecutor we = new WorkflowExector(gsc);
		// we.run();
	}
	
	
		
	public boolean verify()
	{
		return true;
		// verify the structure of the workflow to make sure it is well-formed, and return 
		// true or false and write an error message when necessary?
	}	
	
    @Override
    public String toString() 
	{
		String str = "";
		
		for(WorkflowEdge e: myEdges) {
			if(e.edgeType == 0)
				str = str + "win " + e.winIndex + " => " + e.destTask + ".inputport: " + e.inputPortIndex + "\n";
			else if (e.edgeType == 2)
				str = str + e.srcTask + ".outputPort: " + e.outputPortIndex + " => " + "wout"+e.woutIndex+"\n";
			else
				str = str + e.srcTask + ".outputPort: " + e.outputPortIndex + " => " + e.destTask + ".inputport: " + e.inputPortIndex + "\n";
		}		
		
		return str;		
	}
    
    /* add a single task */
    public Task addTask(String taskTypeName, String location){
    	Task  newtask = null;
    	System.out.println("Attemp to add one task of type "+taskTypeName);
		if(new File(location + taskTypeName + ".jar").exists()){
			location = location + taskTypeName + ".jar";
		}
		File clazzPath = new File(location);
    	URL url = null;
		try {
			url = clazzPath.toURI().toURL();
			URL[] urls = new URL[] {url};
			Thread.currentThread().setContextClassLoader(new URLClassLoader(urls,Thread.currentThread().getContextClassLoader()));
			ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
			Class<URLClassLoader> classLoaderClass = URLClassLoader.class;
			Method method = classLoaderClass.getDeclaredMethod("addURL", new Class[] { URL.class });
			method.setAccessible(true);
			method.invoke(currentClassLoader, urls);
			Class<?> taskclass = Class.forName(taskTypeName);
			newtask = (Task) taskclass.getDeclaredConstructor().newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}catch (InstantiationException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}catch (InvocationTargetException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}catch (NoSuchMethodException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}catch (SecurityException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}
		myTasks.add(newtask);
		System.out.println("One task: "+newtask+ " is added.");
		myStages.add(new Stage(newtask));
		return newtask;		
    }
    
    
    
    public Task addTask(String taskTypeName)
    {
    	Task  newtask = null;
    	Class<?> taskclass;
		try {
			taskclass = Class.forName(taskTypeName);
			newtask =  (Task) taskclass.newInstance();
				
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Dataview.debugger.logException(e);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Dataview.debugger.logException(e);
		} catch (IllegalAccessException e) {
			System.out.println("Exception, possible reason: the constructor of class "+ taskTypeName+" is not public.");
			// TODO Auto-generated catch block
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}
		myTasks.add(newtask);
		System.out.println("One task: "+newtask+ " is added.");
		myStages.add(new Stage(newtask));
		return newtask;		
    }
    
    
    /* add M number of tasks with task type taskTypeName, warning: do not call addTask from this method as this will affect the notion of stages. */
    public Task [] addTasks(String taskTypeName, int M)
    {
    	Task [] newtasks = new Task[M];
    	
    	System.out.println("Attemp to add "+M+" tasks of type "+taskTypeName);
		Class<?> taskclass;
		try {
			taskclass = Class.forName(taskTypeName);
			for(int i=0; i<M; i++) {
				newtasks[i] =  (Task) taskclass.newInstance();
				myTasks.add(newtasks[i]);
			}
				
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Dataview.debugger.logException(e);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Dataview.debugger.logException(e);
		} catch (IllegalAccessException e) {
			System.out.println("Exception, possible reason: the constructor of class "+ taskTypeName+" is not public.");
			// TODO Auto-generated catch block
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}
 
    				
		System.out.println(M+" tasks: "+taskTypeName+ " are added.");
		myStages.add(new Stage(newtasks));
		return newtasks;		
    }

    /* connect the first output of parent to each input of the M children */
    public void addEdges_SplitPattern(Task parent, Task [] children, int M)
    {
    	Dataview.debugger.logFalseCondition("The number of output ports for the parent task is not equal to the number of children.", parent.outs.length == children.length || children.length == M);
     	for(int i=0; i < M; i++){
    		addEdge(parent, i, children[i], 0);
    	} 	
    }

    /* connect the first output of parent to each input of the M children */
    public void addEdges_SplitPattern(int winIndex, Task [] children, int M)
    {
    	
     	for(int i=0; i < M; i++){
    		addEdge(winIndex, children[i], 0);
    	} 	
    }


    
    /* connect the first output of parent to each kth input of the M children */
    public void addEdges_SplitPattern(Task parent, Task [] children, int k, int M)
    {
    	Dataview.debugger.logFalseCondition("The number of output ports for the parent task is not equal to the number of children.", parent.outs.length == children.length || children.length == M); 	
     	for(int i=0; i < M; i++){
    		addEdge(parent, i, children[i], k);
    	} 	
    }

    /* connect the first output of parent to each input of the M children */
    public void addEdges_SplitPattern(int winIndex, Task [] children, int k, int M)
    {
    	
     	for(int i=0; i < M; i++){
    		addEdge(winIndex, children[i], k);
    	} 	
    }

    
    
    /* connect the output of M parents to the ith intput of child */  
    public void addEdges_JoinPattern(Task [] parents,  Task child, int M)
    {
    	Dataview.debugger.logFalseCondition("Workflow.java: The number of parents is not equal to the number of intput ports of the childre. ", parents.length == child.ins.length || parents.length == M);	
     	for(int i=0; i < M; i++){
    		addEdge(parents[i], 0, child, i);
    	} 	
    }
    
    /* connecting M parents to K children, such that the jth output of each parent will connect to the intput of the jth child
     * We assume each parent has outputs, and each child has M inputs.
     */
    public void addEdges_ShufflePattern(Task [] parents,  Task [] children, int M, int K)
    {
    	Dataview.debugger.logFalseCondition("Workflow.java: The number of parents is not equal to M", parents.length == M);	
    	Dataview.debugger.logFalseCondition("Workflow.java: The number of children is not equal to K", children.length == K);	
     	
    	for(int i=0; i<M; i++) {
    		for(int j=0; j<K; j++)
    			addEdge(parents[i], j, children[j], i); // jth output goes to jth child, like mapreduce
    	}
    }    
    
    /* M map jobs, and K reduce jobs, this pattern can simulate the MapReduce jobs */
    public void addEdges_SplitShuffleJoinPattern(Task split, Task [] mapjob, Task [] reducejob, Task join, int M, int K)
    {
       addEdges_SplitPattern(split, mapjob, M);
       addEdges_ShufflePattern(mapjob, reducejob, M, K);
       addEdges_JoinPattern(reducejob, join, K);
    }
    
    /* M parents, M children, each output port 0 is connected to the corresponding input port 0 of childre */
    public void addEdges_ParallelParallelPattern(Task [] parents , Task [] children, int M)
    {
    	Dataview.debugger.logFalseCondition("Workflow.java: The number of parents is not equal to the number of children.", parents.length == M && children.length == M);
    	
         	for(int i=0; i < M; i++)
    			addEdge(parents[i], 0, children[i], 0);
    }
    
    public Stage getStage(int i)
    {
    	if(i>=0) {
    		return myStages.get(i);
    	}
    	else // if negative
    		return myStages.get(myStages.size()+i);  // -1 is the last stage  	
    }
 
	public JSONObject getWorkflowSpecification()
	{
		JSONObject obj = new JSONObject();
		obj.put("workflowName", new JSONValue(workflowName));
		obj.put("workflowDescription", new JSONValue(workflowDescription));
		
		// add all the task instances
		JSONArray tasklib = new JSONArray();
		for(int i=0; i< myTasks.size(); i++) {
		      JSONObject taski = new JSONObject();
		      taski.put("taskInstanceID", new JSONValue(myTasks.get(i).toString()));
		      taski.put("taskType", new JSONValue(myTasks.get(i).getClass().getName()));
		      tasklib.add(new JSONValue(taski));		  
		}
		
		obj.put("taskInstances", new JSONValue(tasklib));
		
		// all input files
		JSONArray src = new JSONArray();
		for(int i=0; i< wins.length; i++) {
			src.add(new JSONValue("win"+i));
		}
		obj.put("workflowInputs", new JSONValue(src));
		
		// all output files
		JSONArray dest = new JSONArray();
		for(int i=0; i< wouts.length; i++) {
			dest.add(new JSONValue("wout"+i));
		}
		obj.put("workflowOutputs", new JSONValue(dest));

		
		// add all task edges
		JSONArray edgelib = new JSONArray();
		for(int i=0; i< myEdges.size(); i++) {
			JSONObject edgespec = myEdges.get(i).getWorkflowEdgeSpecification();
			edgelib.add(new JSONValue(edgespec));
		}
		obj.put("edges", new JSONValue(edgelib));
		
		return obj;		
	}

	/**
	 * 
	 * 
	 * @param t
	 * @return
	 */
	public TaskSchedule getTaskSchedule(Task t) 
	{
		TaskSchedule tsch = new TaskSchedule(t);
		for(WorkflowEdge e: myEdges) {
		    if(e.destTask != null && e.destTask.equals(t)) { // found an incoming data channel
		    	//System.out.println("found an incoming data channel..");
		    	if(e.edgeType == 0)
		    		tsch.AddIncomingDataChannel(new IncomingDataChannel(e.winIndex, e.inputPortIndex));
		    	else
		          tsch.AddIncomingDataChannel(new IncomingDataChannel(e.srcTask, e.outputPortIndex, e.inputPortIndex));
		    }
		    
		    if(e.srcTask != null && e.srcTask.equals(t)) {  // then we found an outgoing data channel
		    	//System.out.println("found an outgoing data channel..");
		    	if(e.edgeType == 2)
		    		tsch.AddOutgoingDataChannel(new OutgoingDataChannel(e.outputPortIndex, e.woutIndex));
		    	else
		          tsch.AddOutgoingDataChannel(new OutgoingDataChannel(e.outputPortIndex, e.destTask, e.inputPortIndex));

		    }		    		    
		} // end for
		
		return tsch;		
	}	
	
	public Map<String, Map<String, Double >> getExecutionTime(){
			return null;
		}
	public Map<String, Map<String, Double>> getTransferTime(){
		return null;
	}
}




