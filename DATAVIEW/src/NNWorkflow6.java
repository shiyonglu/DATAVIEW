import java.util.ArrayList;
import java.util.List;

import dataview.models.*;

/**
 * A NNWorkflow takes pima-indians-diabetes.csv as input
 * The input dataset should be reside in \WebContent\workflowTaskDir\ folder
 * @author Junwen Liu
 * */

public class NNWorkflow6 extends NNWorkflow{		
		public NNWorkflow6()
		{
			super("NNworkflow6", "This neural network workflow");	
			wins = new Object[1];
			wouts = new Object[1];
			wins[0] = "pima-indians-diabetes.csv";
			wouts[0] = "finalprediction.txt";
		}
		
		public void design()
		{
			NNTask[] layers = new NNTask[6];
			
			layers[0] = new Linear(8,5);
			layers[1] = new ReLU();
			layers[2] = new Linear(5,3);
			layers[3] = new ReLU();
			layers[4] = new Linear(3,1);
			layers[5] = new Sigmoid();
			
			Sequential(layers);
		}
}
