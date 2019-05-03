import java.io.*;
import dataview.models.*;
import java.util.HashMap;
import java.util.Scanner;

public class WordCount extends Task{
	
	public WordCount ()
	{
		super("WordCount", "Word Count will count the frequency of each word. ");
		ins = new InputPort[1];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_BigFile, "This is the first number");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_BigFile, "This is the output");		
	}
	
	public void run()
	{
	    // step 1: read    
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader((String)ins[0].getFileName()));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Dataview.debugger.logException(e1);
		}
	    
	    
	    HashMap<String, Integer> hm = new HashMap<String, Integer>();
	    
	    String line = null; 
	    try {
			while((line = br.readLine()) != null){
				System.out.println("line:" + line);
				String[] words = line.split(" ");
				for(String w: words) {
					w = w.replaceAll("[^a-zA-Z]", "");
					if(hm.containsKey(w)) {
						int old = hm.get(w);
					    hm.put(w, old+1);
					}
					else
						hm.put(w,  1);
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // end while			
	    
	    //System.out.println("xxxxxxxxxxxxxxx");
	    Dataview.debugger.logObjectValue("hm",  hm);
    	
	    // step 3: write to the output
	    File outputfile = new File((String)outs[0].getFileName());
		try {
			 if(!outputfile.exists()) outputfile.createNewFile();
			
			 FileWriter fw = new FileWriter(outputfile.getAbsoluteFile(), false);
			 BufferedWriter bw = new BufferedWriter(fw);
			
			 
			 for (String w: hm.keySet()){
	            bw.write(w+":"+hm.get(w)); bw.newLine();
			 }
			 
			 bw.close(); 
			 fw.close();
		} catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}
	}
}
