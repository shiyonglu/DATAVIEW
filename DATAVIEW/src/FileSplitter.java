import java.io.*;
import dataview.models.*;


/* FileSplitter will split a file into K files that are (almost) equal in size */
/* task is called a parameterized task, the parameter K must be instantiated to a concrete value by a constructor. */
public class FileSplitter extends Task{
	private int K = 2; // output port numbers, which is same to the WordCount task numbers in the second stage.
	
	public FileSplitter ()
	{
		super("FileSplitter", "FileSplitter will split a file into K files that are (almost) equal in size ");
		ins = new InputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_BigFile, "This is the first number");
		outs = new OutputPort[K];
		for(int i=0; i<K; i++)
			outs[i] = new OutputPort("out"+i, Port.DATAVIEW_BigFile, "This is the "+i+"th output");				
	}
	
	public static int countLines(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}	
	
	public void run()
	{
		
		// step 1: read from the input ports
		String inputfile = (String)ins[0].getFileName();	
		int numlines =0;
		try {
			numlines = countLines(inputfile);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			Dataview.debugger.logException(e2);
		}
		
		Dataview.debugger.logObjectValue("K",  K);
		int linesperfile = (numlines/K)+1;  // the last file might have less lines
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inputfile));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Dataview.debugger.logException(e1);
		}

		
		// write to K output files
		for(int i=0; i<K; i++)
		{
			String filename = (String)outs[i].getFileName();
			Dataview.debugger.logObjectValue("filename",  filename);
			File outputfile = new File(filename);
			
			try {
				 if(!outputfile.exists()) outputfile.createNewFile();
				
				 FileWriter fw = new FileWriter(outputfile.getAbsoluteFile(), false);
				 BufferedWriter bw = new BufferedWriter(fw);
				
				 
				 for(int j=0; j<linesperfile; j++) { // copy linesperfile lines to the outputfile
					 String line = br.readLine();
				     if(line == null) break;
				     else {bw.write(line); bw.newLine();}
				 }
					 			   
				bw.close();
				fw.close();
			} catch(IOException e) {
				e.printStackTrace();
				Dataview.debugger.logException(e);
			}	
		}
		
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}
		
		// step 3: write to the output port
		// Omitted as we are done with writing to the output files.
	}
}
