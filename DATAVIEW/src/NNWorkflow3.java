import dataview.models.*;

/**
 * A NNWorkflow takes creditcard.csv as input
 * The input dataset should be reside in \WebContent\workflowTaskDir\ folder
 * @author Junwen Liu
 * */

public class NNWorkflow3 extends NNWorkflow{		
		public NNWorkflow3()
		{
			super("NNworkflow3", "This neural network workflow");	
			wins = new Object[1];
			wouts = new Object[1];
			wins[0] = "creditcard.csv";
			wouts[0] = "finalprediction.txt";
		}
		
		
		public void design()
		{
			NNTask[] layers = new NNTask[8];
			
			layers[0] = new Linear(28,10);
			layers[1] = new ReLU();
			layers[2] = new Linear(10,5);
			layers[3] = new ReLU();
			layers[4] = new Linear(5,3);
			layers[5] = new ReLU();
			layers[6] = new Linear(3,1);
			layers[7] = new Sigmoid();
			
			//second parameter is for numOfbatches, third parameter is for numOfEpochs
			Sequential(layers);
		}
}
