package dataview.planners;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dataview.models.*;

/**
 * The workflow planner will get the structure information given a graph
 * 
 *
 */

public class WorkflowPlanner {
	public static final int WorkflowPlanner_Naive1 = 0;
	public static final int WorkflowPlanner_Naive2 = 1;
	public static final int WorkflowPlanner_T_Cluster = 2;
	public static final int WorkflowPlanner_ICPCP = 3;
	public static final int WorkflowPlanner_LPOD = 5;
	
	protected Workflow w;
	
	// this is the graph of the workflow:
	protected int numnode;     // number of tasks
	public List<Integer>[] alist; // adjacency list
		

	
	public WorkflowPlanner()
	{
	}
	

	
	/**
	 * The constructor will create an adjacency list graph representation of the given workflow.
	 *  
	 * @param w
	 */
	public WorkflowPlanner(Workflow w)
	{
	    this.w = w;
	    this.numnode = w.getNumOfTasks();
		alist = new List[numnode];
		for(int i=0; i< numnode; i++) {
			alist[i] = new ArrayList<Integer>();
		}	    
		
		for(WorkflowEdge e: w.getEdges()) {
			if(e.srcTask != null && e.destTask != null) { // add an edge in the adjacency list
				
				int i = w.getIndexOfTask(e.srcTask);
				System.out.print("====== " + e.srcTask.toString() + " " + i);
				
				int j = w.getIndexOfTask(e.destTask);
				System.out.println("   " + e.destTask.toString() + " " + j);
				alist[i].add(j);
			}
		}		
	}
	
	/**
	 * This method needs to re-implemented based on the scheduling algorithm 
	 * @return a global schedule 
	 */
	public GlobalSchedule plan()
	{
		return null;
	}
}
