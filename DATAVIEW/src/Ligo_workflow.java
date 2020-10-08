import dataview.models.DATAVIEW_BigFile;
import dataview.models.Task;
import dataview.models.Workflow;
import dataview.models.WorkflowEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


/*
 * Two parameters are used in the Montage_workflow consturction: 
 * @para The number of mProject and the number of mDiffFit
 */

public class Ligo_workflow extends Workflow{

	final static int num_TmpltBank = 6;
	private Map<Integer, Map<Integer, Double>> edgeMap;    
	private Map<Integer, Map<String, Double >> execTime; // @Key: VMType @Value: execution time 
	
	Ligo_workflow()
	{
		super("Ligo_workflow", "This workflow is used to benefit from the Ligo workflow structure to do experiments.");	
		wins = new Object[num_TmpltBank];
		wouts = new Object[num_TmpltBank];
		for(int i = 0; i < num_TmpltBank; i++){
			wins[i] = new DATAVIEW_BigFile("input" + i +".txt");
		}
		for(int i = 0; i < num_TmpltBank; i++){
			wouts[i] = new DATAVIEW_BigFile("output" + i + ".txt");
		}
		
		
		
		edgeMap = new HashMap<Integer, Map<Integer, Double>>();
		execTime = new HashMap<Integer, Map<String, Double >>();
	}
	
	
	public void design(){
		Task [] stage1 = addTasks("TmpltBank", num_TmpltBank);
		Task[] stage2 = addTasks("Inspiral",num_TmpltBank);
		
		//addEdges_OneToOneMappingFiles()
		
		Task stage3 = addTask("Thinca");
		Task[] stage4 = addTasks("TrigBank",num_TmpltBank);
		Task[] stage5 = addTasks("Inspiral",num_TmpltBank);
		Task stage6 = addTask("Thinca");
		
		
		for(int i = 0; i<stage1.length; i++){
			addEdge(i, stage1[i], 0);
		}
		addEdges_OneToOneMapping(stage1,stage2);
		addEdges_JoinPattern(stage2,stage3,num_TmpltBank);
		addEdges_SplitPattern(stage3,stage4,0,num_TmpltBank);
		addEdges_OneToOneMapping(stage4,stage5);
		addEdges_JoinPattern(stage5,stage6,num_TmpltBank);
		for(int i = 0; i< num_TmpltBank; i++){
			addEdge(stage6, i, i);
		}
		
	}
	
	
	private void addEdges_OneToOneMapping(Task[] parent, Task[] children){
		for(int i=0; i < parent.length; i++ ){
			addEdge(parent[i],0,children[i],0);
		}
	}
	
	
}
