import java.util.ArrayList;
import java.util.List;

import dataview.models.Workflow;
import dataview.models.*;

public class WordCount_workflow extends Workflow{		
		public WordCount_workflow()
		{
			super("WordCount_workflow", "This workflow counts the frequency of a huge input file.");		
		}
		
		
		public void design()
		{

	        // create and add all the tasks
			Task stage1 = addTask("FileSplitter");
			Task [] stage2 = addTasks("WordCount", 2);
			Task stage3 = addTask("WordCountAggregation");
						
			// add edge by a single edge or by a pattern
			addEdge("book.txt", stage1, 0);
			addEdges_SplitPattern(stage1, stage2, 2);
			addEdges_JoinPattern(stage2, stage3, 2);					
			addEdge(stage3, 0, "finalwordcount.txt");
		}
}
