package dataview.planners;

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
import java.util.TreeMap;


import dataview.models.GlobalSchedule;
import dataview.models.LocalSchedule;
import dataview.models.Task;
import dataview.models.TaskSchedule;
import dataview.models.Workflow;
import dataview.models.WorkflowEdge;

public class WorkflowPlanner_LPOD extends WorkflowPlanner {
	/*
	 * edgeMap: startNode ---> endNode and edge weight execTime: task :
	 * (resource running time) taskTime: task : double[MES, EST, LST ,LFT] AST
	 * maybe needed exectime: task execution time in different VM type
	 */
	private Map<Integer, Map<Integer, Double>> edgeMap; // store the edge
														// information
	private Map<Integer, Map<String, Double>> execTime; // store the task
														// execution time for
														// each VM
	private Map<Integer, List<Integer>> nodeParents = new HashMap<Integer, List<Integer>>();

	private Queue<Integer> vertexWithoutIncoming = new LinkedList<Integer>();
	private List<Integer> topoLogicalOrder = new ArrayList<Integer>();
	//private Map<Integer, double[]> taskTime = new HashMap<Integer, double[]>();
	private Map<Integer, List<Double>> taskTime = new HashMap<Integer, List<Double>>();
	private Map<String,LinkedHashMap<Integer, List<Double>>> VMPool = new HashMap<String, LinkedHashMap<Integer, List<Double>>>();
	
	
	private Map<String, LinkedHashMap<Integer, List<Integer>>> taskAssignedVM = new HashMap<String, LinkedHashMap<Integer, List<Integer>>>();
	private Map<Integer,Map<String, List<Double>>> actualAssignment = new HashMap<Integer, Map<String, List<Double>>>();
	private Map<Integer,Integer> combinedTasks = new HashMap<Integer, Integer>();
	
	private ArrayList<ArrayList<Integer>> tasksList = new ArrayList<ArrayList<Integer>>();
	private ArrayList<String> VMS = new ArrayList<String>();
	private ArrayList<Double[]> ProandDepro = new ArrayList<Double[]>();
	
	
	
	
	// record the tasks 
	
	// running
	// time
	// on
	// the
	// VM.

	// private Map<Integer, String> task_assignment = new HashMap<Integer,
	// String>(); // Task
	// assigned
	// to
	// the
	// VM

	// private Map<String, List<Double>> running_server = new HashMap<>();

	List<LocalSchedule> local_schedules = new ArrayList<LocalSchedule>();

	private static Integer t_entry = 0; // Assume t_entry has no incoming edge
	private static Integer t_exit = -1;
	private static double D;
	private static double time_interval;
	private static Map<String, Double> server_cost = new HashMap<String, Double>();
	private static double VMADelay;
	/*
	 * Deadline, time_interval and server_cost information provided by the user.
	 * 
	 */
	static {
		VMADelay = 1;
		D = 50;
		time_interval = 10;
		server_cost.put("VM1", 5.0);
		server_cost.put("VM2", 2.0);
		server_cost.put("VM3", 1.0);
	}
	
	public WorkflowPlanner_LPOD(Workflow w) {
		super(w);
		execTime = w.getExecutionTime();
		System.out.println(execTime);
		edgeMap = w.getTransferTime();
		System.out.println(edgeMap);
	}

	private void doTopologicalSorting() {
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
	
	
	
	
	// Calculate the upward rank value for each task
	public Map<Integer, Double> getProirityValue() {
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

	public double ave_exetime(Collection<Double> times) {
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
	public Map<Integer, Double> sortPriorityValues(Map<Integer, Double> priorityvalue) {
		
		Map<Integer, Double> sortedMap = sortByValue(priorityvalue);
		
		/*
		ValueComparator valuecompator = new ValueComparator(priorityvalue);
		TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(valuecompator);
		 sorted_map.putAll(priorityvalue);
		 */
		return sortedMap;
	}
	
	
	
	
	public static Map<Integer, Double> sortByValue(Map<Integer, Double> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<Integer, Double>> list =
                new LinkedList<Map.Entry<Integer, Double>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
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

        /*
        //classic iterator example
        for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }*/


        return sortedMap;
    }
	
	
	/*
	class ValueComparator implements Comparator<Integer> {
		Map<Integer, Double> base;

		public ValueComparator(Map<Integer, Double> base) {
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with
		// equals.
		public int compare(Integer a, Integer b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			} // returning 0 would merge keys
		}
	}
	*/
	public void buildTaskTime(Map<Integer, Double> sortedpriorityvalue) {
		Set<Integer> orderedTasks = sortedpriorityvalue.keySet();
		Integer[] tasks = orderedTasks.toArray(new Integer[orderedTasks.size()]);
		taskTime.put(0, Arrays.asList(0.0,VMADelay,0.0));
		for (int i = 0; i < tasks.length; i++) {
			/*
			double[] tmp = new double[4];
			tmp[0] = computeMET(tasks[i]);
			tmp[1] = computeEST(tasks[i]);
			tmp[2] = tmp[0] + tmp[1];
			*/
			List<Double> tmp = Arrays.asList(0.0,0.0,0.0);
			tmp.set(0,computeMET(tasks[i]));
			tmp.set(1,computeEST(tasks[i]));
			taskTime.put(tasks[i], tmp);

		}

		for (int i = tasks.length - 1; i >= 0; i--) {
			List<Double> value = taskTime.get(tasks[i].intValue());
			value.set(2,computeLFT(tasks[i]));
			//taskTime.put(tasks[i], value);
		}

	}

	public double computeMET(Integer key) {
		double min = Double.POSITIVE_INFINITY;
		for (double tmp : execTime.get(key).values()) {
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}

	public double computeEST(Integer key) {
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

	public ArrayList<List<Integer>> findPCPs(Map<Integer, Double> sortedpriorityvalue) {
		Set<Integer> orderedTasks = sortedpriorityvalue.keySet();
		List<Integer> taskList = new ArrayList<Integer>();
		taskList.addAll(orderedTasks);
		taskList.remove(new Integer(-1));
		ArrayList<List<Integer>> paths = new ArrayList<List<Integer>>();
		List<Integer> taskhaspath = new ArrayList<Integer>();
		taskhaspath.add(new Integer(-1));
		//for (Integer tmp : taskList) {
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
		//}
		return paths;

	}

	
	public List<Double>  pathsAssignment(ArrayList<List<Integer>> paths) {
		//Map<Double,Map<String, List<Double>>> actualAssignment = new HashMap<Double, Map<String, List<Double>>>();
		
		List<Double> cost = new ArrayList<Double>();
		for (int i = 0; i < paths.size(); i++) {
			List<String> vms = new ArrayList<String>();
			List<Integer> path = paths.get(i);
			List<Map<Double, Map<String, List<Double>>>> pathMappingStructure = pathAssignment(path);
			double mincost = Double.MAX_VALUE;
			double previousIndex = 0;
			double keyvalue = 0;
			String assignedvm = "";
			//  construct the transfer matrix
			Map<Double, Map<String, List<Double>>> lastlevel = pathMappingStructure.get(path.size()-1);
			
			for(Double tmp: lastlevel.keySet()){
				Map<String, List<Double>> assign = lastlevel.get(tmp);
				for(String vm:assign.keySet()){
					List<Double> cost_time = assign.get(vm);
					if(cost_time.get(0)<= mincost){
						previousIndex = cost_time.get(2);
						keyvalue = tmp;	
						assignedvm = vm;
						mincost = cost_time.get(0);
					}	
				}
			}
			cost.add(mincost);
			
			List<Double> astAndFint = lastlevel.get(keyvalue).get(assignedvm).subList(3, 5);
			Map<String, List<Double>> vm_astAndFin = new HashMap<String, List<Double>>();
			vm_astAndFin.put(assignedvm, astAndFint);
			actualAssignment.put(path.get(path.size()-1), vm_astAndFin );
			vms.add(assignedvm);			
			List<Double> tmp = taskTime.get(path.get(path.size()-1));
			tmp.set(0, execTime.get(path.get(path.size()-1)).get(assignedvm));
			tmp.set(2, astAndFint.get(1));
			if(path.size()>=2){
				for(int j = path.size()-2; j>=0; j--){
					mincost = Double.MAX_VALUE;
					String curassignedvm = "";
					keyvalue = previousIndex;
					
					Map<Double, Map<String, List<Double>>> level = pathMappingStructure.get(j);
					Map<String, List<Double>> assign = level.get(keyvalue);
					for(String vm:assign.keySet()){
						List<Double> cost_time = assign.get(vm);
						if(cost_time.get(0)<= mincost){
							previousIndex = cost_time.get(2);
							curassignedvm = vm;
							mincost = cost_time.get(0);
						}	
					}
					 astAndFint = level.get(keyvalue).get(curassignedvm).subList(3, 5);
					 vm_astAndFin = new HashMap<String, List<Double>>();
					vm_astAndFin.put(curassignedvm, astAndFint);
					vms.add(curassignedvm);
					actualAssignment.put(path.get(j), vm_astAndFin );
					tmp = taskTime.get(path.get(j));
					tmp.set(0, execTime.get(path.get(j)).get(curassignedvm));
					tmp.set(2, astAndFint.get(1));
				}
				
				
			}
			
			// check the taskAssignment to see whether the edge can be updated as 0
			Collections.reverse(vms);
			
			int index1 = 0;
			boolean append = false;
			for(int k = 0; k < path.size(); k++){
				String assignedVM = vms.get(k);
				String neighborassignedVM = "";
				List<Double> assignperiod = actualAssignment.get(path.get(k)).get(assignedVM);
				if(k+1 < path.size()){
					neighborassignedVM =  vms.get(k+1);
					if(assignedVM ==neighborassignedVM){
						Map<Integer, Double> value = edgeMap.get(path.get(k));
						value.put(path.get(k+1), 0.0);
						edgeMap.put(path.get(k), value);
						append = true;
						index1++;
						continue;		
					}
				}
				if(!append){
					VMS.add(assignedVM);
					ArrayList<Integer> subtasks = new ArrayList<Integer>();
					subtasks.add(path.get(k));
					tasksList.add(subtasks);
					Double[] vmproAnddepro = {0.0,0.0};
					vmproAnddepro[0] = assignperiod.get(0) - VMADelay;
					Map<Integer,Double> edgeTransfer = edgeMap.get(path.get(k));
					double maxedge =Collections.max(edgeTransfer.values());
					vmproAnddepro[1] = assignperiod.get(1) + maxedge;
					ProandDepro.add(vmproAnddepro);
					
				}else{
					VMS.add(assignedVM);
					ArrayList<Integer> subtasks = new ArrayList<Integer>();
					for(int j=index1; j>=0; j--){
						subtasks.add(path.get(k-j));
						
					}
					tasksList.add(subtasks);
					List<Double> assignperiod1 = actualAssignment.get(path.get(k-index1)).get(assignedVM);
					Double[] vmproAnddepro = {0.0,0.0};
					vmproAnddepro[0] = assignperiod1.get(0) - VMADelay;
					Map<Integer,Double> edgeTransfer = edgeMap.get(path.get(k));
					double maxedge =Collections.max(edgeTransfer.values());
					vmproAnddepro[1] = assignperiod.get(1) + maxedge;
					ProandDepro.add(vmproAnddepro);
					index1 = 0;
					append = false;
				}
						
				
			}
			
			
			
			
			
			//Collections.reverse(vms);
			if(path.size()>=2){
				for(int k = 0; k < path.size()-1; k++){
					if(vms.get(k)==vms.get(k+1)){
						Map<Integer, Double> value = edgeMap.get(path.get(k));
						value.put(path.get(k+1), 0.0);
						edgeMap.put(path.get(k), value);
						combinedTasks.put(path.get(k+1), path.get(k));	
					}
				}
			}
			
			
			for(Integer taskindex:path){
				Map<String, List<Double>> taskAssign = actualAssignment.get(taskindex);
				String assignedVM="";
				for(String vm:taskAssign.keySet()){
					assignedVM = vm;
				}
				List<Double> assignperiod = actualAssignment.get(taskindex).get(assignedVM);
				Double startime = assignperiod.get(0);
				int index=0;
				if(taskAssignedVM.containsKey(assignedVM)){
					LinkedHashMap<Integer, List<Integer>> tmp1 = taskAssignedVM.get(assignedVM);
					if(!combinedTasks.containsKey(taskindex)){
						
						for(Integer vmindex : tmp1.keySet()){
							List<Integer> tasks = tmp1.get(vmindex);
							Integer lastask = tasks.get(tasks.size()-1);
							
							if(startime >= actualAssignment.get(lastask).get(assignedVM).get(1)){
								tasks.add(taskindex);
								tmp1.put(vmindex, tasks);
								taskAssignedVM.put(assignedVM, tmp1);
								break;
							}
							else{
								index++;
								continue;
							}
						}
						if(index == taskAssignedVM.get(assignedVM).keySet().size()){
							List<Integer> tasks = new ArrayList<Integer>();
							tasks.add(taskindex);
							tmp1.put(index+1, tasks);					
						}
					}
					else{
						Integer directParent = combinedTasks.get(taskindex);
						for(Integer vmindex : tmp1.keySet()){
							List<Integer> tasks = tmp1.get(vmindex);
							if(!tasks.contains(directParent)){
								continue;
							}
							tasks.add(taskindex);
							tmp1.put(vmindex, tasks);
							taskAssignedVM.put(assignedVM, tmp1);
							break;
						}
					}
						
				}
				
				
				else{
					List<Integer> tasks = new ArrayList<Integer>();
					tasks.add(taskindex);
					LinkedHashMap<Integer, List<Integer>> tmp1 = new LinkedHashMap<Integer, List<Integer>>();
					tmp1.put(0, tasks);
					taskAssignedVM.put(assignedVM, tmp1);
					
				}
			}
		
			
			
			for(int k = 0; k < path.size(); k++){
				Integer task = path.get(k);
				updateSuccessor(task);
				updatePredecessor(task);
			}	
		}
		return cost;
	}
	
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
	
	
	

	private void updateSuccessor(Integer key) {
		List<Integer> q = new LinkedList<Integer>();
		q.add(key);

		while (!q.isEmpty()) {
			Integer task = q.get(0);
			q.remove(0);
			updateEST(task);
			if (edgeMap.containsKey(task)) {
				for (Integer str : edgeMap.get(task).keySet()) {
					if (!q.contains(str)&& !actualAssignment.containsKey(q))
						q.add(str);
				}
			}
		}
	}
	
	private void updatePredecessor(Integer key) {
		List<Integer> q = new LinkedList<Integer>();
		q.add(key);
		while (!q.isEmpty()) {
			Integer task = q.get(0);
			q.remove(0);
			if(!actualAssignment.containsKey(task)){
				updateLFT(task);
			}
			//this.computeLFT(task);
			if (nodeParents.containsKey(task)) {
				for (Integer str : nodeParents.get(task)) {
					if (!q.contains(str) && !actualAssignment.containsKey(q))
						q.add(str);
				}
			}
		}
	}
	 
	private void updateLFT(Integer key){
		List<Double> value = taskTime.get(key);
		value.set(2,computeLFT(key));
	}

	
	public List<Map<Double, Map<String, List<Double>>>>  pathAssignment(List<Integer> path){

		List<Map<Double, Map<String, List<Double>>>> cost_types = new ArrayList<>();
		for (int i = 0; i < path.size(); i++){
			TreeMap<Double, Map<String, List<Double>>> cost_type = new TreeMap<Double,Map<String, List<Double>>>();
			Map<Integer,Double> edgeTransfer = edgeMap.get(path.get(i));
			double maxedge =Collections.max(edgeTransfer.values());
			if(i == 0){
				double taskLFT = taskTime.get(path.get(i)).get(2);
				double taskEST = taskTime.get(path.get(i)).get(1);
				Map<String, Double> taskExecTime = execTime.get(path.get(i));
				for(String tmp : taskExecTime.keySet()){
					double keyvalue = taskEST + taskExecTime.get(tmp);
					Map<String, List<Double>> assign = new HashMap<String, List<Double>>();
					if( keyvalue <= taskLFT){
						/*
						 *  cost, current billing time left; reference, AST, AFT, maxtransfer,
						 */
						List<Double> cost_time = Arrays.asList(0.0,0.0,0.0,0.0,0.0,0.0);
						if(i==path.size()-1){
							double totalperiod = Math.ceil((VMADelay+taskExecTime.get(tmp)+maxedge)/time_interval);
							cost_time.set(0, totalperiod * server_cost.get(tmp));
							cost_time.set(1, totalperiod * time_interval - (VMADelay+taskExecTime.get(tmp)+maxedge));
							cost_time.set(2,(double) -1);
							cost_time.set(3, keyvalue-taskExecTime.get(tmp));
							cost_time.set(4, keyvalue);
							cost_time.set(5,maxedge);
						}else{
							
							cost_time.set(0, Math.ceil((taskExecTime.get(tmp)+VMADelay)/time_interval)*server_cost.get(tmp));
							cost_time.set(1, Math.ceil((taskExecTime.get(tmp)+VMADelay)/time_interval)*time_interval - taskExecTime.get(tmp));
							cost_time.set(2,(double) -1);
							cost_time.set(3, keyvalue-taskExecTime.get(tmp));
							cost_time.set(4, keyvalue);
							cost_time.set(5,maxedge);
							
						}
						assign.put(tmp, cost_time );
						cost_type.put(keyvalue, assign);
					} 
					
				}
				cost_types.add(cost_type);	
			}
			else{
				double taskLFT = taskTime.get(path.get(i)).get(2);
				double taskEST = taskTime.get(path.get(i)).get(1);
				Map<Double,Map<String,List<Double>>> previous = cost_types.get(i-1);
				Map<String, Double> taskExecTime = execTime.get(path.get(i));
				for(String tmp: taskExecTime.keySet()){
					for(Double pretime:previous.keySet()){
						Map<String,List<Double>> pre_cost_time = previous.get(pretime);
						for(String selctedvm: pre_cost_time.keySet()){
							double keyvalue;
							List<Double> cost_time = Arrays.asList(0.0,0.0,0.0,0.0,0.0,0.0);
							Map<String, List<Double>> assign = new HashMap<String, List<Double>>();
							
							if(tmp==selctedvm){
									if(pretime.doubleValue() <= taskEST){
										keyvalue = taskEST + taskExecTime.get(tmp);
										if(keyvalue <= taskLFT){
											double extratime = taskEST - pretime + taskExecTime.get(tmp) - pre_cost_time.get(tmp).get(1);
											// extratime bigger than 0 means more billing cycle needs to be added 
											// we assume that the data communication is smaller than the computation for each task
											if(extratime >=0) {
												cost_time.set(0, Math.ceil(extratime/time_interval)*server_cost.get(tmp)+ pre_cost_time.get(selctedvm).get(0));
												cost_time.set(1, Math.ceil(extratime/time_interval)*time_interval - extratime);
												
											}else{
												cost_time.set(0, pre_cost_time.get(selctedvm).get(0));
												cost_time.set(1, Math.abs(extratime));
											}
								
											cost_time.set(2, pretime.doubleValue());
											cost_time.set(3, keyvalue-taskExecTime.get(tmp));
											cost_time.set(4, keyvalue);
											cost_time.set(5, maxedge);
											if(cost_type.containsKey(keyvalue)){
												assign = cost_type.get(keyvalue);
												assign.put(tmp, cost_time );
											}else{
												assign.put(tmp, cost_time );
												cost_type.put(keyvalue, assign);
											}	
										}	
									}else{
										keyvalue = pretime.doubleValue() + taskExecTime.get(tmp);
										if(keyvalue <= taskLFT){
											
											double extratime = taskExecTime.get(tmp) - pre_cost_time.get(tmp).get(1);
											if(extratime > 0){
												cost_time.set(0, Math.ceil(extratime/time_interval)*server_cost.get(tmp) + pre_cost_time.get(selctedvm).get(0));
												cost_time.set(1, Math.ceil(extratime/time_interval)*time_interval-extratime);
											}else{
												cost_time.set(0, pre_cost_time.get(selctedvm).get(0));
												cost_time.set(1, Math.abs(extratime));
											}
											cost_time.set(2, pretime.doubleValue());
											cost_time.set(3, keyvalue-taskExecTime.get(tmp));
											cost_time.set(4, keyvalue);
											cost_time.set(5, maxedge);
											if(cost_type.containsKey(keyvalue)){
												assign = cost_type.get(keyvalue);
												assign.put(tmp, cost_time );
											}else{
												assign.put(tmp, cost_time );
												cost_type.put(keyvalue, assign);
											}	
											
										}
										
										
									}
									
						
							}
							
							// Assign the adjacent task into two different VMs
							
							else{
								if(pretime.doubleValue() <= taskEST){
									keyvalue = taskEST + taskExecTime.get(tmp) + edgeMap.get(path.get(i-1)).get(path.get(i));
								}
								else{
									keyvalue = pretime.doubleValue() + taskExecTime.get(tmp) + edgeMap.get(path.get(i-1)).get(path.get(i));
								}
								if(keyvalue < taskLFT){
									cost_time = Arrays.asList(0.0,0.0,0.0,0.0,0.0,0.0);
									/*
									double extratime;
									extratime = pre_cost_time.get(selctedvm).get(4)-pre_cost_time.get(selctedvm).get(0);
									extratime = extratime > 0? extratime:0;
									double cost = Math.ceil((taskExecTime.get(tmp) + VMADelay)/time_interval)*server_cost.get(tmp) + pre_cost_time.get(selctedvm).get(0) + Math.ceil(extratime/time_interval)*server_cost.get(tmp);
									if(i==path.size()-1){
										double transfer = maxedge - (Math.ceil(taskExecTime.get(tmp)/time_interval)*time_interval - taskExecTime.get(tmp) - VMADelay);
										transfer = transfer > 0? transfer:0;
										
										cost = cost + Math.ceil(transfer/time_interval)*server_cost.get(tmp);
									}
									*/
									double transfernewBilling;
									transfernewBilling = pre_cost_time.get(selctedvm).get(5)-pre_cost_time.get(selctedvm).get(1);
									transfernewBilling = transfernewBilling > 0? transfernewBilling:0;
									double cost = Math.ceil((taskExecTime.get(tmp) + VMADelay)/time_interval)*server_cost.get(tmp) + pre_cost_time.get(selctedvm).get(0) + Math.ceil(transfernewBilling/time_interval)*server_cost.get(tmp);
									
									cost_time.set(0, cost);
									cost_time.set(1,Math.ceil((taskExecTime.get(tmp)+ VMADelay)/time_interval)*time_interval - taskExecTime.get(tmp));
									cost_time.set(2, pretime.doubleValue());
									cost_time.set(3, keyvalue-taskExecTime.get(tmp));
									cost_time.set(4, keyvalue);
									cost_time.set(5, maxedge);
									
									if(i==path.size()-1){
										if(cost_time.get(5)-cost_time.get(1)>0){
											double totalcost = cost_time.get(0);
											totalcost += Math.ceil((cost_time.get(5)-cost_time.get(1))/time_interval)*server_cost.get(tmp);
											cost_time.set(0,  totalcost);
										}
											
									}
									if(cost_type.containsKey(keyvalue)){
										assign = cost_type.get(keyvalue);
										assign.put(tmp, cost_time );
									}else{
										assign.put(tmp, cost_time );
										cost_type.put(keyvalue, assign);
									}	
									
								}
							}	
							
						}
					}
				}
				cost_types.add(cost_type);	
				//cost_type.clear();
				
			}
			
		}
		
		return cost_types;
		}
		
		
		 //cost_types;
	
	
	

	public GlobalSchedule plan() {
		nodeParents = this.getNodeParents(w);
		Map<Integer, Double> prorityvalue =	getProirityValue();
		Map<Integer, Double> sorted_map = sortPriorityValues(prorityvalue);
		buildTaskTime(sorted_map);	
		ArrayList<List<Integer>> pcps = findPCPs(sorted_map);
		System.out.println(pcps);
		
		List<Double> cost = pathsAssignment(pcps);
		
		
		System.out.println(VMS);
		System.out.println(tasksList);
		System.out.println(actualAssignment);
		System.out.println("The cost for each path is"+ cost);
		
		GlobalSchedule gsch = new GlobalSchedule();
		for(String str: taskAssignedVM.keySet()){
			LinkedHashMap<Integer, List<Integer>> tmp = taskAssignedVM.get(str);
			for(Integer index : tmp.keySet()){
				List<Integer> ls = tmp.get(index);
				LocalSchedule lsch = new LocalSchedule();
				lsch.setVmType(str);
				for(int i = 0; i < ls.size(); i++){
					TaskSchedule tsch = w.getTaskSchedule(w.getTask(ls.get(i)-1));
					lsch.addTaskSchedule(tsch);
				}
				gsch.addLocalSchedule(lsch);
			}
		}
		return gsch;
	}
	public Map<Integer, List<Integer>> getNodeParents(Workflow w) {
		for (WorkflowEdge e : w.getEdges()) {
			if (e.srcTask == null) {
				if (nodeParents.containsKey(w.getIndexOfTask(e.destTask) + 1)) {
					nodeParents.get(w.getIndexOfTask(e.destTask) + 1).add(0);
				} else {
					List<Integer> tmp = new ArrayList<Integer>();
					tmp.add(0);
					nodeParents.put(w.getIndexOfTask(e.destTask) + 1, tmp);
				}
			} 
			else if (e.destTask == null) {
				if (nodeParents.containsKey(-1)) {
					nodeParents.get(-1).add(w.getIndexOfTask(e.srcTask) + 1);
				} else {
					List<Integer> tmp = new ArrayList<Integer>();
					tmp.add(w.getIndexOfTask(e.srcTask) + 1);
					nodeParents.put(-1, tmp);
				}
			} else {
				if (nodeParents.containsKey(w.getIndexOfTask(e.destTask) + 1)) {
					nodeParents.get(w.getIndexOfTask(e.destTask) + 1).add(w.getIndexOfTask(e.srcTask) + 1);
				} else {
					List<Integer> tmp = new ArrayList<Integer>();
					tmp.add(w.getIndexOfTask(e.srcTask) + 1);
					nodeParents.put(w.getIndexOfTask(e.destTask) + 1, tmp);
				}
			}
		}
		return nodeParents;
	}

}
