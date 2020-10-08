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

	final static  int num_mProectPP = 7;
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
