package dataview.planners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import dataview.models.Dataview;
import dataview.models.GlobalSchedule;
import dataview.models.JSONArray;
import dataview.models.JSONObject;
import dataview.models.JSONParser;
import dataview.models.LocalSchedule;
import dataview.models.Task;
import dataview.models.TaskSchedule;
import dataview.models.Workflow;
import dataview.models.WorkflowEdge;

public class WorkflowPlanner_LPOD extends WorkflowPlanner {
	/**
	 * The workflow configuration file stores the task execution time and data transfer time based on the real task name, but tasks composing a workflow may have the same task name but different task indexes.
	 *    execTimeTemp: stores the task execution time extracted from the workflow configuration file, where tasks are indicated by their unique names.
	 *     edgeMapTemp: stores the data transfer time extracted from the workflow configuration file, where edges are connected between tasks indicated by the real task names.
	 *        execTime: stores the task execution time of each task instance in a workflow, which is represented by an index.
	 *         edgeMap: stores the data transfer time of each edge connected in a workflow, which connects two task instances represented by index.
	 *     nodeParents: stores all the direct parent tasks given a task instance index.
	 *topoLogicalOrder: stores the topological order information of all the task instances, which determines the task execution order.
	 *		  taskTime: stores the time information for a task instance, including the MET, EST, EFT and LFT.
	 *         VMTypes: stores the VM types information. 
	 * local_schedules: stores a list of local schedule.
	 *         t_entry: stores the entry task instance index.
	 *          t_exit: stores the exit task instance index.
	 *          	 D: stores the user defined deadline of a workflow.
	 *   time_interval: stores the length of a billing cycle.
	 *     server_cost: stores the cost of each VM type.
	 *        VMADelay: stores the VM delay time of each VM instance after the provisioning.
	 *              VM: represents each VM instance, which stores the VM type information, the available time information, the left time of current billing cycle, the provisioning time, deprovisioning time and all the task instances running on it.
	 *            TASK: represents each task instance, which stores the task instance index, the actual start time and the actual finish time. 
	 *             sch: represents the workflow schedule generated based on the workflow planner.
	 */
	Map<String, Map<String, Double>> execTimeTemp = new HashMap<String, Map<String, Double>>();
	Map<String, Map<String, Double>> edgeMapTemp = new HashMap<String, Map<String, Double>>(); 
	private Map<Integer, Map<Integer, Double>> edgeMap = new HashMap<Integer,  Map<Integer, Double>>(); // store the edge information 
	private Map<Integer, Map<String, Double>> execTime = new HashMap<Integer, Map<String, Double>>(); // store the task execution time on each VM type
	private Map<Integer, List<Integer>> nodeParents = new HashMap<Integer, List<Integer>>();
	private List<Integer> topoLogicalOrder = new ArrayList<Integer>();
	private Map<Integer, List<Double>> taskTime = new HashMap<Integer, List<Double>>();
	private ArrayList<String> VMTypes;
	List<LocalSchedule> local_schedules = new ArrayList<LocalSchedule>();
	private static Integer t_entry = 0; 
	private static Integer t_exit = -1;
	private static double D;
	private static double time_interval;
	private static Map<String, Double> server_cost = new HashMap<String, Double>();
	private static double VMADelay;
	class VM{
		String vmType;
		double available;
		double vleft;
		double pro;
		double depro;
		ArrayList<Integer> runTask = new ArrayList<Integer>();
	}
	class TASK{
		Integer t;
		double ast;
		double aft;
	}	
	static class sch{
		static ArrayList<TASK> T = new ArrayList<TASK>();
		static ArrayList<VM> VMpool = new ArrayList<VM>();	
	}
	/**
	 * A constructor for the LPOD planner.
	 * @param w a workflow object
	 */
	public WorkflowPlanner_LPOD(Workflow w) {
		super(w);
		execTimeTemp = w.getExecutionTime();
		System.out.println(execTimeTemp);
		edgeMapTemp = w.getTransferTime();
		System.out.println(edgeMapTemp);
		VMTypes = new ArrayList<String>(execTimeTemp.get(execTimeTemp.keySet().iterator().next()).keySet());
		VMADelay = 1;
		D = 50;
		time_interval = 10;
		server_cost.put("VM1", 5.0);
		server_cost.put("VM2", 2.0);
		server_cost.put("VM3", 1.0);
	}
	/**
	 * A constructor for the LPOD planner fills in the execTimeTemp and edgeMapTemp; assigns the deadline, billing cycle, server_cost and VM delay information.
	 * @param w a workflow object
	 * @param location the workflow configuration file location.
	 * 
	 */
	public WorkflowPlanner_LPOD(Workflow w, String location){
		super(w);
		String workflowname = w.workflowName;
		File file = new File(location + workflowname + ".json");
		if(file.exists()){
			String content = null;
			StringBuilder contentBuilder = new StringBuilder();
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(file));
				String sCurrentLine;
			    while ((sCurrentLine = br.readLine()) != null){
			        	contentBuilder.append(sCurrentLine).append("\n");
			    } 
			    br.close();
			} catch (Exception e) {
				Dataview.debugger.logException(e);
			}
			content = contentBuilder.toString();
			JSONParser jsonParser = new JSONParser(content);
			JSONObject workflowobj = jsonParser.parseJSONObject();
			JSONObject taskobj = workflowobj.get("Execution").toJSONObject();
			for(String str: taskobj.keySet()){
				JSONArray jarraytasks = taskobj.get(str).toJSONArray();
				for (int i = 0; i < jarraytasks.size(); i++) {
					JSONObject obj = jarraytasks.get(i).toJSONObject();
					String vm = (String) obj.keySet().toArray()[0];
					Double exeTime = Double.parseDouble(obj.get(vm).toString().replace("\"", ""));
					Map<String, Double> tmp;
					if (execTimeTemp.containsKey(str)) {
						tmp = execTimeTemp.get(str);
						tmp.put(vm, exeTime);
					} else {
						tmp = new HashMap<String, Double>();
						tmp.put(vm, exeTime);
						execTimeTemp.put(str, tmp);
					}
					System.out.println("the task: " + str + " is running on vm " + vm + " :" + exeTime);
				}
			}
			JSONObject jsonedge = workflowobj.get("Tasktransfer").toJSONObject();
			for(String str: jsonedge.keySet()){
				JSONArray jarrayedge  = jsonedge.get(str).toJSONArray();
				
				for (int i = 0; i < jarrayedge.size(); i++) {
					JSONObject obj = jarrayedge.get(i).toJSONObject();
					String childTask = obj.get("To").toString().replace("\"", "");
					Double transferTime = Double.parseDouble(obj.get("Trans").toString().replace("\"", ""));
					Map<String, Double> tmp;
					if (edgeMapTemp.containsKey(str)) {
						tmp = edgeMapTemp.get(str);
						tmp.put(childTask, transferTime);
					} else {
						tmp = new HashMap<String, Double>();
						tmp.put(childTask, transferTime);
						edgeMapTemp.put(str, tmp);
					}
				}
		}
		String onetaskname = new ArrayList<String>(execTimeTemp.keySet()).get(0);
		VMTypes = new ArrayList<String>(execTimeTemp.get(onetaskname).keySet());
		VMADelay = 1;	
		D =240;
		time_interval = 10;
		server_cost.put("t2.xlarge", 5.0);
		server_cost.put("t2.medium", 2.0);
		server_cost.put("t2.micro", 1.0);
		
		
		}	
	}
	
	/**
	 * This method sort the task instances indexes in a topological order starting with the entry task index in a recursive way.
	 */
	private void doTopologicalSorting() {
		Queue<Integer> vertexWithoutIncoming = new LinkedList<Integer>();
		vertexWithoutIncoming.add(t_entry);
		while (!vertexWithoutIncoming.isEmpty()) {
			Integer tmp = vertexWithoutIncoming.poll();
			topoLogicalOrder.add(tmp);
			if (edgeMap.containsKey(tmp)) {
				for (Integer key : edgeMap.get(tmp).keySet()) {
					if (nodeParents.containsKey(key)) {
						boolean indicator = true;
						for (Integer st : nodeParents.get(key)) {
							if (!topoLogicalOrder.contains(st)) {
								indicator = false;
								break;
							}
						}
						if (indicator) {
							vertexWithoutIncoming.add(key);
						}
					}
				}
			}
		}

	}
	
	/**
	 * This method calculates the upward rank value for each task instance.
	 * @return a hashmap stores the upward rank value of each task
	 */
	private Map<Integer, Double> getProirityValue() {
		Map<Integer, Double> priorityvalue = new HashMap<Integer, Double>();
		this.doTopologicalSorting();
		for (int i = topoLogicalOrder.size()-1; i>=0; i-- ){
			if(topoLogicalOrder.get(i).equals(-1)){
				priorityvalue.put(-1, 0.0);
			}
			else if(topoLogicalOrder.get(i).equals(0)){}
			else{
				Map<Integer, Double> children = edgeMap.get(topoLogicalOrder.get(i));
				Set<Integer> childrennodes = children.keySet();
				Iterator<Integer> iterator = childrennodes.iterator();
				double max = Double.NEGATIVE_INFINITY;
				while (iterator.hasNext()) {
					Integer child = iterator.next();
					if(priorityvalue.get(child)+ edgeMap.get(topoLogicalOrder.get(i)).get(child)>max){
						max = priorityvalue.get(child)+ edgeMap.get(topoLogicalOrder.get(i)).get(child);
					}
				}
				Collection<Double> exetimes = execTime.get(topoLogicalOrder.get(i)).values();
				priorityvalue.put(topoLogicalOrder.get(i), max + ave_exetime(exetimes));
			}	
		}
		return priorityvalue;
	}
	/**
	 * This method calculates the average execution time for a task instance on all VM types. 
	 * @param times all the execution times on all VM types
	 * @return the average task execution time.
	 */
	private double ave_exetime(Collection<Double> times) {
		Double exectime = (double) 0;
		int number = 0;
		Iterator<Double> iteroator = times.iterator();
		while (iteroator.hasNext()) {
			exectime += iteroator.next();
			number++;
		}
		return exectime / number;
	}

	// sorted the priority value for all the tasks
	/**
	 * This method sorts the priority value of each task instance in a decreasing order.
	 * @param priorityvalue
	 * @return a sorted priority value
	 */
	private Map<Integer, Double> sortPriorityValues(Map<Integer, Double> priorityvalue) {
		Map<Integer, Double> sortedMap = sortByValue(priorityvalue);
		return sortedMap;
	}
	/**
	 * This method sorts a hashmap by the value in a decreasing order.
	 * @param unsortMap
	 * @return sorted map
	 */
	private static Map<Integer, Double> sortByValue(Map<Integer, Double> unsortMap) {
        // 1. Convert Map to List of Map
        List<Map.Entry<Integer, Double>> list =
                new LinkedList<Map.Entry<Integer, Double>>(unsortMap.entrySet());
        // 2. Sort list with Collections.sort(), provide a custom Comparat. Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
            public int compare(Map.Entry<Integer, Double> o1,
                               Map.Entry<Integer, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
        for (Map.Entry<Integer, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
	/**This method fills in the taskTime object based on the topological order of task instances.
	 * The MET and EST are computed starting from the forward order based on the topological order.
	 * The LFT is computed starting from the reversed order based on the topological order
	 * @param sortedpriorityvalue
	 */
	private void buildTaskTime(Map<Integer, Double> sortedpriorityvalue) {
		Set<Integer> orderedTasks = sortedpriorityvalue.keySet();
		Integer[] tasks = orderedTasks.toArray(new Integer[orderedTasks.size()]);
		taskTime.put(0, Arrays.asList(0.0,VMADelay,0.0));
		for (int i = 0; i < tasks.length; i++) {
			List<Double> tmp = Arrays.asList(0.0,0.0,0.0);
			tmp.set(0,computeMET(tasks[i]));
			tmp.set(1,computeEST(tasks[i]));
			taskTime.put(tasks[i], tmp);
		}
		for (int i = tasks.length - 1; i >= 0; i--) {
			List<Double> value = taskTime.get(tasks[i].intValue());
			value.set(2,computeLFT(tasks[i]));
		}
	}
	/**
	 * This method computes the minimum execution time of a task instance
	 * @param key a task instance
	 * @return
	 */
	private double computeMET(Integer key) {
		double min = Double.POSITIVE_INFINITY;
		for (double tmp : execTime.get(key).values()) {
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}
	/**
	 * This method computes the earliest start time of a task instance
	 * @param key a task instance
	 * @return
	 */
	private double computeEST(Integer key) {
		double max = Double.NEGATIVE_INFINITY;
		if (nodeParents.containsKey(key)) {
			for (Integer st : nodeParents.get(key)) {
				double tmp = taskTime.get(st).get(1) + taskTime.get(st).get(0) + edgeMap.get(st).get(key).doubleValue();
				if (max < tmp) {
					max = tmp;
				}
			}
		}
		return max;
	}
	/**
	 * This method computes the latest execution time of a task instance
	 * @param key a task instance
	 * @return
	 */
	private double computeLFT(Integer key) {
		double min = Double.POSITIVE_INFINITY;
		if (key.equals(t_exit)) {
			return D;
		} else {
			for (Integer str : edgeMap.get(key).keySet()) {
				double tmp = taskTime.get(str).get(2) - taskTime.get(str).get(0) - edgeMap.get(key).get(str);
				if (tmp < min) {
					min = tmp;
				}
			}
		}
		return min;
	}
	/**
	 * This method find all the partial critical paths based on the priority value.
	 * @param sortedpriorityvalue
	 * @return an array of list containing all the paths 
	 */
	public ArrayList<List<Integer>> findPCPs(Map<Integer, Double> sortedpriorityvalue) {
		Set<Integer> orderedTasks = sortedpriorityvalue.keySet();
		List<Integer> taskList = new ArrayList<Integer>();
		taskList.addAll(orderedTasks);
		taskList.remove(new Integer(-1));
		ArrayList<List<Integer>> paths = new ArrayList<List<Integer>>();
		while(!taskList.isEmpty()){	
			List<Integer> criticalpath = new ArrayList<Integer>();
			Integer cur = taskList.get(0);		
			while(taskList.contains(cur)){
				criticalpath.add(cur);
				taskList.remove(cur);
				Double pvalue = Double.NEGATIVE_INFINITY;
				Map<Integer, Double> edge = edgeMap.get(cur);
				Set<Integer> children = edge.keySet();
				Iterator<Integer> iterator = children.iterator();
				Integer index = 0;
				while(iterator.hasNext() ){
					Integer tmptask = iterator.next();
					if(taskList.contains(tmptask)){
						double taskvalue = sortedpriorityvalue.get(tmptask);
						if(taskvalue > pvalue){
							pvalue = taskvalue;
							index = tmptask;
						}
					}	
				}
				cur = index;
			}	
			paths.add(criticalpath);
		}
		return paths;
	}
	/**
	 * This method returns the maximum data transfer time among all the transfered edges.
	 * @param p
	 * @return
	 */
	private double getMaxedge(Integer p){
		Map<Integer, Double> edges = edgeMap.get(p);
		double max = Collections.max(edges.values());
		return max;
	}
	
	/**
	 * This method assigns a single path to the appropriate vm instances, referring to the Algorithm4 in the paper.
	 * @param path
	 */
	private void PathAssign(List<Integer> path){
		// TB
		List<Integer> ID = new ArrayList<Integer>();
		List<Integer> TASK = new ArrayList<Integer>();
		List<Double> TST = new ArrayList<Double>();
		List<Double> TFT  = new ArrayList<Double>();
		List<Double> ACCOST = new ArrayList<Double>();
		//List<Double> TLEFT = new ArrayList<Double>();
		List<Integer> IDREF = new ArrayList<Integer>();
		List<Double> MAXEDGE = new ArrayList<Double>();
		List<String> VMTYPE = new ArrayList<String>();
		List<Double> PROV = new ArrayList<Double>();
		List<Double> DEPROV = new ArrayList<Double>();
		
		// the first task in a path
		for(int k = 0; k <VMTypes.size(); k++){
			
			double tst;
			if(edgeMap.get(0).keySet().contains(path.get(0))){
				tst = VMADelay; 
			}else{
				tst = taskTime.get(path.get(0)).get(1); 
			}
			double tft = tst + execTime.get(path.get(0)).get(VMTypes.get(k));
			if(tft <= taskTime.get(path.get(0)).get(2)){
				ID.add(ID.size()+1); TASK.add(path.get(0));
				TST.add(tst);
				TFT.add(tft);
				double prov = tst - VMADelay; PROV.add(prov);
				double maxedge = getMaxedge(path.get(0)); MAXEDGE.add(maxedge);
				double deprov = prov+ Math.ceil((tft+maxedge-prov)/time_interval)*time_interval; DEPROV.add(deprov);
				
				double accost = Math.ceil((deprov-prov)/time_interval)*server_cost.get(VMTypes.get(k)); ACCOST.add(accost);
				//double tleft = Math.ceil((VMADelay+execTime.get(path.get(0)).get(VMTypes.get(k)))/time_interval)*time_interval - (VMADelay+execTime.get(path.get(0)).get(VMTypes.get(k)));   TLEFT.add(tleft);
				
				IDREF.add(-1);
				
				VMTYPE.add(VMTypes.get(k));
			}
			
			
		}
		// the remaining tasks in a path
		
		if(path.size() > 1){
			for(int i = 1; i < path.size(); i++){
				
				int firstindex = TASK.indexOf(path.get(i-1));
				int lastindex = TASK.lastIndexOf(path.get(i-1));
				double maxedge = getMaxedge(path.get(i));
				for(int k = 0; k < VMTypes.size(); k++){  
					double cost1 = Double.MAX_VALUE;
					double cost2 = Double.MAX_VALUE;
					double candidate1_tst = -1;
					double candidate1_tft = -1;
					double candidate1_prov = -1;
					double candidate1_deprov = -1;
					double candidate1_accost = -1;
					Integer candidate1_idref = null; 
					String candidate1_vm = "";
					double candidate2_tst = -1;
					double candidate2_tft = -1;
					double candidate2_prov = -1;
					double candidate2_deprov = -1;
					double candidate2_accost = -1;
					String candidate2_vm = "";
					Integer candidate2_idref = null; 
					for(int j = firstindex; j <= lastindex; j++){
						if(VMTYPE.get(j) != VMTypes.get(k)){
							double tst= Math.max(taskTime.get(path.get(i)).get(1),(TFT.get(j)+edgeMap.get(path.get(i-1)).get(path.get(i))));
							double tft = tst + execTime.get(path.get(i)).get(VMTypes.get(k)); 
							double prov = tst - VMADelay;
							double deprov = prov + Math.ceil((tft+maxedge-prov)/time_interval)*time_interval;
							double accost = ACCOST.get(j) + ((deprov-prov)/time_interval)*server_cost.get(VMTypes.get(k));
							if(tft <= taskTime.get(path.get(i)).get(2)){
								if (accost < cost1){
									cost1 = accost;
									candidate1_tst = tst;
									candidate1_tft = tft;
									candidate1_prov = prov;
									candidate1_deprov = deprov;
									candidate1_accost = accost; 
									candidate1_vm = VMTypes.get(k);
									candidate1_idref = j;
								}
							}
							
						}else{
							double tst= Math.max(taskTime.get(path.get(i)).get(1),TFT.get(j));
							double tft = tst + execTime.get(path.get(i)).get(VMTypes.get(k)); 
							double prov = PROV.get(j);
							double deprov;
							if(tft + maxedge <= DEPROV.get(j)) {
								 deprov = DEPROV.get(j);
							} else{
								 deprov = DEPROV.get(j) + Math.ceil((tft + maxedge - DEPROV.get(j))/time_interval)* time_interval;
							}
							double accost = ACCOST.get(j) + ( (deprov - DEPROV.get(j))/time_interval)*server_cost.get(VMTypes.get(k));
							if(tft <= taskTime.get(path.get(i)).get(2)){
								if (accost < cost2){
									cost2 = accost;
									candidate2_tst = tst;
									candidate2_tft = tft;
									candidate2_prov = prov;
									candidate2_deprov = deprov;
									candidate2_accost = accost; 
									candidate2_vm = VMTypes.get(k);
									candidate2_idref = j;
								}
							}
						}	
					}
					if (candidate1_tst != -1){
						ID.add(ID.size()+1); TASK.add(path.get(i)); 
						TST.add(candidate1_tst);
						TFT.add(candidate1_tft);
						PROV.add(candidate1_prov);
						DEPROV.add(candidate1_deprov);
						ACCOST.add(candidate1_accost);
						IDREF.add(candidate1_idref);
						VMTYPE.add(candidate1_vm);
						MAXEDGE.add(maxedge);
						
					}
					if (candidate2_tst != -1){
						ID.add(ID.size()+1); TASK.add(path.get(i)); 
						TST.add(candidate2_tst);
						TFT.add(candidate2_tft);
						PROV.add(candidate2_prov);
						DEPROV.add(candidate2_deprov);
						ACCOST.add(candidate2_accost);
						IDREF.add(candidate2_idref);
						VMTYPE.add(candidate2_vm);
						MAXEDGE.add(maxedge);
					}
					
				}
			}
		}
		
			Integer lastTask = path.get(path.size()-1);
			int lastTaskfirstIndex = TASK.indexOf(lastTask);
			int lastTaskLastIndex = TASK.lastIndexOf(lastTask);
			List<Double> sublist = ACCOST.subList(lastTaskfirstIndex, lastTaskLastIndex+1);
			int smallest = ACCOST.lastIndexOf(Collections.min(sublist));
			int preindex = IDREF.get(smallest); 
			TASK lastask = new TASK(); 
			lastask.t=lastTask; lastask.ast = TST.get(smallest); lastask.aft = TFT.get(smallest);
			VM lastvm = new VM(); 
			String prevm = VMTYPE.get(smallest);
			lastvm.vmType = prevm; lastvm.runTask.add(lastTask); lastvm.available = TFT.get(smallest); lastvm.vleft = DEPROV.get(smallest) - TFT.get(smallest);
			lastvm.pro = PROV.get(smallest); lastvm.depro = DEPROV.get(smallest); 
			sch.T.add(lastask); sch.VMpool.add(lastvm);
			for (int i = path.size() - 2; i >= 0; i--){
				TASK ctTask = new TASK();
				ctTask.t = path.get(i);
				ctTask.ast = TST.get(preindex);
				ctTask.aft = TFT.get(preindex);
				sch.T.add(ctTask);
				String ctvm = VMTYPE.get(preindex);
				if (prevm == ctvm) {
					int vmindex = getVMIndex(path.get(i + 1));
					VM ctVM = sch.VMpool.get(vmindex);
					ctVM.runTask.add(path.get(i));
				}else{
					VM vm = new VM(); 
					vm.runTask.add(path.get(i));
					vm.pro = PROV.get(preindex);
					vm.depro = DEPROV.get(preindex);
					vm.available = TFT.get(preindex);
					vm.vleft = DEPROV.get(preindex) - TFT.get(preindex);
					vm.vmType = VMTYPE.get(preindex);
					sch.VMpool.add(vm);
				}
				preindex = IDREF.get(preindex);
			}
		
		 
		
	}
	/**
	 * This method returns the VM instance index in the VM pool for a specific task instance.
	 * @param t
	 * @return
	 */
	private int getVMIndex(Integer t){
		int found = -1;
		for(int i =0; i < sch.VMpool.size(); i++){
			if(sch.VMpool.get(i).runTask.contains(t)){
				found = i;
			}
		}
		return found;
	}
	/** This method tries to assign a partial part of a path to free VM instance in the VM pool, then assign the remaining path to the appropriate VM instances, referring to the Algorithm3 in the paper.
	 * 
	 * @param paths
	 */
	private void  pathsAssignment(ArrayList<List<Integer>> paths) {
		for (int i = 0; i < paths.size(); i++){
			List<Integer> path = paths.get(i);
			int lj = 0;
			for(int j = 0; j < path.size(); j++){
				boolean found = false;
				for(int k = 0; k< sch.VMpool.size(); k++){
					double ast;
					if (sch.VMpool.get(k).available<=taskTime.get(path.get(j)).get(1)){
						ast = taskTime.get(path.get(j)).get(1);
					}else{
						ast = sch.VMpool.get(k).available;
					}
					
					if( sch.VMpool.get(k).depro >= (execTime.get(path.get(j)).get(sch.VMpool.get(k).vmType) + ast)
						&& taskTime.get(path.get(j)).get(2) >= (execTime.get(path.get(j)).get(sch.VMpool.get(k).vmType) + ast)){
						TASK t = new TASK();
						t.t = path.get(j);
						t.ast = ast;
						t.aft = t.ast + execTime.get(path.get(j)).get(sch.VMpool.get(k).vmType);
						sch.T.add(t);  
						VM vm = sch.VMpool.get(k);
						vm.vmType = sch.VMpool.get(k).vmType;
						vm.available = t.aft;
						vm.vleft = sch.VMpool.get(k).vleft - execTime.get(path.get(j)).get(sch.VMpool.get(k).vmType);
						vm.runTask.add(path.get(j));
						lj = j +1;
						found = true;
						break;
					}
				}
				if(found == false){
					break;
				}
			}
			List<Integer> subpath = path.subList(lj, path.size());
			if(subpath.size()!=0){
				PathAssign(subpath);
			}
			for(int j = 0; j < path.size(); j++){
				double ast = sch.T.get(index(path.get(j))).ast;
				double aft = sch.T.get(index(path.get(j))).aft;
				taskTime.get(path.get(j)).set(1,ast);
				taskTime.get(path.get(j)).set(2,aft );
				taskTime.get(path.get(j)).set(0,aft-ast);
				updateSuccessor(path.get(j));
				updatePredecessor(path.get(j));
			}
		}
		
	}
	/**
	 * Output the workflow schedule generated from the workflow planner LPOD. 
	 */
	private static void print(){
		for(int i= 0; i< sch.T.size(); i++){
			System.out.println("The task is "+ sch.T.get(i).t + " with start time "+ sch.T.get(i).ast + " and finished at " + sch.T.get(i).aft);
		}
		double totalCost = 0;
		for(int i = 0; i < sch.VMpool.size(); i++){
			System.out.println("Tasks: "+ sch.VMpool.get(i).runTask + "on "+sch.VMpool.get(i).vmType);
			System.out.println("VM provisioning time: " + sch.VMpool.get(i).pro);
			System.out.println("VM deprovisioning time: "+ sch.VMpool.get(i).depro);
			System.out.println("Avaliable time: "+ sch.VMpool.get(i).available);
			System.out.println("Current left time: "+ sch.VMpool.get(i).vleft);
			totalCost += (Math.ceil((sch.VMpool.get(i).depro-sch.VMpool.get(i).pro)/time_interval))*server_cost.get(sch.VMpool.get(i).vmType);
		}
		System.out.println("The final total cost is " + totalCost);
	}
	/**
	 * This method returns the task instance index in an assigned VM instance.
	 * @param t
	 * @return
	 */
	private int index(Integer t){
		for(int i = 0; i < sch.T.size(); i++){
			if (t.equals(sch.T.get(i).t))
				return i;
		}
		return 0;
	}
	
	/**
	 * This method updates the earliest start time after one path is assigned.
	 * @param key
	 */
	private void updateEST(Integer key) {
		double max = Double.NEGATIVE_INFINITY;
		if (nodeParents.containsKey(key)) {
			for (Integer st : nodeParents.get(key)) {
				double tmp = taskTime.get(st).get(1) + taskTime.get(st).get(0) + edgeMap.get(st).get(key).doubleValue();
				if (max < tmp) {
					max = tmp;
				}
			}
		}
		List<Double> value = taskTime.get(key);
		value.set(1, max);
		taskTime.put(key, value);
	}
	/**
	 * This method check if a task instance is already assigned.
	 * @param str
	 * @return
	 */
	private boolean alreadAssign(Integer str){
		for(int i =0; i < sch.T.size(); i++){
			if(sch.T.get(i).t.equals(str)){
				return true;
			}	
		}
		return false;
	}
	/**
	 * Update the EST of all the successors of assigned task instances.
	 * @param key
	 */
	private void updateSuccessor(Integer key) {
		List<Integer> q = new LinkedList<Integer>();
		q.add(key);
		while (!q.isEmpty()) {
			Integer task = q.get(0);
			q.remove(0);
			if(!alreadAssign(task)){
				updateEST(task);
			}
			if (edgeMap.containsKey(task)) {
 				for (Integer str : edgeMap.get(task).keySet()) {
					if (!q.contains(str)&& !alreadAssign(str))
						q.add(str);
				}
			}
		}
	}
	/**
	 * Update the LFT of all the predecessors of assigned task instances.
	 * @param key
	 */
	private void updatePredecessor(Integer key) {
		List<Integer> q = new LinkedList<Integer>();
		q.add(key);
		while (!q.isEmpty()) {
			Integer task = q.get(0);
			q.remove(0);
			if(!alreadAssign(task)){
				updateLFT(task);
			}
			//this.computeLFT(task);
			if (nodeParents.containsKey(task)) {
				for (Integer str : nodeParents.get(task)) {
					if (!q.contains(str) && !alreadAssign(str))
						q.add(str);
				}
			}
		}
	}
	/**
	 * Update the LFT of a task instance.
	 * @param key
	 */
	private void updateLFT(Integer key){
		List<Double> value = taskTime.get(key);
		value.set(2,computeLFT(key));
	}
	

	public GlobalSchedule plan() {
		edgeProcessing(w);
		getTaskExecutionTime(w);
		Map<Integer, Double> prorityvalue =	getProirityValue();
		Map<Integer, Double> sorted_map = sortPriorityValues(prorityvalue);
		buildTaskTime(sorted_map);	
		ArrayList<List<Integer>> pcps = findPCPs(sorted_map);
		System.out.println(pcps);
		pathsAssignment(pcps);
		print();
		GlobalSchedule gsch = new GlobalSchedule(w);
 		for(VM vm : sch.VMpool){
 			LocalSchedule lsch = new LocalSchedule();
 			lsch.setVmType(vm.vmType);
 			for(int i = 0; i<vm.runTask.size(); i++){
 				TaskSchedule tsch = w.getTaskSchedule(w.getTask(vm.runTask.get(i)-1));
 				lsch.addTaskSchedule(tsch);
 			}
 			gsch.addLocalSchedule(lsch);
 		}
		return gsch;
	}
	
	public void getTaskExecutionTime(Workflow w){
		for(int i = 1; i<= w.getNumOfTasks(); i++){
			Task t = w.getTask(i-1);
			Map<String, Double> exectime = execTimeTemp.get(t.taskName);
			execTime.put(i, exectime);
		}
		Map<String, Double> entryExecTime = new HashMap<String, Double>();
		for(String tmp: VMTypes){
			entryExecTime.put(tmp, 0.0);
		}
		execTime.put(0, entryExecTime);
		execTime.put(-1, entryExecTime);
	}
	/**
	 * This method fills in the edgeMap based on the workflow structure and edgeMapTemp objects together.
	 * The nodeParents is filled in with the workflow structure only.
	 * @param w
	 */
	private void edgeProcessing(Workflow w) {		
		for (WorkflowEdge e : w.getEdges()) {
			if (e.srcTask == null) {
				if (edgeMap.containsKey(0)) {
					Map<Integer, Double> tmp = edgeMap.get(0);
					tmp.put(w.getIndexOfTask(e.destTask) + 1, 0.0);
				}else{
					Map<Integer, Double> tmp = new HashMap<Integer, Double>();
					tmp.put(w.getIndexOfTask(e.destTask) + 1, 0.0);
					edgeMap.put(0, tmp);
				}
				if (nodeParents.containsKey(w.getIndexOfTask(e.destTask) + 1)) {
					nodeParents.get(w.getIndexOfTask(e.destTask) + 1).add(0);
				} else {
					List<Integer> tmp = new ArrayList<Integer>();
					tmp.add(0);
					nodeParents.put(w.getIndexOfTask(e.destTask) + 1, tmp);
				}
			} else if (e.destTask == null) {
				Map<Integer, Double> edge = new HashMap<Integer, Double>();
				edge.put(-1, 0.0);
				edgeMap.put(w.getIndexOfTask(e.srcTask) + 1, edge);
				
				if (nodeParents.containsKey(-1)) {
					nodeParents.get(-1).add(w.getIndexOfTask(e.srcTask) + 1);
				} else {
					List<Integer> tmp = new ArrayList<Integer>();
					tmp.add(w.getIndexOfTask(e.srcTask) + 1);
					nodeParents.put(-1, tmp);
				}
			} else {
				String source = e.srcTask.taskName;
				String to= e.destTask.taskName;
				double transfertime = edgeMapTemp.get(source).get(to);
				if(!edgeMap.containsKey(w.getIndexOfTask(e.srcTask) + 1)){
					Map<Integer, Double> tmp = new HashMap<Integer, Double>();
					tmp.put(w.getIndexOfTask(e.destTask) + 1, transfertime);
					edgeMap.put(w.getIndexOfTask(e.srcTask)+1, tmp);
				}else{
					Map<Integer, Double> tmp = edgeMap.get(w.getIndexOfTask(e.srcTask) + 1);
					tmp.put(w.getIndexOfTask(e.destTask) + 1, transfertime);
					
				}
				if (nodeParents.containsKey(w.getIndexOfTask(e.destTask) + 1)) {
					if (!nodeParents.get(w.getIndexOfTask(e.destTask) + 1).contains(w.getIndexOfTask(e.srcTask) + 1)) {
						nodeParents.get(w.getIndexOfTask(e.destTask) + 1).add(w.getIndexOfTask(e.srcTask) + 1);
					}
				} else {
					List<Integer> tmp = new ArrayList<Integer>();
					tmp.add(w.getIndexOfTask(e.srcTask) + 1);
					nodeParents.put(w.getIndexOfTask(e.destTask) + 1, tmp);
				}
			}
		}
		
	}
	

}
