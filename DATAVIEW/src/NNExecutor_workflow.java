import java.util.ArrayList;
import java.util.List;

import dataview.models.*;

public class NNExecutor_workflow extends Workflow{		
		public NNExecutor_workflow()
		{
			super("NNExecutor_workflow", "This NNExector workflow reuse the trained models on new dataset.");	
			wins = new Object[2];
			wouts = new Object[1];
			wins[0] = new DATAVIEW_BigFile("GeneticNNWorkflow@749070052");
			wins[1] = new DATAVIEW_BigFile("Breast_cancer_data.csv");
			wouts[0] = new DATAVIEW_BigFile("output.txt");
		}
		
		
		public void design()
		{
	        // create and add all the tasks
			Task stage1 = addTask("NNExecutor");
						
			// add edge by a single edge or by a pattern
			addEdge(0, stage1, 0);
			addEdge(1, stage1, 1);				
			addEdge(stage1, 0, 0);
		}
}