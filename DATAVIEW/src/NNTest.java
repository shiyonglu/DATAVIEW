

import java.io.File;

import dataview.models.*;
import dataview.nntrainers.*;


/**
 *  This is the main entry to initialize a NNWorkflow, called design() method, feed into one of proper NNWorkflow trainer and call trainer.train() method.
 *  The result will be return from GPU side in JSON format as a String.
 * @author Junwen Liu
 * */

public class NNTest {

	public static void main(String[] args) throws Exception {
		
		long startTime = System.nanoTime();
		
		// step 1: create a workflow
		WorkflowVisualization frame = new WorkflowVisualization();	
//		NNWorkflow2 w = new NNWorkflow2();
//		NNWorkflow3 w = new NNWorkflow3();
		NNWorkflow6 w = new NNWorkflow6();
//		NNWorkflow7 w = new NNWorkflow7();
//		NNWorkflow8 w = new NNWorkflow8();
//		NNWorkflow9 w = new NNWorkflow9();

		// step 2: design a NNWorkflow
		w.design();
		frame.drawWorkflowGraph(w);
		
		//String API_Repo = System.getProperty("user.dir") + File.separator + "WebContent" +File.separator + "TrainerDLLs"+ File.separator;
		
		// step3: chose one of a NNWorkflow trainer 
		NNWorkflowTrainer_LocalGPU trainer = new NNWorkflowTrainer_LocalGPU(w, 6, 1000);
//		NNWorkflowTrainer_SingleXavier trainer = new NNWorkflowTrainer_SingleXavier(w, 6, 1000);
//		NNWorkflowTrainer_SingleNano trainer = new NNWorkflowTrainer_SingleNano(w, 6, 1000);
//		NNWorkflowTrainer_CrossValOnLocalGPU trainer = new NNWorkflowTrainer_CrossValOnLocalGPU(w, 6, 1000);
//		NNWorkflowTrainer_CrossValOnGPUCluster trainer = new NNWorkflowTrainer_CrossValOnGPUCluster(w, 6, 1000);
//		NNWorkflowTrainer_CrossValOnSingleXavier trainer = new NNWorkflowTrainer_CrossValOnSingleXavier(w, 6, 1000);
//		NNWorkflowTrainer_CrossValOnSingleNano trainer = new NNWorkflowTrainer_CrossValOnSingleNano(w, 6, 1000);
	
		// step4: execute NNTrainer
		String result = trainer.train();
		
		long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
 
        System.out.println("Execution time in seconds : " +
                                (double)timeElapsed / 1000000000);
		
		System.out.println("The trained model received in JAVA Side:\n" + result);
	}

}
