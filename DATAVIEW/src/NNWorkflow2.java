import java.util.ArrayList;
import java.util.List;

import dataview.models.*;

/**
 * A NNWorkflow takes Breast_cancer_data.csv as input
 * The input dataset should be reside in \WebContent\workflowTaskDir\ folder
 * @author Junwen Liu
 * */

public class NNWorkflow2 extends NNWorkflow{		
		public NNWorkflow2()
		{
			super("NNworkflow2", "This neural network workflow");	
			wins = new Object[1];
			wouts = new Object[1];
			wins[0] = "Breast_cancer_data.csv";
			wouts[0] = "finalprediction.txt";
		}
		
		public void design()
		{
			NNTask[] layers = new NNTask[4];
			
			layers[0] = new Linear(5,3);
			layers[1] = new ReLU();
			layers[2] = new Linear(3,1);
			layers[3] = new Sigmoid();
			
			Sequential(layers);
		}
}
