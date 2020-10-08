package dataview.planners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import dataview.planners.WorkflowPlanner_ICPCP.TASK;
import dataview.planners.WorkflowPlanner_ICPCP.VM;
import dataview.planners.WorkflowPlanner_ICPCP.sch;

public class WorkflowPlanner_E2C2D extends WorkflowPlanner {
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
	 *confidentialTasks: represents those tasks running on SGX machines.
	 */
	private Map<String, Map<String, Double>> execTimeTemp = new HashMap<String, Map<String, Double>>();
	private Map<String, Map<String, Double>> edgeMapTemp = new HashMap<String, Map<String, Double>>(); 
	private Map<Integer, Map<Integer, Double>> edgeMap; 
	private Map<Integer, Map<String, Double>> execTime; 
	private Map<Integer, List<Integer>> nodeParents = new HashMap<Integer, List<Integer>>();
	private Queue<Integer> vertexWithoutIncoming = new LinkedList<Integer>();
	private ArrayList<Integer> topoLogicalOrder = new ArrayList<Integer>();
	private Map<Integer, List<Double>> taskTime = new HashMap<Integer, List<Double>>();
	private ArrayList<String> VMTypes;
	ArrayList<Integer> confidentialTasks = new ArrayList<Integer>();
	List<LocalSchedule> local_schedules = new ArrayList<LocalSchedule>();
	private static Integer t_entry = 0; // Assume t_entry has no incoming edge
	private static Integer t_exit = -1;
	private static double D;
	private static double time_interval;
	private static Map<String, Double> server_cost = new HashMap<String, Double>();
	private static double VMADelay;
	class VM {
		String vmType;
		double available;
		double vleft;
		double pro;
		double depro;
		ArrayList<Integer> runTask = new ArrayList<Integer>();
	}
	class TASK {
		Integer t;
		double ast;
		double aft;
	}
	static class sch {
		static ArrayList<TASK> T = new ArrayList<TASK>();
		static ArrayList<VM> VMpool = new ArrayList<VM>();
		static ArrayList<VM> SecuVMpool = new ArrayList<VM>();
	}

	public WorkflowPlanner_E2C2D(Workflow w, String location){
		super(w);
		execTime = new HashMap<Integer, Map<String, Double>>();
		edgeMap = new HashMap<Integer, Map<Integer, Double>>(); 
		Map<String, Integer> taskIndexMapping = new HashMap<String, Integer>();
		String workflowname = w.workflowName;
		for(int i = 1; i<= w.getNumOfTasks(); i++){
			Task t = w.getTask(i-1);
			taskIndexMapping.put(t.taskName, i);
		}
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
				for(int i = 0; i<jarraytasks.size(); i++){
					JSONObject obj = jarraytasks.get(i).toJSONObject();
					String vm = (String) obj.keySet().toArray()[0];
					Double exeTime = Double.parseDouble(obj.get(vm).toString().replace("\"", ""));
					Map<String, Double> tmp;
					if(execTime.containsKey(taskIndexMapping.get(str))){
						tmp = execTime.get(taskIndexMapping.get(str));
						tmp.put(vm, exeTime);
					}else{
						tmp = new HashMap<String, Double>();
						tmp.put(vm, exeTime);
						execTime.put(taskIndexMapping.get(str), tmp);
					}
					System.out.println("the task: " +str +  " is running on vm "+ vm + " :" + exeTime);
				}
			}
			Integer oneTask = new ArrayList<Integer>(execTime.keySet()).get(0);
			List<String> vm = new ArrayList<String>(execTime.get(oneTask).keySet());
			Map<String, Double> entryExecTime = new HashMap<String, Double>();
			for(String tmp:vm){
				entryExecTime.put(tmp, 0.0);
			}
			execTime.put(0, entryExecTime);
			execTime.put(-1, entryExecTime);
			JSONObject jsonedge = workflowobj.get("Tasktransfer").toJSONObject();
			System.out.println();
			Set<Integer> parenTasks = new HashSet<Integer>();
			Set<Integer> childTasks = new HashSet<Integer>();
			for(String str: jsonedge.keySet()){
				parenTasks.add(taskIndexMapping.get(str));
				JSONArray jarrayedge  = jsonedge.get(str).toJSONArray();
				for(int i = 0; i<jarrayedge.size(); i++){
					JSONObject obj = jarrayedge.get(i).toJSONObject();
					Integer childTask = taskIndexMapping.get(obj.get("To").toString().replace("\"", ""));
					childTasks.add(childTask);
					Double transferTime = Double.parseDouble(obj.get("Trans").toString().replace("\"", ""));
					Map<Integer, Double> tmp;
					if(edgeMap.containsKey(taskIndexMapping.get(str))){
						tmp = edgeMap.get(taskIndexMapping.get(str));
						tmp.put(childTask, transferTime);
					}else{
						tmp = new HashMap<Integer, Double>();
						tmp.put(childTask, transferTime);
						edgeMap.put(taskIndexMapping.get(str), tmp);
					}
				}
			}
			Set<Integer> existingTasks = new HashSet<Integer>(taskIndexMapping.values());
			Set<Integer> soucingTasks = new HashSet<Integer>();
			soucingTasks = (Set<Integer>) ((HashSet) existingTasks).clone();
			existingTasks.removeAll(parenTasks);
			soucingTasks.removeAll(childTasks);
			Map<Integer, Double> tmp;
			for(Integer t: soucingTasks){
				if(edgeMap.containsKey(0)){
					tmp = edgeMap.get(0);
					tmp.put(t,  0.0);
				}else{
					tmp = new HashMap<Integer, Double>();
					tmp.put(t, 0.0);
					edgeMap.put(0, tmp);
				}
			}
			for(Integer t:existingTasks){
				tmp = new HashMap<Integer, Double>();
				tmp.put(-1, 0.0);
				edgeMap.put(t, tmp);
			}
			
		}
		VMTypes = new ArrayList<String>(execTime.get(1).keySet());
		VMADelay = 0.01;
		D =122;
		time_interval = 90;
		server_cost.put("t2.xlarge", 5.0);
		server_cost.put("t2.medium", 2.0);
		server_cost.put("t2.micro", 1.0);
		
	}
	public WorkflowPlanner_E2C2D(Workflow w) {
		super(w);
		execTimeTemp = w.getExecutionTime();
		System.out.println(execTime);
		edgeMapTemp = w.getTransferTime();
		System.out.println(edgeMap);
		VMTypes = new ArrayList<String>(execTime.get(1).keySet());
		VMADelay = 1;
		D = 50;
		time_interval = 10;
		server_cost.put("VM1", 5.0);
		server_cost.put("VM2", 2.0);
		server_cost.put("VM3", 1.0);
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
	private double computeMET(Integer key) {
		double min = Double.POSITIVE_INFINITY;
		for (double tmp : execTime.get(key).values()) {
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}
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
	private double computeLFT(Integer key) {
		double min = Double.POSITIVE_INFINITY;
		if (key.equals(t_exit)) {
			return D;
		} else {
			for (Integer str : edgeMap.get(key).keySet()) {
				double tmp = taskTime.get(str).get(3) - taskTime.get(str).get(0) - edgeMap.get(key).get(str);
				if (tmp < min) {
					min = tmp;
				}
			}
		}
		taskTime.get(key).set(3, min);
		return min;
	}
	private void buildTaskTime() {
		this.doTopologicalSorting();
		for (Integer key : topoLogicalOrder) {
			if (key.equals(t_entry)) {
				taskTime.put(0, Arrays.asList(0.0, VMADelay, VMADelay, 0.0));
			} else {
				List<Double> tmp = Arrays.asList(0.0, 0.0, 0.0, 0.0);
				double met = computeMET(key);
				double est = computeEST(key);
				tmp.set(0, met);
				tmp.set(1, est);
				double mft = met + est;
				tmp.set(2, mft);
				taskTime.put(key, tmp);
			}

		}
		for (int i = topoLogicalOrder.size() - 1; i >= 0; i--) {
			Integer key = topoLogicalOrder.get(i);
			List<Double> tmp = taskTime.get(key);
			tmp.set(3, computeLFT(key));
		}

	}
	private boolean alreadAssign(Integer str) {
		for (int i = 0; i < sch.T.size(); i++) {
			if (sch.T.get(i).t.equals(str)) {
				return true;
			}
		}
		return false;
	}
	private Integer criticalChild(Integer task) {
		Integer criticalChild = null;
		if (task == 0) {
			double maxLFT = -Double.MAX_VALUE;
			List<Integer> children = new ArrayList<Integer>(edgeMap.get(task).keySet());
			for (Integer child : children) {
				if (nodeParents.get(child).size() == 1) {
					if ((taskTime.get(child).get(2) > maxLFT) && !alreadAssign(child)) {
						maxLFT = taskTime.get(child).get(2);
						criticalChild = child;
					}
				}

			}

		} else {

			double maxLFT = -Double.MAX_VALUE;
			List<Integer> children = new ArrayList<Integer>(edgeMap.get(task).keySet());
			for (Integer child : children) {
				if ((taskTime.get(child).get(2) > maxLFT) && !alreadAssign(child)) {
					maxLFT = taskTime.get(child).get(2);
					criticalChild = child;
				}

			}

		}
		return criticalChild;
	}
	private boolean hasChild(Integer task) {
		if (edgeMap.containsKey(task)) {
			return true;
		} else {
			return false;
		}
	}
	private ArrayList<ArrayList<Integer>> splitCriticalPathByConfidentialTask(ArrayList<Integer> criticalPath) {
		ArrayList<ArrayList<Integer>> paths = new ArrayList<ArrayList<Integer>>();
		if (confidentialTasks.isEmpty()) {
			paths.add(criticalPath);
			return paths;
		}
		ArrayList<Integer> path = new ArrayList<Integer>();
		for (int i = 0; i < criticalPath.size(); i++) {
			if (confidentialTasks.contains(criticalPath.get(i))) {
				if (!path.isEmpty()) {
					paths.add(path);
				}
				path = new ArrayList<Integer>();
				/* Checking consecutive confidentialTasks */
				for (int j = i; j < criticalPath.size(); j++) {
					if (confidentialTasks.contains(criticalPath.get(i))) {
						path.add(criticalPath.get(i));
					} else {
						paths.add(path);
						i = j;
						break;
					}
				}
				/* clearing the path list for next use */
				path = new ArrayList<Integer>();
			} else {
				path.add(criticalPath.get(i));
			}
		}
		return paths;
	}

	private ArrayList<Integer> orderByEFT() {
		ArrayList<Integer> topoLogicalOrderByEFT = (ArrayList) topoLogicalOrder.clone();
		for (int i = 0; i < topoLogicalOrderByEFT.size(); i++) {
			topoLogicalOrderByEFT.set(i, null);
		}
		for (int i = 0; i < topoLogicalOrder.size(); i++) {
			Integer t = topoLogicalOrder.get(i);
			if (hasChild(t)) {
				ArrayList<Integer> children = new ArrayList<Integer>(edgeMap.get(t).keySet());
				LinkedHashMap<Integer, Double> map = new LinkedHashMap<Integer, Double>();
				ArrayList<Integer> indexes = new ArrayList<Integer>();
				for (Integer child : children) {
					if(nodeParents.get(child).size()==1){
						Integer index = topoLogicalOrder.indexOf(child);
						indexes.add(index);
						map.put(index, taskTime.get(child).get(2));
					}
					
				}
				LinkedHashMap<Integer, Double> orderMap = sortByValue(map);
				ArrayList<Integer> newIndex = new ArrayList<Integer>(orderMap.keySet());
				for (int j = 0; j < newIndex.size(); j++) {
					topoLogicalOrderByEFT.set(indexes.get(j), topoLogicalOrder.get(newIndex.get(j)));
				}
			}
			if (!topoLogicalOrderByEFT.contains(t)) {
				topoLogicalOrderByEFT.set(i, t);
			}

		}
		return topoLogicalOrderByEFT;
	}

	public static LinkedHashMap<Integer, Double> sortByValue(LinkedHashMap<Integer, Double> hm) {
		// Create a list from elements of HashMap
		List<Map.Entry<Integer, Double>> list = new LinkedList<Map.Entry<Integer, Double>>(hm.entrySet());
		// Sort the list
		Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
			public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
		// put data from sorted list to hashmap
		LinkedHashMap<Integer, Double> temp = new LinkedHashMap<Integer, Double>();
		for (Map.Entry<Integer, Double> aa : list) {
			temp.put(aa.getKey(), aa.getValue());
		}
		return temp;
	}
	private void assignChildren() {
		ArrayList<Integer> topoLogicalOrderByEFT = orderByEFT();
		while (topoLogicalOrderByEFT.size() != 0) {
			Integer task = topoLogicalOrderByEFT.get(0);
			ArrayList<Integer> criticalPath = new ArrayList<Integer>();
			if (!alreadAssign(task)) {
				criticalPath.add(task);
			}
			while (hasChild(task)) {
				task = criticalChild(task);
				if (task != null) {
					criticalPath.add(task);
				}
			}
			ArrayList<ArrayList<Integer>> paths = splitCriticalPathByConfidentialTask(criticalPath);
			for (ArrayList<Integer> path : paths) {
				Collections.reverse(path);
				System.out.println(path);
				assignPath(path);
				for (Integer t : path) {
					updateSuccessor(t);
					updatePredecessor(t);
				}

			}
			criticalPath.add(t_entry);
			criticalPath.add(t_exit);
			topoLogicalOrderByEFT.removeAll(criticalPath);
		}

	}
	private void updateSuccessor(Integer key) {
		List<Integer> q = new LinkedList<Integer>();
		q.add(key);
		while (!q.isEmpty()) {
			Integer task = q.get(0);
			q.remove(0);
			updateEST(task);
			updateEFT(task);
			if (edgeMap.containsKey(task)) {
				for (Integer str : edgeMap.get(task).keySet()) {
					if (!q.contains(str))
						q.add(str);
				}
			}
		}
	}
	private void updateLFT(Integer key) {
		List<Double> value = taskTime.get(key);
		value.set(3, computeLFT(key));
	}
	private void updatePredecessor(Integer key) {
		List<Integer> q = new LinkedList<Integer>();
		q.add(key);
		while (!q.isEmpty()) {
			Integer task = q.get(0);
			q.remove(0);
			// this.computeLFT(task);
			if (!alreadAssign(task)) {
				updateLFT(task);
			}
			if (nodeParents.containsKey(task)) {
				for (Integer str : nodeParents.get(task)) {
					if (!q.contains(str))
						q.add(str);
				}
			}
		}
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
		taskTime.get(key).set(1, max);
	}

	private void updateEFT(Integer key) {
		List<Double> tmp = taskTime.get(key);
		double eft = tmp.get(1) + tmp.get(0);
		tmp.set(2, eft);
	}

	public TASK getTASK(Integer t) {
		TASK currentTask = null;
		for (TASK task : sch.T) {
			if (task.t == t)
				currentTask = task;
		}
		return currentTask;
	}

	public Integer hasChildOnAssignedPath(Integer task, ArrayList<Integer> path) {
		Integer child = null;
		for (Integer t : path) {
			if (edgeMap.get(task).containsKey(t)) {
				child = t;
				break;
			}
		}
		return child;
	}

	public boolean hasParentOnAssignedPath(Integer task, ArrayList<Integer> path) {
		boolean has = false;
		for (Integer t : path) {
			if (edgeMap.get(t).containsKey(task)) {
				has = true;
				break;
			}
		}
		return has;
	}

	// the path order is reversed-- t9--t6--t2
	public void assignPath(ArrayList<Integer> path) {
		if (confidentialTasks.containsAll(path)) {
			assignToNewInstance(path, "SGX");
			return;
		}
		boolean assigned = false;
		Integer lastTask = path.get(0);
		Integer firstTask = path.get(path.size() - 1);
		if (sch.VMpool.isEmpty()) {
			assignToNewInstance(path);
			assigned = true;
		} else {
			for (VM tmp : sch.VMpool) {
				double[] startAndEnd = getExecutionTime(path, tmp.vmType);
				double startTime = startAndEnd[0];
				double endTime = startAndEnd[1];
				double duration = endTime - startTime;
				Integer child = hasChildOnAssignedPath(lastTask, tmp.runTask);
				// the last task's child of current path cannot be the first
				// task of the previous vms
				if (child != null) {
					List<Integer> exsitingPath = tmp.runTask;
					int index = exsitingPath.indexOf(child);
					Integer preChild = exsitingPath.get(index - 1);
					TASK childTask = getTASK(child);
					TASK preChildTask = getTASK(preChild);
					if (duration <= (childTask.ast - preChildTask.aft)) {
						// Insert the path between the two tasks
						for (int i = path.size() - 1; i >= 0; i--) {
							TASK task = new TASK();
							Integer t = path.get(i);
							task.t = t;
							task.ast = Math.max(startTime, taskTime.get(t).get(1));
							task.aft = task.ast + execTime.get(t).get(tmp.vmType);
							startTime = task.aft;
							sch.T.add(task);
							tmp.runTask.add(t);
						}
						assigned = true;
						break;
					}
				}
				boolean foudParent = hasParentOnAssignedPath(firstTask, tmp.runTask);
				Integer foudAnotherChild = hasChildOnAssignedPath(firstTask, tmp.runTask);
				// assign the whole path to the end
				if (foudParent && foudAnotherChild != null) {
					if (duration <= tmp.vleft) {
						startTime = tmp.available;
						for (int i = path.size() - 1; i >= 0; i--) {
							TASK task = new TASK();
							Integer t = path.get(i);
							task.t = t;
							task.ast = Math.max(startTime, taskTime.get(t).get(1));
							task.aft = task.ast + execTime.get(t).get(tmp.vmType);
							startTime = task.aft;
							sch.T.add(task);
							tmp.runTask.add(t);
						}

					}
					assigned = true;
					break;
				}

			}
		}
		if (!assigned)
			assignToNewInstance(path);
		for (int i = path.size() - 1; i >= 0; i--) {
			List<Double> tmp = taskTime.get(path.get(i));
			double met = 0;
			double act = 0;
			for (TASK task : sch.T) {
				if (task.t.equals(path.get(i))) {
					met = task.aft - task.ast;
					act = task.aft;
				}
			}
			tmp.set(0, met);
			tmp.set(3, act);
		}

		// update edge weigth
		for (int i = path.size() - 1; i > 0; i--) {
			for (Integer index : edgeMap.get(path.get(i)).keySet()) {
				if (path.contains(index)) {
					edgeMap.get(path.get(i)).put(index, 0.0);
				}
			}
		}

	}
	private void assignToNewInstance(List<Integer> path) {
		double cost = java.lang.Double.MAX_VALUE;
		String selectServer = "";
		for (String server : VMTypes) {
			if (checkEachTaskFinishedWithLFT(path, server)) {
				double instanceCost = calculateCost(path, server);
				if (instanceCost <= cost) {
					cost = instanceCost;
					selectServer = server;
				}
			}
		}
		VM vm = new VM();
		double firstAst = -Double.MAX_VALUE;
		for (int i = path.size() - 1; i >= 0; i--) {

			TASK task = new TASK();
			task.t = path.get(i);
			if (taskTime.get(path.get(i)).get(1) > firstAst) {
				firstAst = taskTime.get(path.get(i)).get(1);
			}
			task.ast = firstAst;
			task.aft = firstAst + execTime.get(path.get(i)).get(selectServer);
			sch.T.add(task);
			vm.runTask.add(path.get(i));
			firstAst = task.aft;
		}

		double[] startAndEnd = getExecutionTime(path, selectServer);
		vm.pro = startAndEnd[0] - VMADelay;
		vm.available = startAndEnd[1];
		vm.depro = Math.ceil(startAndEnd[1] / time_interval) * time_interval;
		vm.vleft = vm.depro - vm.available;
		vm.vmType = selectServer;
		sch.VMpool.add(vm);
	}

	public void assignToNewInstance(ArrayList<Integer> path, String vmtype) {
		Collections.reverse(path);
		double cost = java.lang.Double.MAX_VALUE;
		String selectServer = "";
		if (checkEachTaskFinishedWithLFT(path, vmtype)) {
			double instanceCost = calculateCost(path, vmtype);
			if (instanceCost <= cost) {
				cost = instanceCost;
				selectServer = vmtype;
			}
		}
		VM vm = new VM();
		for (int i = path.size() - 1; i >= 0; i--) {
			TASK task = new TASK();
			task.t = path.get(i);
			task.ast = taskTime.get(path.get(i)).get(1);
			task.aft = task.ast + execTime.get(path.get(i)).get(selectServer);
			sch.T.add(task);
			vm.runTask.add(path.get(i));
		}

		double[] startAndEnd = getExecutionTime(path, selectServer);
		vm.pro = startAndEnd[0] - VMADelay;
		vm.available = startAndEnd[1];
		vm.depro = Math.ceil(startAndEnd[1] / time_interval) * time_interval;
		vm.vleft = vm.depro - vm.available;
		vm.vmType = selectServer;
		sch.SecuVMpool.add(vm);
	}

	private boolean checkEachTaskFinishedWithLFT(List<Integer> path, String server) {
		Integer firstNode = path.get(path.size() - 1);
		double firstEST = taskTime.get(firstNode).get(1);
		for (int i = path.size() - 1; i >= 0; i--) {
			if (firstEST + execTime.get(path.get(i)).get(server) > taskTime.get(path.get(i)).get(3)) {
				return false;
			} else {
				firstEST = firstEST + execTime.get(path.get(i)).get(server);
			}
		}
		return true;
	}
	private double calculateCost(List<Integer> path, String server) {
		double[] timePeroid = getExecutionTime(path, server);
		double exec = timePeroid[1] - timePeroid[0];
		double cost = Math.ceil(exec / time_interval) * server_cost.get(server);
		return cost;
	}
	/**
	 * get the EST of the last task to make sure the AST should be bigger than the EST
	 * @param path
	 * @param server
	 * @return
	 */
	private double[] getExecutionTime(List<Integer> path, String server) {
		double[] result = { 0.0, 0.0 };
		double firstStart = taskTime.get(path.get(path.size() - 1)).get(1);
		double exec = -Double.MAX_VALUE;
		for (int i = path.size() - 1; i >= 0; i--) {
			if (taskTime.get(path.get(i)).get(1) >= exec) {
				exec = taskTime.get(path.get(i)).get(1);
			}
			exec += execTime.get(path.get(i)).get(server);
		}

		result[0] = firstStart;
		result[1] = exec;
		return result;
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
	private void init() {
		edgeProcessing(w);
		getTaskExecutionTime(w);
		TASK entry = new TASK();
		entry.t = t_entry;
		sch.T.add(entry);
		TASK exit = new TASK();
		exit.t = t_exit;
		sch.T.add(exit);
	}
	private static void print() {
		for (int i = 0; i < sch.T.size(); i++) {
			System.out.println("The task is " + sch.T.get(i).t + " with start time " + sch.T.get(i).ast
					+ " and finished at " + sch.T.get(i).aft);
		}
		double totalCost = 0;
		for (int i = 0; i < sch.VMpool.size(); i++) {
			System.out.println("Tasks: " + sch.VMpool.get(i).runTask + "on " + sch.VMpool.get(i).vmType);
			System.out.println("VM provisioning time: " + sch.VMpool.get(i).pro);
			System.out.println("VM deprovisioning time: " + sch.VMpool.get(i).depro);
			System.out.println("Avaliable time: " + sch.VMpool.get(i).available);
			System.out.println("Current left time: " + sch.VMpool.get(i).vleft);
			totalCost += (Math.ceil((sch.VMpool.get(i).depro - sch.VMpool.get(i).pro) / time_interval))
					* server_cost.get(sch.VMpool.get(i).vmType);
		}
		System.out.println("The final total cost is " + totalCost);
	}

	public GlobalSchedule plan() {
		init();
		buildTaskTime();
		assignChildren();
		System.out.println(sch.VMpool.size());
		print();

		GlobalSchedule gsch = new GlobalSchedule(w);
		for (VM vm : sch.VMpool) {
			LocalSchedule lsch = new LocalSchedule();
			lsch.setVmType(vm.vmType);
			for (int i = 0; i < vm.runTask.size(); i++) {
				TaskSchedule tsch = w.getTaskSchedule(w.getTask(vm.runTask.get(i) - 1));
				lsch.addTaskSchedule(tsch);
			}
			gsch.addLocalSchedule(lsch);
		}
		return gsch;
	}

	public void edgeProcessing(Workflow w) {		
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
