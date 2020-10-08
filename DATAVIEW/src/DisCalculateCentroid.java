import java.io.*;
import java.util.Vector;
import dataview.models.*;

public class DisCalculateCentroid extends Task{

	private int M = DisKMeansWorkflow.M; 
	private int K = DisKMeansWorkflow.K;
	
	private static int iteration = 1;

	public DisCalculateCentroid()
	{
		/*super("CalculateCentroid", "Calculate the centroid of a cluster.");
		ins = new InputPort[M * 2];
		outs = new OutputPort[M];
		for (int i = 0; i < ins.length; i++) {
			ins[i] = new InputPort("in" + i, Dataview.datatype.DATAVIEW_String, "This contains all the points total number of points with partial sum with respect to cluster");
		}

		for (int i = 0; i < outs.length; i++) {
			outs[i] = new OutputPort("out" + i, Dataview.datatype.DATAVIEW_String, "This will produce the centroid of each cluster");
		}*/

		super("CalculateCentroid", "Calculate the centroid of a cluster.");
		ins = new InputPort[M];
		outs = new OutputPort[M];
		for (int i = 0; i < ins.length; i++) {
			ins[i] = new InputPort("in" + i, Port.DATAVIEW_String, 
					"This contains all the points total number of points with partial sum with respect to cluster");
		}

		for (int i = 0; i < outs.length; i++) {
			outs[i] = new OutputPort("out" + i, Port.DATAVIEW_String, "This will produce the centroid of each cluster");
		}
	}


	

	public void run() {

		// read the input
		String[] inputs1 = new String[ins.length];
		String[] inputs = new String[ins.length * 2];
		int index = 0;
		for (int i = 0; i < ins.length; i++) {
			inputs1[i] = (String) ins[i].read();
			String[] parts = inputs1[i].split("XX");
			inputs[index++] = parts[0];
			inputs[index++] = parts[1];
		}
		
		
		

		int totalAttributes = 0;
		if (inputs.length > 1) {
			String lines[] = inputs[1].split("\\r?\\n");
			if (lines.length > 0) {
				totalAttributes = lines[0].split(" ").length;
			}

		}
		int[] totalPointsPerCluster = new int[K];
		int[][] partialSum = new int[K][totalAttributes];

		// total Points per cluster
		for (int i = 0; i < inputs.length; i += 2) {
			String values[] = inputs[i].split(" ");	
			for (int j = 0; j < values.length; j++) {
				totalPointsPerCluster[j] += Integer.parseInt(values[j].trim());
			}
		}


		// partial Sum
		for (int i = 1; i < inputs.length; i += 2) {
			String lines[] = inputs[i].split("\\r?\\n");	
			for (int j = 0; j < lines.length; j++) {
				String[] values = lines[j].split(" ");
				for (int k = 0; k < values.length; k++) {
					partialSum[j][k] += Integer.parseInt(values[k].trim());
				}
			}
		}

		// calculating centroids
		int[][] centroids = new int[K][totalAttributes];
		for (int i = 0; i < K; i++) {
			for (int j = 0; j < totalAttributes; j++) {
				if (totalPointsPerCluster[i] != 0) {
					centroids[i][j] = partialSum[i][j] / totalPointsPerCluster[i];
				}
			}
		}

		// writing centrids
		String centroidString = "";
		for (int i = 0; i < K; i++) {
			for (int j = 0; j < totalAttributes; j++) {
				centroidString += centroids[i][j];
				if (j != totalAttributes - 1) centroidString += " ";
			}
			if (i != K - 1) centroidString += "\n";
		}

		for (int i = 0; i < outs.length; i++) {
			outs[i].write(centroidString);
		}
		System.out.println("Iteration : " + (iteration++) );
	}	
}
