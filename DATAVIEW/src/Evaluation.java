import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dataview.models.*;

public class Evaluation extends Task{
	/*
	 * The constructor will decide how many inputports and how many outputports and the detailed information of each port.
	 */
	ArrayList<ArrayList<String>> trueDiagnosisList = new ArrayList<ArrayList<String>>();
	ArrayList<ArrayList<String>> recommendedDiagnosisList = new ArrayList<ArrayList<String>>();
	public Evaluation()
	{
		super("Evaluation", "This is a task that evaluates the algorithm. It has two inputports and one outputport.");
		ins = new InputPort[2];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_String, "This is the first number");
		ins[1] = new InputPort("in1", Port.DATAVIEW_String, "This is the second number");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_String, "This is the output");	
		
	}
	
	public void run() 
	{
		
		
		// step 1: read from the input ports
		String input0 = (String) ins[0].read();
		String input1 = (String) ins[1].read();
		
		// step 2: computation of the function
		
		readDiagnosisLabels(input0, input1);
		Map<String, Double> map=new HashMap<String, Double>(); 
		map.put("Precision@1 ", calculatePrecision(1));
		map.put("Precision@2 ", calculatePrecision(2));
		map.put("Precision@3 ", calculatePrecision(3));
		map.put("Precision@4 ", calculatePrecision(4));
		
		map.put("Recall@1 ", calculateRecall(1));
		map.put("Recall@2 ", calculateRecall(2));
		map.put("Recall@3 ", calculateRecall(3));
		ObjectMapper mapper = new ObjectMapper();
		String output0 = "";
		try {
			output0 = mapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	
		// step 3: write to the output port
		outs[0].write(output0);	
		
	}

	private double calculateRecall(int topK) {
		double recallSum = 0;
		for (int i = 0; i < trueDiagnosisList.size(); i++) {
			double count = 0;
			double matched = 0;
			HashSet<String> set = new HashSet<String>();
			for (String reco : trueDiagnosisList.get(i)) {
				set.add(reco);
			}
			for (int j = 0; j < recommendedDiagnosisList.get(i).size() && j < topK; j++) {
				
				if (set.contains(recommendedDiagnosisList.get(i).get(j))) {
					matched++;
				}
				count++;
			}
			/*if (count < topK) {
				matched += (topK - count);
			}*/
			recallSum += (matched / trueDiagnosisList.get(i).size());
		}
		
		return recallSum * 100 / (double)trueDiagnosisList.size();
	}
	
	double calculatePrecision(int topK) {
		double precisionSum = 0;
		for (int i = 0; i < trueDiagnosisList.size(); i++) {
			double count = 0;
			double matched = 0;
			HashSet<String> set = new HashSet<String>();
			for (String reco : trueDiagnosisList.get(i)) {
				set.add(reco);
			}
//			System.out.println("first " + i);
			for (int j = 0; j < recommendedDiagnosisList.get(i).size() && j < topK; j++) {
				if (set.contains(recommendedDiagnosisList.get(i).get(j))) {
					matched++;
				}
				count++;
			}
			/*if (count < topK) {
				matched += (topK - count);
			}*/
			precisionSum += (matched / topK);
		}
		
		return precisionSum * 100 / (double)trueDiagnosisList.size();
	}
	
	
	void readDiagnosisLabels(String fileTest, String fileTestPrime) {
		try {
			InputStream inputStream1 = new ByteArrayInputStream(fileTest.getBytes(Charset.forName("UTF-8")));
			BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(inputStream1));
			
			InputStream inputStream2 = new ByteArrayInputStream(fileTestPrime.getBytes(Charset.forName("UTF-8")));
			BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(inputStream2));
			
	
			String line;
			boolean isFound = false;
			while ((line = bufferedReader1.readLine()) != null) {
				bufferedReader2.readLine();
				if (line.equals("@data")) {
					isFound = true;
					break;
				}
			}
			
			while ((line = bufferedReader1.readLine()) != null) {
				ArrayList<String> diag = new ArrayList<String>(Arrays.asList(line.split("\"")[1].split("_")));
				trueDiagnosisList.add(diag);
			}
			
			while ((line = bufferedReader2.readLine()) != null) {
				
				if (line.split("\"").length == 2) {
					ArrayList<String> diag = new ArrayList<String>(Arrays.asList(line.split("\"")[1].split("_")));
					if (diag !=  null) {
						recommendedDiagnosisList.add(diag);
					}
				} else {
					recommendedDiagnosisList.add(new ArrayList<String>());
				}
			}
		
		} catch (Exception exception) {
			System.out.println("Exception Evaluation : " + exception);
		}

	}
	
}
