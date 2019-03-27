package dataview.models;
import java.util.*;

/**
 * TaskSchedule consists of EST, EFT, AST, AFT, LFT, TaskID, parents, and children
 * 
 *
 */
public class TaskSchedule {
	private Task t;     // this is the ID
	private String taskInstanceID;
	private String taskName;
    private List<IncomingDataChannel> indcs;
    private List<OutgoingDataChannel> outdcs;
    private String myIP;
    
    
    
    
    public TaskSchedule(Task t)
    {
       this.t = t;
       this.taskInstanceID = t.toString()+"";
       this.taskName = t.getClass().getName();
       indcs = new ArrayList<IncomingDataChannel>();
       outdcs = new ArrayList<OutgoingDataChannel>();
    }
    
    public String getTaskInstanceID() {
		return taskInstanceID;
	}

	public void setTaskInstanceID(String taskInstanceID) {
		this.taskInstanceID = taskInstanceID;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

    
    
    public Task getTask()
    {
       return t;
    }
    
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
    
    public List<IncomingDataChannel> getIncomingDataChannels()
    {
       return indcs;
    }
    
    public List<OutgoingDataChannel> getOutgoingDataChannels()
    {
       return outdcs;
    }
    
    public ArrayList<String> getChildren()
    {
    	
       ArrayList<String> children = new ArrayList<String>();
    	 
       for(OutgoingDataChannel outd: outdcs){
    	   if(outd.getDestTask() != null ) children.add(outd.getDestTask().toString());
       }
       
       return children;
    }
    
    public ArrayList<String> getParents()
    {
    	
       ArrayList<String> parents = new ArrayList<String>();
    	 
       for(IncomingDataChannel ind: indcs){
    	   if(ind.getSrcTask() != null ) parents.add(ind.getSrcTask().toString());
       }
       
       return parents;
    }
    
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
