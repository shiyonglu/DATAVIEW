package dataview.planners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

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
import dataview.planners.WorkflowPlanner_LPOD.TASK;
import dataview.planners.WorkflowPlanner_LPOD.VM;
import dataview.planners.WorkflowPlanner_LPOD.sch;

/**
 * This IC-PCP algorithm is implemented based on this paper "Deadline-constrained workflow scheduling algorithms for Infrastructure as a Service Clouds"
 * The algorithm needs to take the task execution time in different VM types and the data transfer time as the input.
 *
 * @author changxinbai
 *
 */
public class WorkflowPlanner_ICPCP extends WorkflowPlanner {
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
	private Map<String, Map<String, Double>> execTimeTemp = new HashMap<String, Map<String, Double>>();
	private Map<String, Map<String, Double>> edgeMapTemp = new HashMap<String, Map<String, Double>>(); 
	private Map<Integer, Map<Integer, Double>> edgeMap = new HashMap<Integer,  Map<Integer, Double>>(); 
	private Map<Integer, Map<String, Double>> execTime = new HashMap<Integer, Map<String, Double>>(); 
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
	 * A constructor for the IC-PCP planner.
	 * @param w a workflow object
	 */
	public WorkflowPlanner_ICPCP(Workflow w) {
		super(w);
		execTimeTemp = w.getExecutionTime();
		System.out.println(execTime);
		edgeMapTemp = w.getTransferTime();
		System.out.println(edgeMap);
		VMTypes = new ArrayList<String>(execTime.get(1).keySet());
		VMADelay = 1;
		D = 50;
		time_interval = 10;
		server_cost.put("t2.xlarge", 5.0);
		server_cost.put("t2.medium", 2.0);
		server_cost.put("t2.micro", 1.0);
	}
	/**
	 * A constructor for the IC-PCP planner fills in the execTimeTemp and edgeMapTemp; assigns the deadline, billing cycle, server_cost and VM delay information.
	 * @param w a workflow object
	 * @param location the workflow configuration file location.
	 * 
	 */
	public WorkflowPlanner_ICPCP(Workflow w, String location){
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
	 * This method fills the edgeMap,nodeParents and taskTime object by calling the edgeProceesing and getTaskExecutionTime methods.
	 * Then assigns the entry and exit task to workflow schedule sch.
	 */
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
	
	
	public GlobalSchedule plan() {
		this.init();
		this.buildTaskTime();
		this.assignParents(t_exit);
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
	/**This method fills in the taskTime object based on the topological order of task instances.
	 * The MET and EST are computed starting from the forward order based on the topological order.
	 * The LFT is computed starting from the reversed order based on the topological order
	 */
	private void buildTaskTime() {
		doTopologicalSorting();
		for (Integer key : topoLogicalOrder) {
			if (key.equals(t_entry)) {
				taskTime.put(0, Arrays.asList(0.0,VMADelay,VMADelay,0.0));
			} else {
				List<Double> tmp = Arrays.asList(0.0,0.0,0.0,0.0);
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
	/**
	 * This method computes the minimum execution time of a task instance
	 * @param key
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
	 * This method checks if the task instance is assigned.
	 * @param str
	 * @return
	 */
	private boolean alreadAssign(Integer str) {
		for (int i = 0; i < sch.T.size(); i++) {
			if (sch.T.get(i).t.equals(str)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * This illustrates the assignParents method, referring to the paper.
	 * @param t
	 */
	private void assignParents(Integer t) {
		while (true) {
			boolean allAssigned = true;
			for (Integer p : nodeParents.get(t)) {
				if (!alreadAssign(p)) {
					allAssigned = false;
					break;
				}
			}
			if (allAssigned)
				break;
			List<Integer> pcp = this.findPCP(t);
			assignPCP(pcp);
			updateTaskTime(pcp);

		}
	}
	/**
	 * This method updates the successors and predecessors of each task instance in a pcp, then assignParents method is called for each task.
	 * @param pcp
	 */
	private void updateTaskTime(List<Integer> pcp){
		for (int i = 0; i <= pcp.size()-1; i++){
			Integer task = pcp.get(i);
			updateSuccessor(task);
			updatePredecessor(task);
			assignParents(task);
		}
	}
	/**
	 * Update the EST and EFT of all the successors of assigned task instances.
	 * @param key
	 */
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
			if (nodeParents.containsKey(task)) {
				for (Integer str : nodeParents.get(task)) {
					if (!q.contains(str))
						q.add(str);
				}
			}
		}
	}
	/**
	 * Update the LFT by calling the computeLFT method.
	 * @param key
	 */
	private void updateLFT(Integer key){
		List<Double> value = taskTime.get(key);
		value.set(3,computeLFT(key));
	}
	
	/**
	 * Update the EFT by using the updated MET and EST.
	 * @param key
	 */
	private void updateEFT(Integer key) {
		List<Double> tmp = taskTime.get(key);
		double eft = tmp.get(1) + tmp.get(0);
		tmp.set(2, eft);
	}
	/**
	 * Update the LFF of each task instance.
	 * @param key
	 * @return
	 */
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
	/**
	 * Update the EST of each task instance.
	 * @param key
	 */
	private void updateEST(Integer key) {
		double max = Double.NEGATIVE_INFINITY;
		max = computeEST(key);
		taskTime.get(key).set(1, max);
	}
	/**
	 * This method computes the EST of a task instance
	 * @param key
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
	 * This methods returns a pcp by a starting task instance. 
	 * @param key
	 * @return the pcp will not include the input task instance and the order is reversed.
	 */
	private List<Integer> findPCP(Integer key) {
		List<Integer> pcp = new ArrayList<Integer>();
		pcp.add(key);
		findMaxParent(key, pcp);
		pcp.remove(0);
		System.out.println(pcp.toString());
		return pcp;
	}
	/**
	 * This method finds the max unassigned parent task and add it into the pcp with updating the starting task.
	 * @param key
	 * @param res
	 */
	private void findMaxParent(Integer key, List<Integer> res) {
		double max = Double.NEGATIVE_INFINITY;
		Integer p = null;
		if (!nodeParents.containsKey(key))
			return;
		for (Integer parent : nodeParents.get(key)) {
			double tmp = taskTime.get(parent).get(2) + edgeMap.get(parent).get(key);
			if (tmp > max && !alreadAssign(parent)) {
				max = tmp;
				p = parent;
			}
		}
		if (p != null) {
			res.add(p);
			findMaxParent(p, res);
		}
	}
	
	public TASK getTASK(Integer t){
		TASK currentTask = null;
		for(TASK task: sch.T){
			if(task.t == t)
				currentTask = task;
		}
		return currentTask;
	}
	/**
	 * This method checks if the input task has child task, which is in a path already.
	 * @param task
	 * @param path
	 * @return the task instance index
	 */
	public Integer hasChildOnAssignedPath(Integer task,  ArrayList<Integer> path){
		Integer child = null;
		for(Integer t: path){
			if(edgeMap.get(task).containsKey(t)){
				child = t;
				break;
			}
		}
		return child;
	}
	/**
	 * This method checks if the input task is the child task of one task in the path. 
	 * @param task
	 * @param path
	 * @return
	 */
	public boolean hasParentOnAssignedPath(Integer task, ArrayList<Integer> path){
		boolean has = false;
		for(Integer t: path){
			if(edgeMap.get(t).containsKey(task)){
				has = true;
				break;
			}
		}
		return has;
	}
	/**
	 * This method assigns a PCP with two situations: first assigning partial path to the available VM instance in the VM pool. 
	 * second assigning the path to the cheapest VM instance.
	 * @param path
	 */
	public void assignPCP(List<Integer> path) {
		boolean assigned = false;
		Integer lastTask = path.get(0);
		Integer firstTask = path.get(path.size() - 1);
		if (sch.VMpool.isEmpty()) {
			assignPCPToNewInstance(path);
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
					assigned = true;
					break;
				}

			}
		}
		if (!assigned)
			assignPCPToNewInstance(path);
		// The MET and LFT is updated
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
		// The edge weight is updated to 0, since the pcp will be assigned on VM instance.
		for (int i = path.size() - 1; i > 0; i--) {
			for (Integer index : edgeMap.get(path.get(i)).keySet()) {
				if (path.contains(index)) {
					edgeMap.get(path.get(i)).put(index, 0.0);
				}
			}
		}

	}
	/**
	 * Get the EST of the last task to make sure the AST should be bigger than the EST
	 */
	private double[] getExecutionTime(List<Integer> path, String server) {
		double[] result = {0.0,0.0};
		double firstStart = taskTime.get(path.get(path.size()-1)).get(1);
		double exec = -Double.MAX_VALUE;
		for (int i = path.size() - 1; i >= 0; i--) {
			if(taskTime.get(path.get(i)).get(1) >= exec){
				exec = taskTime.get(path.get(i)).get(1);
			}
			exec += execTime.get(path.get(i)).get(server);
		}
		
		result[0] = firstStart;
		result[1] = exec;
		return result;
	}
	/**
	 * This method assigns a path to a cheapest VM instance by having the LFT constrain.
	 * @param path
	 */
	private void assignPCPToNewInstance(List<Integer> path) {
		double cost = java.lang.Double.MAX_VALUE;
		String selectServer="";
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
		for(int i= path.size()-1; i>=0; i--){
			
			TASK task = new TASK();
			task.t = path.get(i);
			if(taskTime.get(path.get(i)).get(1)>firstAst){
				firstAst = taskTime.get(path.get(i)).get(1);
			}
			task.ast = firstAst;
			task.aft = firstAst + execTime.get(path.get(i)).get(selectServer);
			sch.T.add(task); 
			vm.runTask.add(path.get(i));
			firstAst = task.aft;
		}
		
		double[] startAndEnd = getExecutionTime(path,selectServer);
		vm.pro = startAndEnd[0] - VMADelay;
		vm.available = startAndEnd[1];
		vm.depro = Math.ceil(startAndEnd[1]/time_interval)*time_interval;
		vm.vleft =vm.depro - vm.available;
		vm.vmType = selectServer;
		sch.VMpool.add(vm);
	}
	/**
	 * The LFT constrain will make sure the workflow will be finished before the deadline.
	 * @param path
	 * @param server
	 * @return
	 */
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
	/**
	 * The method calculates the cost for a specific VM type.
	 * @param path
	 * @param server
	 * @return
	 */
	private double calculateCost(List<Integer> path, String server) {
		double[] timePeroid = getExecutionTime(path, server);
		double exec = timePeroid[1] - timePeroid[0];
		double cost = Math.ceil(exec / time_interval) * server_cost.get(server);
		return cost;
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
