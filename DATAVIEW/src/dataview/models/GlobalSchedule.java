package dataview.models;
import java.util.*;



/**
 * 
 * A Global schedule consists of multiple virtual machines. 
 *
 */
public class GlobalSchedule {
	private Workflow w;
	private List<LocalSchedule> lschs;
	
	/**
	 * Constructs and initialize multiple local schedule
	 * @param localSchedules
	 * localSchedules multiple local schedule
	 */
	public GlobalSchedule() {
		lschs = new ArrayList<LocalSchedule>();
	}
	
	public GlobalSchedule(Workflow w)
    {
		lschs = new ArrayList<LocalSchedule>();
		this.w = w;
    }
	
	public void addLocalSchedule(LocalSchedule lsch)
	{
		lschs.add(lsch);
	}
	
	/**
	 * Return the number of local schedules, which is the number of VMs that are needed. 
	 */
	public int length()
	{
	  return lschs.size();
	}

	
	/**
	 * Return the number of tasks in the global schedule, which is equal to the number of TaskSchedules.
	 * 
	 * @return Return the number of tasks in the global schedule, which is equal to the number of TaskSchedules.
	 */
	public int getNumberOfTasks()
	{
		int taskNum = 0;
		
		for(LocalSchedule lsch: lschs){
				taskNum += lsch.length();
		}
		
		return taskNum;
	}
	
	public LocalSchedule getLocalSchedule(int i)
	{
		return lschs.get(i);
	}

	
	public Workflow getWorkflow()
	{
		return w;
	}
	
	
	public JSONObject getSpecification()
	{
		JSONObject obj = new JSONObject();

		// add the specifications for all task schedules in this local schedule   	
		JSONArray spec = new JSONArray();
		for(int i=0; i< lschs.size(); i++) {
			JSONObject lsch_spec = lschs.get(i).getSpecification();
			spec.add(new JSONValue(lsch_spec));	      
		}
		obj.put("localSchedules", new JSONValue(spec));
	
		return obj;			
	}
	
	public String getIP(Task t)
	{
		for(int i= 0; i<lschs.size(); i++ ){
			   LocalSchedule lsch = lschs.get(i);
			   if(lsch.containsTask(t)){
				   return lsch.getIP();
			   }
		}
	
		return null;	
	}
	
	/**
	 * CompleteIPAssignment propagates the IP assignment of LocalSchedules to Taskschedules as well as outgoing data channels in 
	 * TaskSchedules.
	 * 
	 * This method is necessary to propagate all IPS to the outgoing data channels of each task schedule before
	 * we assign each local schedule to a VM. This method can only be called after each local schedule of the global schedule has 
	 * been assigned an IP, in this way, all the chidlren of a task already have IPs assigned to them so that
	 * we can set the IP for each child task that corresponds to each outgoing data channel.  
	 * 
	 */
	public void completeIPAssignment()
	{
		for(int i= 0;  i < lschs.size(); i++ ){
		   LocalSchedule lsch = lschs.get(i);
		   for(int j=0; j<lsch.length(); j++){
		       TaskSchedule tsch = lsch.getTaskSchedule(j);
		       List<OutgoingDataChannel> outdcs = tsch.getOutgoingDataChannels();
		       for(OutgoingDataChannel outdc: outdcs){
		    	      if(outdc.destTask == null) continue;
		    	          outdc.setDestIP(getIP(outdc.destTask));
		       }
		       
		   }
		   
		}
		
	}
	
}
