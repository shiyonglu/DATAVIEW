import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataview.models.DATAVIEW_BigFile;
import dataview.models.Task;
import dataview.models.Workflow;
import dataview.models.WorkflowEdge;

public class Dummy_Workflow extends Workflow {
	
	private Map<String, Map<String, Double>> edgeMapTemp;  // 
	private Map<String, Map<String, Double >> execTimeTemp; // @Key: VMType @Value: execution time 

	
	public Dummy_Workflow() {
		super("SampleWorkflow", " This workflow illustrate the example used in the paper");
		wins = new Object[1];
		wouts = new Object[1];
		wins[0] = new DATAVIEW_BigFile("originalInput.txt");
		wouts[0] =new DATAVIEW_BigFile("DGoutput0.txt");
		
		edgeMapTemp = new HashMap<String, Map<String, Double>>();
		execTimeTemp = new HashMap<String, Map<String, Double >>();
		
	}
	public void design()
	{
		
		
        // create and add all the tasks
		
		Task T1 = addTask("T1");
		Task T2 = addTask("T2");
		Task T3 = addTask("T3");
		Task T4 = addTask("T4");
		Task T5 = addTask("T5");
		Task T6 = addTask("T6");
		Task T7 = addTask("T7");
		Task T8 = addTask("T8");
		Task T9 = addTask("T9");
		Task T10 = addTask("T10");
		// add edges
		
		addEdge(0, T1, 0);
		addEdge(0, T2, 0);
		addEdge(0, T3, 0);
		
		addEdge(T1, 0, T4, 0);
		addEdge(T2, 0, T5, 0);
		addEdge(T2, 1, T6, 0);
		addEdge(T3, 0, T6, 1);
		
		
		addEdge(T4, 0, T7, 0);
		addEdge(T4, 1, T8, 0);
		addEdge(T5, 0, T8, 1);
		addEdge(T6, 0, T9, 0);
		addEdge(T6,1,T10,0);
		
		addEdge(T7, 0 ,0);
		addEdge(T8, 0, 0);
		addEdge(T9, 0, 0);
		addEdge(T10,0,0);
		   
	}
	
	
	public Map<String, Map<String, Double >> getExecutionTime(){
		List<String> vmType = new ArrayList<String>();
		vmType.add("VM1");vmType.add("VM2");vmType.add("VM3");
		for (int i = 0; i< this.getNumOfTasks(); i++){
			for (int j = 0; j < vmType.size(); j++){
				if(execTimeTemp.containsKey(this.getTask(i).taskName)){
					
					System.out.println(this.getTask(i).taskName);
					execTimeTemp.get(this.getTask(i).taskName).put(vmType.get(j), getTaskWeight(this.getTask(i), vmType.get(j)));
				}
				else{
					Map<String, Double> tmp = new HashMap<String, Double>();
					tmp.put(vmType.get(j),getTaskWeight(this.getTask(i), vmType.get(j)));
					execTimeTemp.put(this.getTask(i).taskName, tmp);
				}
			}

		}	
		/*
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
		} */
		return execTimeTemp;
	}

	public Map<String, Map<String, Double>> getTransferTime(){
		for (int i = 0; i<this.getEdges().size();i++){
			WorkflowEdge edge = this.getEdges().get(i);
			if(edge.srcTask != null && edge.destTask != null ){
				if(edgeMapTemp.containsKey(edge.srcTask.taskName)){
					edgeMapTemp.get(edge.srcTask.taskName).put(edge.destTask.taskName, getEdgeWeight(edge) );
				}else{
					Map<String, Double> tmp = new HashMap<String, Double>();
					tmp.put(edge.destTask.taskName,getEdgeWeight(edge) );
					edgeMapTemp.put(edge.srcTask.taskName, tmp);
				}
			}
			
			/*
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
			}*/

		}	
		return edgeMapTemp;
	}





	private double getTaskWeight(Task t, String VMtype){
		String name = t.taskName;
		switch(VMtype){
		case "VM1":
			switch(name){
			case "T1":
				return 4;
			case "T2":
				return 10;
			case "T3":
				return 6;
			case "T4":
				return 8;
			case "T5":
				return 6;
			case "T6":
				return 8;
			case "T7":
				return 10;
			case "T8":
				return 6;
			case "T9":
				return 10;
			case "T10":
				return 3;
			}

		case "VM2":
			switch(name){
			case "T1":
				return 10;
			case "T2":
				return 24;
			case "T3":
				return 10;
			case "T4":
				return 12;
			case "T5":
				return 17;
			case "T6":
				return 16;
			case "T7":
				return 16;
			case "T8":
				return 13;
			case "T9":
				return 16;
			case "T10":
				return 6;
			}
		case "VM3":
			switch(name){
			case "T1":
				return 16;
			case "T2":
				return 32;
			case "T3":
				return 18;
			case "T4":
				return 20;
			case "T5":
				return 21;
			case "T6":
				return 22;
			case "T7":
				return 22;
			case "T8":
				return 15;
			case "T9":
				return 28;
			case "T10":
				return 8;
			}
		}

		return -1.0;
	}

	public double getEdgeWeight(WorkflowEdge edge){
		Task src = edge.srcTask;
		Task des = edge.destTask;
		if(src!=null && des!=null){
			if(src.taskName.equals("T1")  && des.taskName.equals("T4") ){
				return 1; 
			}
			if(src.taskName.equals("T2") && des.taskName.equals("T5")){
				return 2;
			}
			if(src.taskName.equals("T2") && des.taskName.equals("T6")){
				return 2;
			}
			if(src.taskName.equals("T3") && des.taskName.equals("T6")){
				return 2;
			}
			if(src.taskName.equals("T4") && des.taskName.equals("T7")){
				return 1;
			}
			if(src.taskName.equals("T4") && des.taskName.equals("T8")){
				return 1;
			}
			if(src.taskName.equals("T5") && des.taskName.equals("T8")){
				return 4;
			}
			if(src.taskName.equals("T6") && des.taskName.equals("T9")){
				return 3;
			}
			if(src.taskName.equals("T6") && des.taskName.equals("T10")){
				return 3;
			}
		}
		return 0;
	}
	
	
	
	
		
}
