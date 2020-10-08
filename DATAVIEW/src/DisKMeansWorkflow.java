


import dataview.models.DATAVIEW_BigFile;
import dataview.models.Task;
import dataview.models.Workflow;

public class DisKMeansWorkflow extends Workflow {

	public static int K = 2; // number of clusters
	public static int M = 1; // number splitting datasets/ partitions
	public static int iteration = 2; // number of iteration


	public DisKMeansWorkflow() {
		super("Distributed K-means", " Calculating K means clustering for huge number of datasets");
		wins = new Object[1];
		wins[0] = new DATAVIEW_BigFile("KMeansInput.txt");
		wouts = new Object[M];
		for(int i = 0; i < M; i++){
			wouts[i] = new DATAVIEW_BigFile("KMoutput" + i + ".txt");
		}
		
	}
	public void design()
	{

		Task T1 = addTask("DisKMeansInitialization");

		Task[] T2 = new Task[M * iteration];
		Task[] T3 = new Task[iteration];
		Task[] T4 = new Task[M * iteration];

		for (int i = 0; i < M * iteration; i++) {
			T2[i] = addTask("DisCalculateCentroidStep1");
			T4[i] = addTask("DisReassignCluster");
		}

		for (int i = 0; i < iteration; i++) {
			T3[i] = addTask("DisCalculateCentroid");
		}
		
		
		addEdge(0,T1,0);
		//addEdge("originalInput.enc", T1, 0);

		for (int i = 0; i < M; i++) {
			addEdge(T1, i, T2[i], 0);
		}



		for (int it = 0; it < iteration; it++) {
			// Step1 to Centroit && Step1 to Reassign
			int index = it * M;
			for (int i = 0; i < M; i++) {
				addEdge(T2[index + i], 0, T3[it], i);
				addEdge(T2[index + i], 1, T4[index + i], 0);
			}

			for (int i = 0; i < M; i++) {
				addEdge(T3[it], i, T4[index + i], 1);
			}

			for (int i = 0; i < M; i++) {
				if (it != iteration - 1){
					addEdge(T4[index + i], 0, T2[(it + 1) * M + i], 0);
				}else{
					addEdge(T4[index + i], 0, i);
				}
			}
		}



			/*for (int count = 0; count < iteration; count++) {
			int index = 0;
			int i = count * M;
			int limitT2 = i + M; 
			for (i = count * M; i < limitT2; i++) {
				addEdge(T2[i], 0, T3[count], index++);
				//addEdge(T2[i], 1, T3[count], index++);
				addEdge(T2[i], 1, T4[i], 0);
			}
			i = count * M;
			int limitT4 = i + M;
			for (i = count * M; i < limitT4; i++) {
				addEdge(T3[count], i, T4[i], 1);
			}


			if (count == iteration - 1) {
				for (i = count * M; i < limitT4; i++) {
					addEdge(T4[i], 0, "output" + i+ ".txt");
				}
			} else {
				index = (count + 1) * M;
				for (i = count * M; i < limitT4; i++) {
					addEdge(T4[i], 0, T2[index++], 0);
				}
			}
		}*/
		}

	}
