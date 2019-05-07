import java.util.Random;

import dataview.models.Dataview;
import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;


/*The number of mProjectPP jobs (which re-project the input image) is equal to the number of input FITS images processed. 
 * Two inputs and two outputs are included in the task. 
 * The outputs are the re-projected image and an “area” image that consists 
 * of the fraction of the image that belongs in the final mosaic.
 * @inputs: region.hdr, FITS format file
 * @output: FITS format file and FITS area 
 */

public class mProjectPP extends Task{
	public mProjectPP(){
		super("mProjectPP","re-project the input image");
		ins = new InputPort[1];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_String, "region.hdr and the FITS format file");
		outs[0] = new OutputPort("out1", Port.DATAVIEW_String, "FITS format file and the FITS area ");
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
	private static String toString(int[] array) {
	      StringBuilder sb = new StringBuilder();
	      for (int i = 0; i < array.length; i++) {
	         sb.append(array[i]);
	         sb.append(" ");
	      }
	      return sb.toString();
	   }
}
