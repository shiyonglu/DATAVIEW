/* 
 * This step will take a points table and then calculate the partial sum of each cluster. 
 * 
 * The output will contain for each cluster, the partial number of points in that cluster, and the partial sum vector
 * The output will be used as the input for a subsequent task to calculate the centroid of each cluster.
 *  
 * Input ports: ins[0]: the points table, each row represents the ID of the point, the cluster number, and the vector of the point:
 * Output ports: outs[i], i=0, ..., K-1: the number of points in cluster i, and the partial sum vector of cluster i
 *  
 * 
 * Author: Ishtiaq Ahmed
 * 03/01/2019
 * 
 * 
 * Big Data Research Laboratory 
 * Wayne State University
 * @All rights reserved.
 * 
 * Logs:
 *  o 03/01/2019, first release
 * 
 */
import java.io.*;

import dataview.models.*;

public class DisCalculateCentroidStep1 extends Task{
	private int K = DisKMeansWorkflow.K;
	private static int counterObject = 1;

	public DisCalculateCentroidStep1()
	{ 
		/*super("CalculateCentroid_step1", "Calcualte the partial sum of each cluster");
		ins = new InputPort[1];
		outs = new OutputPort[3];
		ins[0] = new InputPort("in0", Dataview.datatype.DATAVIEW_String, "This is the first number");

		outs[0] = new OutputPort("out0", Dataview.datatype.DATAVIEW_String, "number of points per cluster");
		outs[1] = new OutputPort("out1", Dataview.datatype.DATAVIEW_String, "partial sum of each cluster");
		outs[2] = new OutputPort("out2", Dataview.datatype.DATAVIEW_String, "original input file "); */
		
		super("CalculateCentroid_step1", "Calcualte the partial sum of each cluster");
		ins = new InputPort[1];
		outs = new OutputPort[2];
		ins[0] = new InputPort("in0", Port.DATAVIEW_String, "This is the first number");

		outs[0] = new OutputPort("out0", Port.DATAVIEW_String, "number of points per cluster & partial sum of each cluster");
		outs[1] = new OutputPort("out1", Port.DATAVIEW_String, "original input file "); 
		
		



	}

	public void run()
	{
		// read the input
		String input0 = (String) ins[0].read();
		

		String lines[] = input0.split("\\r?\\n");
		int totalAttributes = lines.length > 0 ? lines[0].split(" ").length - 1 : 0;

		int[][] partialSum = new int[K][totalAttributes];
		int[] totalPointsPerCluster = new int[K];
		for (int i = 0; i < lines.length; i++) {
			String[] nums = lines[i].split(" ");
			totalPointsPerCluster[Integer.parseInt(nums[0].trim())]++;
			for (int j = 1; j < nums.length; j++) {
				partialSum[Integer.parseInt(nums[0].trim())][j - 1] += Integer.parseInt(nums[j].trim()); 				
			}
		}

		String pointsPerCluster = "";
		String partialSumPerCluster = "";

		for (int i = 0; i < K; i++) {
			pointsPerCluster += totalPointsPerCluster[i];
			if (i == K - 1) pointsPerCluster += "\n";
			else pointsPerCluster += " ";
			for (int j = 0; j < totalAttributes; j++) {
				partialSumPerCluster += partialSum[i][j];
				if (j == totalAttributes - 1) partialSumPerCluster += "\n";
				else partialSumPerCluster += " ";
			}

		}
		/*
		outs[0].write1(pointsPerCluster);
		outs[1].write1(partialSumPerCluster); 
		outs[2].write1(input0);*/
		
		outs[0].write(pointsPerCluster + "XX" + partialSumPerCluster);
		outs[1].write(input0);
		
		try {
			/*if (counterObject++ % 2 == 0)Thread.sleep(2000);
			else */Thread.sleep(900);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
