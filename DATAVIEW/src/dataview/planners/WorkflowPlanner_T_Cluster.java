package dataview.planners;
import dataview.models.GlobalSchedule;
import dataview.models.LocalSchedule;
import dataview.models.Task;
import dataview.models.TaskSchedule;
import dataview.models.Workflow;
import java.util.*;

/**
 * The T_cluster planner implements the algorithm proposed by Aravind Mohand, et al. The main advantage of this scheduling
 * algorithm is that it is only based on the structure of the given workflow, and no need to know the weights of nodes and edges.
 * It will retrieve from the workfow path-by-path until 
 * 
 * 
 * @author shiyonglu
 * 7/31/2018
 *
 */
public class WorkflowPlanner_T_Cluster extends WorkflowPlanner{

	public WorkflowPlanner_T_Cluster(Workflow w) {
		super(w);
		// TODO Auto-generated constructor stub
	}
	
	public GlobalSchedule plan()
	{
		List<Integer> planned_tasks = new ArrayList<Integer>();
		
		GlobalSchedule gsch = new GlobalSchedule(w);
		
		
		List<Integer> path;
		
		while((path = findAPath(planned_tasks)) != null){
			// convert each path to a local schedule
			LocalSchedule lsch = new LocalSchedule();
			for(Integer t: path) {
				planned_tasks.add(t);
				alist[t].clear(); // remove all the outgoing edges from this task
				TaskSchedule tsch = w.getTaskSchedule(w.getTask(t));
				lsch.addTaskSchedule(tsch);
				lsch.setVmType("t2.micro");
			
			}
			// add the new local schedule to the global schedule
			
			gsch.addLocalSchedule(lsch);	
		}				
	
		return gsch;	
	}
	
	private List<Integer> findAPath(List<Integer> planned_tasks)
	{
		List<Integer> path = new ArrayList<Integer>();
		
		int t;
		
		for(t=0; t<numnode; t++) {		
			if(planned_tasks.contains(t)) continue; // this task is already planned
			if (isNoParents(t)) break;
		}
		
		if(t == numnode) return null; // no first task
		
		// t is the first task of a new local schedule
		do {
			path.add(t);
		}while((t = nextTask(t, planned_tasks)) != -1);
		
		System.out.println("Found a path: "+ path);
		return path;
	}
	
	/* t has no parents in the adjacency list */
	private boolean isNoParents(int t)
	{
		for(int i=0; i<numnode; i++)
              if(alist[i].contains(t)) return false;
		
		return true;		
	}
	
	private int nextTask(int t, List<Integer> planned_tasks)
	{
		for(Integer c: alist[t]) {        // pick a child that is not visited
			if(!planned_tasks.contains(c)) return c;
		}
		
		return -1;		
	}
}
 