import java.util.Random;

import dataview.models.Dataview;
import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;


public class TmpltBank extends Task{
	public int num_TmpltBank=6;
	public TmpltBank(){
		super("TmpltBank","");
		ins = new InputPort[num_TmpltBank];
		outs = new OutputPort[1];
		for(int i = 0; i < num_TmpltBank; i++){
			ins[i] = new InputPort("in" + i, Port.DATAVIEW_BigFile, "");
		}
		
		outs[0] = new OutputPort("out0", Port.DATAVIEW_BigFile, "");
	}
	
	@Override
	public void run() {
		Random rand = new Random(1000);
		int totalNumber = 50000;
		/*
		int arr[] = new int[totalNumber];
		int arr1[] = new int[totalNumber];
		for (int i = 0; i < totalNumber; i++) {
			arr[i] = rand.nextInt(Integer.MAX_VALUE) + 1;
			arr1[i] = rand.nextInt(Integer.MAX_VALUE) + 1;
		}
		
		int arr2[] = new int[totalNumber];
		for(int i = 0; i < totalNumber; i++){
			arr2[i] = arr[i] + arr1[i]; 
		}
		*/
		int arr1[] = new int[totalNumber];
		for (int i = 0; i < totalNumber; i++) {
			arr1[i] = rand.nextInt(Integer.MAX_VALUE) + 1;
		}
		int temp1;
		for (int i = 0; i < totalNumber; i++) {
			for (int j = 0; j < totalNumber; j++) {
				if (arr1[i] > arr1[j]) {
					temp1 = arr1[i];
					arr1[i] = arr1[j];
					arr1[j] = temp1;
				}
			}
		}
		
		int arr[] = new int[totalNumber];
		for (int i = 0; i < totalNumber; i++) {
			arr[i] = rand.nextInt(Integer.MAX_VALUE) + 1;
		}
		int temp;
		for (int i = 0; i < totalNumber; i++) {
			for (int j = 0; j < totalNumber; j++) {
				if (arr[i] > arr[j]) {
					temp = arr[i];
					arr[i] = arr[j];
					arr[j] = temp;
				}
			}
		}
		
		//outs[0].write(toString(arr));
	}

}
