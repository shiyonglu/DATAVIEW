import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dataview.models.Dataview;
import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;


public class Extraction extends Task{
	/*
	 * The constructor will decide how many inputports and how many outputports and the detailed information of each port.
	 */
	
	public Extraction()
	{
		super("Extraction", "This is a task that implements Extraction numerical information from txt file. It has one inputports and one outputport.");
		ins = new InputPort[1];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_String, "This is the first number");
		//ins[1] = new InputPort("2in", "Integer", "This is the second number");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_String, "This is the output");		
	}
	
	public void run()
	{
		// step 1: read from the input ports
		String input0 = (String) ins[0].read();
		Dataview.debugger.logSuccessfulMessage("here is the input0" + input0);
		
		// step 2: computation of the function
		
		ArrayList<PatientDetails> patientList = new ArrayList<PatientDetails>();
		String lines[] = input0.split("\\r?\\n");
				
		int numofline = lines.length;
		
		for(int i = 0; i < numofline; i++ ){
			
			PatientDetails patientDetails = new PatientDetails();
			// Finding Age
			patientDetails.setAge(retrieveValue(lines[i], "age"));
			// Finding heartRate
			patientDetails.setHeartRate(retrieveValue(lines[i], "heartRate"));
			// Finding isObesity
			patientDetails.setObese(lines[i].contains("has obesity") ? true : false);
			// Finding isSmoke
			patientDetails.setSmoke(lines[i].contains("Patient is smoker") ? true : false);
			// Finding glucose
			patientDetails.setGlucose(retrieveValue(lines[i], "glucose"));
			//Finding diagnosisLabel
			patientDetails.setDiagnosisLabels(retrieveDiagnosisLabels(lines[i]));
			patientList.add(patientDetails);
		}
		
		StringBuilder stringBuilder = new StringBuilder();
		for (PatientDetails patientDetails:patientList){
			stringBuilder.append(patientDetails.getAge() + ",");
			stringBuilder.append(patientDetails.getHeartRate() + ",");
			stringBuilder.append((patientDetails.isObese() ? "Yes" : "No") + ",");
			stringBuilder.append((patientDetails.isSmoke() ? "Yes" : "No") + ",");
			stringBuilder.append(patientDetails.getGlucose() + ",\"");
			
			for (int i = 0; i < patientDetails.getDiagnosisLabels().size(); i++) {
				stringBuilder.append(patientDetails.getDiagnosisLabels().get(i).trim());
				if (i < patientDetails.getDiagnosisLabels().size() - 1) {
					stringBuilder.append("_");
				}
			}
			stringBuilder.append("\"\n");
			
		}
		String output0 = stringBuilder.toString();
	
		// step 3: write to the output port
		outs[0].write(output0);			
	}
	
	private ArrayList<String> retrieveDiagnosisLabels(String string) {
		// TODO Auto-generated method stub
		ArrayList<String> diagnosisLabels = new ArrayList<String>();
		String[] diagnosisLabelList = string.split(":")[1].split("_"); 
		diagnosisLabels.addAll(new ArrayList<String>(Arrays.asList(diagnosisLabelList)));
		return diagnosisLabels;	
	}
	private int retrieveValue(String line, String category) {
		Pattern pattern = null;
		if(category.equals("age")) {
			pattern = Pattern.compile("([0-9]{1,3}\\syear(s?)\\sold)", Pattern.CASE_INSENSITIVE); 
		} else if (category.equals("heartRate")) {
			pattern = Pattern.compile("(HeartRate\\sis\\s[0-9]{1,3})", Pattern.CASE_INSENSITIVE);
		} else if (category.equals("glucose")) {
			pattern = Pattern.compile("(Glucose\\sis\\s[0-9]{1,3})", Pattern.CASE_INSENSITIVE);
		}
		Matcher matcher = pattern.matcher(line);
		String value = "";
		while (matcher.find()) {
			String group = matcher.group();
			for (int i = 0; i < group.length(); i++) {
				if(Character.isDigit(group.charAt(i))) {
					value += group.charAt(i);
				}
			}
			break;
		}
		return Integer.parseInt(value);
	}
	 class PatientDetails {
		int age;
		int heartRate;
		int glucose;
		boolean isSmoke;
		boolean isObese;
		ArrayList<String> diagnosisLabels;
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
		public int getHeartRate() {
			return heartRate;
		}
		public void setHeartRate(int heartRate) {
			this.heartRate = heartRate;
		}
		public int getGlucose() {
			return glucose;
		}
		public void setGlucose(int glucose) {
			this.glucose = glucose;
		}
		public boolean isSmoke() {
			return isSmoke;
		}
		public void setSmoke(boolean isSmoke) {
			this.isSmoke = isSmoke;
		}
		public boolean isObese() {
			return isObese;
		}
		public void setObese(boolean isObese) {
			this.isObese = isObese;
		}
		public ArrayList<String> getDiagnosisLabels() {
			return diagnosisLabels;
		}
		public void setDiagnosisLabels(ArrayList<String> diagnosisLabels) {
			this.diagnosisLabels = diagnosisLabels;
		}
		
	}
}
