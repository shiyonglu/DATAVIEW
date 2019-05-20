package dataview.planners;
import dataview.models.GlobalSchedule;
import dataview.models.LocalSchedule;
import dataview.models.Task;
import dataview.models.TaskSchedule;
import dataview.models.Workflow;

/**
 * The naive1 workflow planner will assign each task of a workflow to a separate VM. Therefore, the number of VMs that are needed will
 * be equal to the number of tasks in the workflow. This algorithms is in favor on those workflows that 
 * have less data movement but has high implict parallelism and computation need for each task.
 * 
 * @author shiyonglu
 * 7/31/2018
 *
 */
public class WorkflowPlanner_Naive1 extends WorkflowPlanner{

	public WorkflowPlanner_Naive1(Workflow w) {
		super(w);
		// TODO Auto-generated constructor stub
	}
	
	public GlobalSchedule plan()
	{
		GlobalSchedule gsch = new GlobalSchedule(w);
		for(int i=0; i<numnode; i++) {
			Task t = w.getTask(i);
			TaskSchedule tsch = w.getTaskSchedule(t);
			LocalSchedule lsch = new LocalSchedule();
			lsch.addTaskSchedule(tsch);
			lsch.setVmType("t2.micro");
			gsch.addLocalSchedule(lsch);
		
		}
	
		return gsch;	
	}

}
