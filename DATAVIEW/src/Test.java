

import java.io.File;

import dataview.models.*;
import dataview.planners.WorkflowPlanner;
import dataview.planners.WorkflowPlanner_ICPCP;

import dataview.planners.WorkflowPlanner_Naive1;
import dataview.planners.WorkflowPlanner_Naive2;
import dataview.planners.WorkflowPlanner_T_Cluster;
import dataview.workflowexecutors.WorkflowExecutor;
import dataview.workflowexecutors.WorkflowExecutor_Beta;

/** 
 * six steps shows the whole process to creat a workflow, design a workflow, generate a workflow schedule, and execute a workfow in EC2.
 */

public class Test {

	public static void main(String[] args) throws Exception {
		
		// step 1: create a workflow
		//WorkflowVisualization frame = new WorkflowVisualization();	
		//W1 w = new W1();
		//SampleWorkflow w = new SampleWorkflow();
		//Montage_workflow w = new Montage_workflow();
		MR w = new MR();
		//Diagnosis w = new Diagnosis();
		//DummyWorkflow w = new DummyWorkflow();
		
		// step 2: design a workflow
		w.design();
		//frame.drawWorkflowGraph(w);
		
		// step 3: choose a workflow planner
		int whichplanner = WorkflowPlanner.WorkflowPlanner_T_Cluster;
		
		WorkflowPlanner wp = null;
		switch (whichplanner) {
		case WorkflowPlanner.WorkflowPlanner_Naive1:
			wp = new WorkflowPlanner_Naive1(w);
			break;
		case WorkflowPlanner.WorkflowPlanner_Naive2:
			wp = new WorkflowPlanner_Naive2(w);
			break;
		case WorkflowPlanner.WorkflowPlanner_T_Cluster:
			wp = new WorkflowPlanner_T_Cluster(w);
			break;
		case WorkflowPlanner.WorkflowPlanner_ICPCP:
			wp = new WorkflowPlanner_ICPCP(w);
			break;
		default:
			wp = new WorkflowPlanner_T_Cluster(w);
			break;
		}
		

		// step 4: generate a workflow schedule	
		
		GlobalSchedule gsch = wp.plan();
		//System.out.println(gsch.getSpecification());
		// step 5: select a workflow executor 
		String fileLocation = System.getProperty("user.dir") + File.separator + "WebContent" +File.separator;
		int whichexecutor = WorkflowExecutor.WorkflowExecutor_Beta;
		WorkflowExecutor we = null;
		switch (whichexecutor) {
		case WorkflowExecutor.WorkflowExecutor_Beta:
			we = new WorkflowExecutor_Beta(fileLocation+"workflowTaskDir"+ File.separator, fileLocation + "workflowLibDir"+ File.separator , gsch);
			break;
		default:
			we = new WorkflowExecutor_Beta(fileLocation+"workflowTaskDir"+ File.separator, fileLocation + "workflowLibDir"+ File.separator , gsch);
		}
		
		// step 6: execute a workflow
		we.execute();	
	}

}
