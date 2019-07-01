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

public class Montage_workflow extends Workflow{

	//	final static  int num_mProectPP = 12;
	//	final static  int num_mDiffFit = 20;

	final static  int num_mProectPP = 8;
	final static  int num_mDiffFit = 8;
	private Map<Integer, Map<Integer, Double>> edgeMap;  // 
	private Map<Integer, Map<String, Double >> execTime; // @Key: VMType @Value: execution time 

	Montage_workflow()
	{
		super("Montage_workflow", "This workflow is used to benefit from the Montage workflow structure to do experiments.");	
		
		wins = new Object[num_mProectPP];
		wouts = new Object[1];
		for(int i = 0; i < num_mProectPP; i++){
			wins[i] = new DATAVIEW_BigFile("input" + i +".txt");
		}
		
		wouts[0] = new DATAVIEW_BigFile("output0.txt");
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
			case "mProjectPP":
				return 2.40;
			case "mDiffFit":
				return 20.7;
			case "mConcatFit":
				return 81.43;
			case "mBgModel":
				return 118;
			case "mBackground":
				return 9.7;
			case "mImgTbl":
				return 154.99;
			case "mAdd":
				return 39.4;
			case "mShrink":
				return 60.6;
			case "mJPEG":
				return 196.1;
			}

		case "VM2":
			switch(name){
			case "mProjectPP":
				return 2.40;
			case "mDiffFit":
				return 21.6;
			case "mConcatFit":
				return 82.6;
			case "mBgModel":
				return 120.46;
			case "mBackground":
				return 9.7;
			case "mImgTbl":
				return 157.3;
			case "mAdd":
				return 43.8;
			case "mShrink":
				return 62.5;
			case "mJPEG":
				return 199.5;
			}
		case "VM3":
			switch(name){
			case "mProjectPP":
				return 2.81;
			case "mDiffFit":
				return 184.3;
			case "mConcatFit":
				return 850.85;
			case "mBgModel":
				return 1231.8;
			case "mBackground":
				return 984.7;
			case "mImgTbl":
				return 1619.3;
			case "mAdd":
				return 543.561;
			case "mShrink":
				return 610.272;
			case "mJPEG":
				return 2100.94;
			}
		}

		return -1.0;
	}

	public double getEdgeWeight(WorkflowEdge edge){
		Task src = edge.srcTask;
		Task des = edge.destTask;
		if(src!=null && des!=null){
			if(src.taskName.equals("mProjectPP")  && des.taskName.equals("mDiffFit") ){
				return 118.5/20; 
			}
			if(src.taskName.equals("mDiffFit") && des.taskName.equals("mConcatFit")){
				return 318.8/20;
			}
			if(src.taskName.equals("mConcatFit") && des.taskName.equals("mBgModel")){
				return 637.6/20;
			}
			if(src.taskName.equals("mBgModel") && des.taskName.equals("mBackground")){
				return 738.3/20;
			}
			if(src.taskName.equals("mProjectPP") && des.taskName.equals("mBackground")){
				return 118.5/20;
			}
			if(src.taskName.equals("mBackground") && des.taskName.equals("mImgTbl")){
				return 218.7;
			}
			if(src.taskName.equals("mImgTbl") && des.taskName.equals("mAdd")){
				return 838.9/20;
			}
			if(src.taskName.equals("mAdd") && des.taskName.equals("mShrink")){
				return 419.5/20;
			}
			if(src.taskName.equals("mShrink") && des.taskName.equals("mJPEG")){
				return 536.9/20;
			}
		}
		return 0;
	}

	public void design(){
		Task [] stage1 = addTasks("mProjectPP", num_mProectPP);
		Task[] stage2 = addTasks("mDiffFit",num_mDiffFit);

		//addEdges_OneToOneMappingFiles()

		Task stage3 = addTask("mConcatFit");
		Task stage4 = addTask("mBgModel");
		Task[] stage5 = addTasks("mBackground",num_mProectPP);
		Task stage6 = addTask("mImgTbl");
		Task stage7 = addTask("mAdd");
		Task stage8 = addTask("mShrink");
		Task stage9 = addTask("mJPEG");

		for(int i = 0; i<stage1.length; i++){
			addEdge(i, stage1[i], 0);
		}
		addMulEdges(stage1,stage2);
		addEdges_JoinPattern(stage2,stage3,num_mDiffFit);
		addEdge(stage3,stage4);
		addEdges_SplitPattern(stage4,stage5,0,num_mProectPP);
		addEdges_OneToOneMapping(stage1,stage5);
		addEdges_JoinPattern(stage5,stage6,num_mProectPP);
		addEdge(stage6,stage7);
		addEdge(stage7,stage8);
		addEdge(stage8,stage9);
		addEdge(stage9, 0, 0);
	}

	private void addMulEdges (Task [] parent, Task [] children){
		/* M：3
		 * N：4
		 * each task in N has two incoming edges
		 */
		int p_length = parent.length;  
		int c_length = children.length;
		int[][] assign = new int[p_length][c_length];	
		int outgoingdegree = 2*c_length/p_length;
		for(int i = 0; i < p_length; i ++){
			for(int j = 0; j<outgoingdegree; j++){
				if((i*outgoingdegree+j)/c_length!=0){
					assign[i][(i*outgoingdegree+j)%c_length] = 2;
				}else{
					assign[i][(i*outgoingdegree+j)%c_length] = 1;
				}

			}
		}
		// 

		int columnTotal[] = new int[c_length];
		for(int i = 0; i < p_length; i++){
			for(int j = 0; j < c_length; j++){
				columnTotal[j] = columnTotal[j] + assign[i][j]; 
			}
		}

		for(int i = 0; i < p_length; i++){
			for(int j = outgoingdegree; j <c_length; j++ ){
				if(columnTotal[j]==1){
					if(assign[i][j]!=1){
						assign[i][j] = 2;
						columnTotal[j]=2;
						break;
					}
				}
			}
		}



		for(int i = 0; i < p_length; i++ ){
			for(int j = 0; j < c_length; j++){
				if(assign[i][j] == 1){
					addEdge(parent[i],0,children[j],0);
				}
				if(assign[i][j]==2){
					addEdge(parent[i],0,children[j],1);
				}
			}
		}



	}
	/*   1   2    3
	 * 	 |   |    |	
	 *   x   y    z
	 */


	private void addEdges_OneToOneMapping(Task[] parent, Task[] children){
		for(int i=0; i < parent.length; i++ ){
			addEdge(parent[i],0,children[i],1);
		}
	}


}
