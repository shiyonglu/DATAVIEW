import java.util.ArrayList;
import java.util.List;

import dataview.models.*;

/**
 * A NNWorkflow takes Data_for_UCI_named.csv as input
 * The input dataset should be reside in \WebContent\workflowTaskDir\ folder
 * @author Junwen Liu
 * */

public class NNWorkflow8 extends NNWorkflow{		
		public NNWorkflow8()
		{
			super("NNworkflow8", "This neural network workflow");	
			wins = new Object[1];
			wouts = new Object[1];
			wins[0] = "Data_for_UCI_named.csv";
			wouts[0] = "finalprediction.txt";
		}
		
		public void design()
		{
			NNTask[] layers = new NNTask[8];
			
			layers[0] = new Linear(13,8);
			layers[1] = new ReLU();
			layers[2] = new Linear(8,5);
			layers[3] = new ReLU();
			layers[4] = new Linear(5,3);
			layers[5] = new ReLU();
			layers[6] = new Linear(3,1);
			layers[7] = new Sigmoid();
			
			Sequential(layers);
		}
}
