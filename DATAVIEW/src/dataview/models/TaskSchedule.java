
package dataview.models;
import java.util.*;

/**
 *  A TaskSchedule consists of EST (Earlist Start Time), EFT (Earliest Finish Time),
 *  AST (Actual Start Time), AFT (Actual Finish Time), LFT (Latest Finish Time), 
 *  TaskInstanceID (the ID of the task object), parents (parent task objects connected to this task object), 
 *  and children (child task objects connected to this task object.  
 *  
 *  
 *
 */
public class TaskSchedule {
	private Task t;     // the task object
	private String taskInstanceID; // the ID of the task object
	private String taskName;       // the name of the task object
    private List<IncomingDataChannel> indcs; // the list of incoming data channels for this task object in a workflow
    private List<OutgoingDataChannel> outdcs; // the list of outgoing data channels for this task object in a workflow
    private String myIP; // the IP of the host that this task object is mapped to run on
    
    
    
    /**
     * The constructor that constructs a TaskSchedule object for a given a task object.
     * @param t a task object
     */
    public TaskSchedule(Task t)
    {
       this.t = t;
       this.taskInstanceID = t.toString()+"";
       this.taskName = t.getClass().getName();
       indcs = new ArrayList<IncomingDataChannel>();
       outdcs = new ArrayList<OutgoingDataChannel>();
    }
    
    /**
     * Return the ID of the task object
     * 
     * @return Return the ID of the task object
     */
    public String getTaskInstanceID() {
		return taskInstanceID;
	}

    /**
     * Set the ID of the task object
     * 
     * @param taskInstanceID the ID of the task object that we like to set
     */
	public void setTaskInstanceID(String taskInstanceID) {
		this.taskInstanceID = taskInstanceID;
	}

	/**
	 * Get the name of the task object
	 * @return Get the name of the task object.
	 */
	public String getTaskName() {
		return taskName;
	}

	/**
	 * Set the name of the task object, ususally the same of the task class name, but could be set a different one if necessary.
	 * 
	 * @param taskName
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

    
    
	/**
	 * Return the task object for this TaskSchedule object.
	 * 
	 * @return the task object for this TaskSchedule object.
	 */
    public Task getTask()
    {
       return t;
    }
    
    /**
     * Set the IP for this TaskSchedule object
     * 
     * @param myIP
     */
    public void setIP(String myIP)
    {
    	this.myIP = myIP;
    }
    
    /* *
     * return the IP address of the machine that this task is mapped to 
     * 
     * */
    public String getIP()
    {
    	return myIP;
    }
    
    /** 
     * Return the list of incoming data channels
     * 
     * @return Return the list of incoming data channels
     */
    public List<IncomingDataChannel> getIncomingDataChannels()
    {
       return indcs;
    }
    
    /**
     * Return the list of outgoing data channels. 
     * @return The list of outgoing data channels.
     */
    public List<OutgoingDataChannel> getOutgoingDataChannels()
    {
       return outdcs;
    }
    
    /** Return the list of names of the tasks that are the children of this task object.
     * 
     * @return the list of names of the tasks that are the children of this task object.
     */
    public ArrayList<String> getChildren()
    {
    	
       ArrayList<String> children = new ArrayList<String>();
    	 
       for(OutgoingDataChannel outd: outdcs){
    	   if(outd.getDestTask() != null ) children.add(outd.getDestTask().toString());
       }
       
       return children;
    }
    
    /** 
     * Return the list of names of the tasks that are the parents of this task object.
     * 
     * @return Return the list of names of the tasks that are the parents of this task object.
     */
    public ArrayList<String> getParents()
    {
    	
       ArrayList<String> parents = new ArrayList<String>();
    	 
       for(IncomingDataChannel ind: indcs){
    	   if(ind.getSrcTask() != null ) parents.add(ind.getSrcTask().toString());
       }
       
       return parents;
    }
    
    /**
     * 
     * @return
     */
    public ArrayList<String> getParentsPorts()
    {
        ArrayList<String> parents = new ArrayList<String>();
   	 
        for(IncomingDataChannel ind: indcs){
     	   if(ind.getOutputPortIndex() != -1 ) parents.add(ind.getOutputPortIndex()+"");
        }
        
        return parents;    
    }
    
    
    public ArrayList<String> getChildrenPorts()
    {
        ArrayList<String> children = new ArrayList<String>();
   	 
        for(OutgoingDataChannel outd: outdcs){
     	   if(outd.getInputPortIndex() != -1 ) children.add(outd.getInputPortIndex()+"");
        }
        
        return children;
    }

    
    public void AddIncomingDataChannel(IncomingDataChannel indc)
    {
         indcs.add(indc);
    }
    
    public void AddOutgoingDataChannel(OutgoingDataChannel outdc)
    {
         outdcs.add(outdc);
    }
    
    public JSONObject getSpecification()
    {
    	
    	JSONObject obj = new JSONObject();
    	obj.put("taskInstanceID", new JSONValue(this.taskInstanceID));
    	obj.put("taskName", new JSONValue(this.taskName));	
    	obj.put("myIP", new JSONValue(this.myIP));	
    	// add the specifications for all incoming data channels   	
		JSONArray indcs_spec = new JSONArray();
		for(int i=0; i< indcs.size(); i++) {
			  //System.out.println("found an coming spec....");
		      JSONObject parent = indcs.get(i).getSpecification();
		      indcs_spec.add(new JSONValue(parent));	      
		}
		obj.put("incomingDataChannels", new JSONValue(indcs_spec));
    	
    	// add the specifications for all outgoing data channels
		JSONArray outdcs_spec = new JSONArray();
		for(int i=0; i< outdcs.size(); i++) {
		      JSONObject child = outdcs.get(i).getSpecification();
		      outdcs_spec.add(new JSONValue(child));	      
		}
		obj.put("outgoingDataChannels", new JSONValue(outdcs_spec));
		
		return obj;		
    }
    	
}
