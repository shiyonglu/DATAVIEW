import java.util.ArrayList;
import java.util.List;

import dataview.models.*;

public class WordCount_workflow extends Workflow{		
		public WordCount_workflow()
		{
			super("WordCount_workflow", "This workflow counts the frequency of a huge input file.");	
			wins = new Object[1];
			wouts = new Object[1];
			wins[0] = new DATAVIEW_BigFile("book.txt");
			wouts[0] = new DATAVIEW_BigFile("finalwordcount.txt");
		}
		
		
		public void design()
		{
			int K = 2; // number of wordcount tasks  
	        // create and add all the tasks
			Task stage1 = addTask("FileSplitter");
			Task [] stage2 = addTasks("WordCount", K);
			Task stage3 = addTask("WordCountAggregation");
						
			// add edge by a single edge or by a pattern
			addEdge(0, stage1, 0);
			addEdges_SplitPattern(stage1, stage2, K);
			addEdges_JoinPattern(stage2, stage3, K);					
			addEdge(stage3, 0, 0);
		}
}
