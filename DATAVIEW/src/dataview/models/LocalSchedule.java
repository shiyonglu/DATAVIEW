package dataview.models;
import java.util.*;

/**
 * 
 * @author ishtiaqahmed, Bai and Shiyong Lu
 * LocalSchedule consists of VM type information and assigned IP and taskSchedules
 * Each LocalSchedule will run in each thread in Workflow executor
 *
 */
public class LocalSchedule {
	private String vmType;
	private String myIP;              // to be allocated by a cloud provisioner 
	private List<TaskSchedule> tschs;
	
	/**
	 * Constructs and initialize VM info and corresponding taskSchedules
	 * @param vmInfo
	 * @param taskSchedules
	 */
	public LocalSchedule() {
		tschs = new ArrayList<TaskSchedule>();
	}
	
	/* To check if the local schecdule contains task t or not 
	 * 
	 */
	public boolean containsTask(Task t)
	{
	     for(int i=0; i<tschs.size(); i++){
	        TaskSchedule tsch = tschs.get(i);
	        if(t.equals(tsch.getTask())) return true;
	     }
	     
	     return false;
	}
	
	public TaskSchedule getTaskSchedule(int i) {
		return tschs.get(i);
	}
	
	public void addTaskSchedule(TaskSchedule tsch)
	{
	    tschs.add(tsch);
	}
	
	public int length()
	{
		return tschs.size();
	
	}
	public String getVmType() {
		return vmType;
	}

	public void setVmType(String vmType) {
		this.vmType = vmType;
	}
	
	
	public String getIP() {
		return myIP;
	}
	
	public void setIP(String myIP)
	{
		this.myIP = myIP;
		for(int i=0; i< tschs.size(); i++){
			tschs.get(i).setIP(myIP);
		}
		
	}
	
	public JSONObject getSpecification()
	{
		
    	JSONObject obj = new JSONObject();
    	obj.put("vmType", new JSONValue(vmType));
    	obj.put("myIP", new JSONValue(myIP));
    	
    	// add the specifications for all task schedules in this local schedule   	
		JSONArray spec = new JSONArray();
		for(int i=0; i< tschs.size(); i++) {
		      JSONObject tsch_spec = tschs.get(i).getSpecification();
		      spec.add(new JSONValue(tsch_spec));	      
		}
		obj.put("taskSchedules", new JSONValue(spec));
		
		return obj;		
	}	
}
