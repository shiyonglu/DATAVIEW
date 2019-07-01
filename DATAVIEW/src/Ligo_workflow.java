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

	final static int num_TmpltBank = 3;
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
	
	public Map<Integer, Map<String, Double >> getExecutionTime(){
		List<String> vmType = new ArrayList<String>();
		vmType.add("VM1");vmType.add("VM2");vmType.add("VM3");
		for (int i = 0; i< this.getNumOfTasks(); i++){
			for (int j = 0; j < vmType.size(); j++){
				if(execTime.containsKey(i+1)){
					execTime.get(i+1).put(vmType.get(j), getTaskWeight(this.getTask(i), vmType.get(j)));
				}
				else{
					Map<String, Double> tmp = new HashMap<String, Double>();
					tmp.put(vmType.get(j),getTaskWeight(this.getTask(i), vmType.get(j)));
					execTime.put(i+1, tmp);
				}
			}
			
		}	
		// Add the dummy entry task and exit task
		for(int i = 0; i < vmType.size(); i++){
			if(execTime.containsKey(0)){
				execTime.get(0).put(vmType.get(i), 0.0);
			}
			else{
				Map<String, Double> tmp = new HashMap<String, Double>();
				tmp.put(vmType.get(i), 0.0);
				execTime.put(0, tmp);
			}
			if(execTime.containsKey(-1)){
				execTime.get(-1).put(vmType.get(i), 0.0);
			}
			else{
				Map<String, Double> tmp = new HashMap<String, Double>();
				tmp.put(vmType.get(i), 0.0);
				execTime.put(-1, tmp);
			}
		}
		return execTime;
	}
	
	public Map<Integer, Map<Integer, Double>> getTransferTime(){
		for (int i = 0; i<this.getEdges().size();i++){
			WorkflowEdge edge = this.getEdges().get(i);
			if(edge.srcTask==null){
				if(edgeMap.containsKey(0)){
					edgeMap.get(0).put(this.getIndexOfTask(edge.destTask)+1, (double) 0);
				}else{
					Map<Integer, Double> tmp = new HashMap<Integer, Double>();
					tmp.put(this.getIndexOfTask(edge.destTask)+1, (double) 0);
					edgeMap.put(0, tmp);
				}
			}
			else if(edge.destTask==null){
				if(edgeMap.containsKey(this.getIndexOfTask(edge.srcTask)+1)){
					edgeMap.get(this.getIndexOfTask(edge.srcTask)+1).put(-1, (double)0);
				}else{
					Map<Integer, Double> tmp = new HashMap<Integer, Double>();
					tmp.put(-1, (double) 0);
					edgeMap.put(this.getIndexOfTask(edge.srcTask)+1, tmp);
				}
			}else{
				if(edgeMap.containsKey(this.getIndexOfTask(edge.srcTask)+1)){
					edgeMap.get(this.getIndexOfTask(edge.srcTask)+1).put(this.getIndexOfTask(edge.destTask)+1,getEdgeWeight(edge) );
				}else{
					Map<Integer, Double> tmp = new HashMap<Integer, Double>();
					tmp.put(this.getIndexOfTask(edge.destTask)+1,getEdgeWeight(edge) );
					edgeMap.put(this.getIndexOfTask(edge.srcTask)+1, tmp);
				}
			}
		
		}	
		
		
		return edgeMap;
	}
	
	
	
	
	
	private double getTaskWeight(Task t, String VMtype){
		String name = t.taskName;
		switch(VMtype){
			case "VM1":
				switch(name){
				case "TmpltBank":
					return 2.4;
				case "Inspiral":
					return 20.77;
				case "Thinca":
					return 196.1;
				case "TrigBank":
					return 81.43;
				}
			case "VM2":
				switch(name){
				case "TmpltBank":
					return 2.41;
				case "Inspiral":
					return 21.61;
				case "Thinca":
					return 199.5;
				case "TrigBank":
					return 82.7;
				}
			case "VM3":
				switch(name){
				case "TmpltBank":
					return 2.81;
				case "Inspiral":
					return 184.3;
				case "Thinca":
					return 2100.9;
				case "TrigBank":
					return 850.9;
				}
			
		}
		
		return -1;
		
	}
	
	public double getEdgeWeight(WorkflowEdge edge){
		Task src = edge.srcTask;
		Task des = edge.destTask;
		if(src!=null && des!=null){
			if(src.taskName.equals("TmpltBank")  && des.taskName.equals("Inspiral") ){
				return 118.5/20; 
			}
			if(src.taskName.equals("Inspiral") && des.taskName.equals("Thinca")){
				return 318.8/20;
			}
			if(src.taskName.equals("Thinca") && des.taskName.equals("TrigBank")){
				return 956.4/20;
			}
			if(src.taskName.equals("TrigBank") && des.taskName.equals("Inspiral")){
				return 637.6/20;
			}
		}
		return 0;
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
		for(int i = 0; i<stage1.length; i++){
			addEdge(stage6, 0, i);
		}
		
	}
	
	
	private void addEdges_OneToOneMapping(Task[] parent, Task[] children){
		for(int i=0; i < parent.length; i++ ){
			addEdge(parent[i],0,children[i],1);
		}
	}
	
	
}
