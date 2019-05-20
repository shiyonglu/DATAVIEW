package dataview.planners;
import dataview.models.GlobalSchedule;
import dataview.models.LocalSchedule;
import dataview.models.Task;
import dataview.models.TaskSchedule;
import dataview.models.Workflow;
import java.util.*;

/**
 * The naive2 workflow planner will all the tasks of a workflow to one single VM. Therefore, no data movement for intermediate 
 * data products will be involved among VMs. This planner is in favor of pipelines or  sequential workflows in which there is little or 
 * no parallelism.
 * 
 * 
 * @author shiyonglu
 * 7/31/2018
 *
 */
public class WorkflowPlanner_Naive2 extends WorkflowPlanner{

	public WorkflowPlanner_Naive2(Workflow w) {
		super(w);
		// TODO Auto-generated constructor stub
	}
	
	public GlobalSchedule plan()
	{
		List<Integer> planned_tasks = new ArrayList<Integer>();
		
		GlobalSchedule gsch = new GlobalSchedule(w);
		LocalSchedule lsch = new LocalSchedule();
		
		int nt;
		while((nt = NextTask(planned_tasks)) != -1){  // doing a topological ordering of all the tasks in the workflow
			planned_tasks.add(nt);
			TaskSchedule tsch = w.getTaskSchedule(w.getTask(nt));
			lsch.addTaskSchedule(tsch);
			lsch.setVmType("t2.micro");
		
		}
		
		// only one single local schedule for the global schedule
		gsch.addLocalSchedule(lsch);
	
		return gsch;	
	}
	
	private int NextTask(List<Integer> planner_tasks)
	{
		for(int t=0; t<numnode; t++) {		
			if(planner_tasks.contains(t)) continue; // this task is already planned
			if(isNoParents(t)) {
				alist[t].clear(); // delete all the outgoing edges from this task
				return t;		
			}
		}	       
		
		return -1; // no more next task
	}
	
	/* t has no parents in the adjacency list */
	private boolean isNoParents(int t)
	{
		for(int i=0; i<numnode; i++)
              if(alist[i].contains(t)) return false;
		
		return true;		
	}
	
	

}
 