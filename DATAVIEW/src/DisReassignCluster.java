/* 
 * This step will reassign a point to a new cluster based on the centroid matrix, which stores the K vectors, each represents
 * a centroid of a cluster. 
 * 
 * Given the points in port ins[0], and the centroid matrix in ins[1], we reassign each point to a new cluster number and 
 * store the new assignment int port outs[0].
 * 
 * Author: Shiyong Lu
 * 7/10/2018
 * 
 * 
 * Big Data Research Laboratory 
 * Wayne State University
 * @All rights reserved.
 * 
 * Logs:
 *  o 7/10/2018, first release
 * 
 */

import dataview.models.*;

public class DisReassignCluster extends Task {
	private int K = DisKMeansWorkflow.K; // number of clusters

	public DisReassignCluster()
	{
		super("ReassignCluster_task", "Given the points in port ins[0], and the centroid matrix in ins[1], we reassign each point to a new cluster number");

		ins = new InputPort[2];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_String, "This is a points split, ");
		ins[1] = new InputPort("in1", Port.DATAVIEW_String, "This is a centroid matrix");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_String, "This is a new points split with new cluster number assigned.");				
	}

	public void run() {

		// read the input
		String[] inputs = new String[ins.length];
		for (int i = 0; i < ins.length; i++) {
			inputs[i] = (String) ins[i].read();
		}

		
		

		// construct centroids matrix
		
		int totalAttributes = 0;
		if (inputs.length > 1) {
			String lines[] = inputs[1].split("\\r?\\n");
			if (lines.length > 0) {
				String line = lines[0];
				totalAttributes = line.split(" ").length;
			}
		}
		int[][] centroids = new int[K][totalAttributes];
		
		String lines[] = inputs[1].split("\\r?\\n");
		for (int i = 0; i < lines.length; i++) {
			String[] values = lines[i].split(" ");
			for (int j = 1; j < values.length; j++) {
				centroids[i][j - 1] = Integer.parseInt(values[j]);
			}
		}
		
		
		
		// construct Input matrix without cluster ID
		
		String[] previousPoints = inputs[0].split("\\r?\\n");
		int[][] originalInput = new int[previousPoints.length][totalAttributes];
		for (int i = 0; i < previousPoints.length; i++) {
			String[] values = previousPoints[i].split(" ");
			for (int j = 1; j < values.length; j++) {
				originalInput[i][j - 1] = Integer.parseInt(values[j]);
			}
		}
		
		
		
		// assigning new clusterId
		
		
		int[] clusterId = new int[previousPoints.length];
		
		for (int i = 0; i < previousPoints.length; i++) {
			String[] val = previousPoints[i].split(" ");
			int[] values = new int[val.length - 1];
			for (int j = 1; j < val.length; j++) values[j - 1] = Integer.parseInt(val[j]);
			
			int closestDistance = Integer.MAX_VALUE;
			int closestClusterId = 0;
			for (int j = 0; j < K; j++) {
				int distance = 0;
				for (int k = 0; k < totalAttributes; k++) {
					distance += Math.pow(centroids[j][k] - values[k], 2);
				}
				if (closestDistance > distance) {
					closestDistance = distance;
					closestClusterId = j;
				}
			}
			clusterId[i] = closestClusterId;
		}


		// writing output
		String output = "";
		for (int i = 0; i < originalInput.length; i++) {
			output += clusterId[i] + " ";
			for (int j = 0; j < originalInput[i].length; j++) {
				output += originalInput[i][j];
				if (j != originalInput[i].length - 1) output += " ";
			}
			if (i != originalInput.length - 1) output += "\n";
		}
		outs[0].write(output);
	}
}
