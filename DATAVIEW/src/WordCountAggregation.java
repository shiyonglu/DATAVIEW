import java.io.*;
import dataview.models.*;
import java.util.HashMap;
import java.util.Map;

public class WordCountAggregation extends Task{
	private int K;   // input port numbers
	
	public WordCountAggregation()
	{
		super("WordCountAggregation", "Aggregate all the counts from the prevous counting output");
		K = 2;
		ins = new InputPort[K];
		outs = new OutputPort[1];
		outs[0] = new OutputPort("in0", Port.DATAVIEW_HashMap, "This is the frequency of each word");
		for(int i=0; i<K; i++)
			ins[i] = new InputPort("in"+i, Port.DATAVIEW_HashMap, "This is the "+i+"th input");		
	}
	
	
	@SuppressWarnings("unchecked")
	public void run()
	{
		Map[]  hm = new Map[K];
		HashMap<String, Integer> outhm = new HashMap<String, Integer>();
		// step 1: read from the input ports
		for(int i=0; i<K; i++) {
			hm[i] = (HashMap<String, String>) ins[i].read();
			for(Object w: hm[i].keySet()) {
				Integer freq = Integer.valueOf((String)hm[i].get(w));
				if(outhm.containsKey(w)) {
					Integer old = outhm.get(w);
				    outhm.put((String)w, new Integer(old+freq));
				}
				else
					outhm.put((String)w,  freq);		
			}
		} // for each input hashmap

	
	    HashMap<String, String> finalhm = new HashMap<String, String>();
		for(String w: outhm.keySet()) {
			finalhm.put(w, outhm.get(w).toString());
		}
			
		
		// step 3: write the final HashMap<String, String> object to the output port
		outs[0].write(finalhm);
	}
}
