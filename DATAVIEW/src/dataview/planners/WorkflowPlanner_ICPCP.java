package dataview.planners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import dataview.models.GlobalSchedule;
import dataview.models.LocalSchedule;
import dataview.models.Task;
import dataview.models.TaskSchedule;
import dataview.models.Workflow;
import dataview.models.WorkflowEdge;

/**
 * This IC-PCP algorithm is implemented based on this paper "Deadline-constrained workflow scheduling algorithms for Infrastructure as a Service Clouds"
 * The algorithm needs to take the task execution time in different VM types and the data trasnfer time as the input.
 * @author changxinbai
 *
 */
public class WorkflowPlanner_ICPCP extends WorkflowPlanner {
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
	private Map<Integer, double[]> taskTime = new HashMap<Integer, double[]>();

	private Map<String, LinkedHashMap<Integer, List<Integer>>> taskAssignedVM = new HashMap<String, LinkedHashMap<Integer, List<Integer>>>();
	private Map<Integer, List<Double>> task_aet = new HashMap<Integer, List<Double>>(); // the
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
	/*
	 * Deadline, time_interval and server_cost information provided by
	 * 
	 */
	static {
		D = 750000;
		time_interval = 10;
		server_cost.put("t2.xlarge", 5.0);
		server_cost.put("t2.large", 2.0);
		server_cost.put("t2.micro", 1.0);
	
	}

	class ServerandCost {
		String server;
		Double cost;
	}

	public WorkflowPlanner_ICPCP(Workflow w) {
		super(w);
		execTime = w.getExecutionTime();
		System.out.println(execTime);
		edgeMap = w.getTransferTime();
		System.out.println(edgeMap);
	}

	private void init() {
		nodeParents = this.getNodeParents(w);
		ArrayList<Double> tmpDouble1 = new ArrayList<>();
		tmpDouble1.add((double) 0);
		tmpDouble1.add((double) 0);
		task_aet.put(t_entry, tmpDouble1);
		ArrayList<Double> tmpDouble2 = new ArrayList<>();
		tmpDouble2.add((double) 0);
		tmpDouble2.add(D);
		task_aet.put(t_exit, tmpDouble2);
	}

	public GlobalSchedule plan() {
		this.init();
		this.buildTaskTime();
		this.assignParents(t_exit);
		// System.out.println(task_assignment);
		System.out.println(taskAssignedVM);
		System.out.println(task_aet);
		// System.out.println(running_server);
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

	private void buildTaskTime() {
		this.doTopologicalSorting();
		// System.out.println(topoLogicalOrder);
		for (Integer key : topoLogicalOrder) {
			if (key.equals(t_entry)) {
				taskTime.put(key, new double[4]);
			} else {
				double[] tmp = new double[4];
				tmp[0] = computeMET(key);
				tmp[1] = computeEST(key);
				tmp[2] = tmp[0] + tmp[1];
				taskTime.put(key, tmp);
			}

		}
		for (int i = topoLogicalOrder.size() - 1; i >= 0; i--) {
			Integer key = topoLogicalOrder.get(i);
			double[] value = taskTime.get(key);
			value[3] = computeLFT(key);
			taskTime.put(key, value);

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

	private void assignParents(Integer t) {
		while (true) {
			boolean allAssigned = true;
			for (Integer p : nodeParents.get(t)) {
				if (!task_aet.containsKey(p)) {
					allAssigned = false;
					break;
				}
			}
			if (allAssigned)
				break;
			List<Integer> pcp = this.findPCP(t);
			String server = assignPCP(pcp);
			/*
			 * System.out.println("This is MEX update"); for(Integer str :
			 * taskTime.keySet()){ double[] tmp = taskTime.get(str); for(int i =
			 * 0; i < tmp.length; i++){ System.out.print(" " + tmp[i]); }
			 * System.out.println(); }
			 */

			updateTaskTime(pcp, server);

		}
	}

	private void updateTaskTime(List<Integer> pcp, String server) {
		for (int i = pcp.size() - 1; i >= 0; i--) {
			Integer task = pcp.get(i);
			updateSuccessor(task);
			updatePredecessor(task);
			assignParents(task);
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

	private void updatePredecessor(Integer key) {
		List<Integer> q = new LinkedList<Integer>();
		q.add(key);
		while (!q.isEmpty()) {
			Integer task = q.get(0);
			q.remove(0);
			this.computeLFT(task);
			if (nodeParents.containsKey(task)) {
				for (Integer str : nodeParents.get(task)) {
					if (!q.contains(str))
						q.add(str);
				}
			}
		}
	}

	private void updateEFT(Integer key) {
		double[] tmp = taskTime.get(key);
		tmp[2] = tmp[0] + tmp[1];
		taskTime.put(key, tmp);
	}

	private double computeLFT(Integer key) {
		double min = Double.POSITIVE_INFINITY;
		if (key.equals(t_exit)) {
			return D;
		} else {
			for (Integer str : edgeMap.get(key).keySet()) {
				double tmp = taskTime.get(str)[3] - taskTime.get(str)[0] - edgeMap.get(key).get(str);
				if (tmp < min) {
					min = tmp;
				}
			}
		}
		taskTime.get(key)[3] = min;
		return min;
	}

	private void updateEST(Integer key) {
		double max = Double.NEGATIVE_INFINITY;
		if (nodeParents.containsKey(key)) {
			for (Integer st : nodeParents.get(key)) {
				double tmp = taskTime.get(st)[1] + taskTime.get(st)[0] + edgeMap.get(st).get(key).doubleValue();
				if (max < tmp) {
					max = tmp;
				}
			}
		}
		taskTime.get(key)[1] = max;
	}

	private double computeEST(Integer key) {
		double max = Double.NEGATIVE_INFINITY;
		if (nodeParents.containsKey(key)) {
			for (Integer st : nodeParents.get(key)) {
				double tmp = taskTime.get(st)[1] + taskTime.get(st)[0] + edgeMap.get(st).get(key).doubleValue();
				if (max < tmp) {
					max = tmp;
				}
			}
		}
		// taskTime.get(key)[1] = max;

		return max;
	}

	private List<Integer> findPCP(Integer key) {
		List<Integer> pcp = new ArrayList<Integer>();
		pcp.add(key);
		findMaxParent(key, pcp);
		pcp.remove(0);
		System.out.println(pcp.toString());
		return pcp;
	}

	private void findMaxParent(Integer key, List<Integer> res) {
		double max = Double.NEGATIVE_INFINITY;
		Integer p = null;
		if (!nodeParents.containsKey(key))
			return;
		for (Integer parent : nodeParents.get(key)) {
			double tmp = taskTime.get(parent)[2] + edgeMap.get(parent).get(key);
			if (tmp > max && !task_aet.containsKey(parent)) {
				max = tmp;
				p = parent;
			}
		}
		if (p != null) {
			res.add(p);
			findMaxParent(p, res);
		}
	}

	private void updateTask_AET(List<Integer> path, String server) {
		

		double firstEFT = java.lang.Double.NEGATIVE_INFINITY;
		for (int i = path.size() - 1; i >= 0; i--) {

			if (nodeParents.containsKey(path.get(i))) {
				for (Integer index : nodeParents.get(path.get(i))) {
					if (taskTime.get(index)[2] + edgeMap.get(index).get(path.get(i)) > firstEFT) {
						firstEFT = taskTime.get(index)[2] + edgeMap.get(index).get(path.get(i));
					}
				}
			}
			if (task_aet.containsKey(path.get(i))) {
				List<Double> start_endList = task_aet.get(path.get(i));
				start_endList.clear();
				start_endList.add(firstEFT);
				start_endList.add(firstEFT + execTime.get(path.get(i)).get(server));
				task_aet.put(path.get(i), start_endList);
			} else {
				List<Double> start_endList = new ArrayList<>();
				start_endList.add(firstEFT);
				start_endList.add(firstEFT + execTime.get(path.get(i)).get(server));
				task_aet.put(path.get(i), start_endList);
			}
			firstEFT = firstEFT + execTime.get(path.get(i)).get(server);
		}

	}
	
	public void updateTaskAssignedVM(List<Integer> path, String server){
		Integer lastKey = null;
		if (taskAssignedVM.containsKey(server)) {
			Iterator<Integer> iterator = taskAssignedVM.get(server).keySet().iterator();
			while (iterator.hasNext()) {
				lastKey = iterator.next();
			}
			LinkedHashMap<Integer, List<Integer>> newInstance = taskAssignedVM.get(server);
			newInstance.put(lastKey + 1, path);
			taskAssignedVM.put(server, newInstance);
		} else {
			LinkedHashMap<Integer, List<Integer>> tmp = new LinkedHashMap<Integer, List<Integer>>();
			tmp.put(0, path);
			taskAssignedVM.put(server, tmp);
		}
	}

	private String assignPCP(List<Integer> path) {
		String assignedServer = "";
		if (taskAssignedVM.keySet().isEmpty()) {
			assignedServer = assignPCPToNewInstance(path).server;
			updateTaskAssignedVM( path,  assignedServer);
			updateTask_AET(path, assignedServer);
		} else {
			boolean indicator = false;
			for (String str : taskAssignedVM.keySet()) {
				double[] pathPeriod = getExecutionTime(path, str);
				double pathExec = pathPeriod[1] - pathPeriod[0];
				double parthStart = pathPeriod[0];
				double parthend = pathPeriod[1];
				Map<Integer, List<Integer>> tmp = taskAssignedVM.get(str);
				double cost = Double.POSITIVE_INFINITY; 
				for (Integer index : tmp.keySet()) {
					List<Integer> currentpath = tmp.get(index);
					double[] execPair = getExecutionTime(currentpath, str);
					double exec = execPair[1] - execPair[0];
					double remainingTime = Math.ceil(exec / time_interval) * time_interval - exec;
					// double remainingTime = D - exec;
					// if(pathExec <= remainingTime){
					if (pathExec <= remainingTime) {
						Integer lastnode = path.get(0);
						for (Integer id : edgeMap.get(lastnode).keySet()) {
							if (currentpath.indexOf(id) != -1) {
								currentpath.addAll(currentpath.indexOf(id) + 1, path);
								indicator = true;
								assignedServer = str;
								updateTask_AET(currentpath, str);
								path = currentpath;
							}
						}
					}
					else{
						Integer lastNode = currentpath.get(0);
						double finishTime = task_aet.get(lastNode).get(1);
						Integer pathFirstNode = path.get(path.size()-1);
						if(parthStart > finishTime && edgeMap.get(lastNode).containsKey(pathFirstNode) && finishTime +  parthend- parthStart <=D){
							edgeMap.get(lastNode).put(pathFirstNode, (double) 0);
							currentpath.addAll(0, path);
							assignedServer = str;
							updateTask_AET(currentpath, str);
							indicator = true;
						}
					}

				}
			}
			if (!indicator) {
				assignedServer = this.assignPCPToNewInstance(path).server;
				updateTaskAssignedVM( path,  assignedServer);
				updateTask_AET(path, assignedServer);
			}
		}

		// update MET
		for (int i = path.size() - 1; i >= 0; i--) {
			double[] tmp = taskTime.get(path.get(i));
			tmp[0] = execTime.get(path.get(i)).get(assignedServer);
			taskTime.put(path.get(i), tmp);

		}

		// update edge weigth
		for (int i = path.size() - 1; i > 0; i--) {
			for (Integer index : edgeMap.get(path.get(i)).keySet()) {
				if (path.contains(index)) {
					edgeMap.get(path.get(i)).put(index, 0.0);
				}
			}
		}
		return assignedServer;
	}

	private double[] getExecutionTime(List<Integer> path, String server) {
		double[] result = {0.0,0.0};
		double firstStart = 0.0;
		/*
		 * get the EFT of the last node in a path PCP: 9--6--2, 2 is the first
		 * node
		 *
		 */
		double exec = 0.0;
		Integer firstNode = path.get(path.size() - 1);
		for (Integer index : nodeParents.get(firstNode)) {
			if (taskTime.get(index)[2] + edgeMap.get(index).get(firstNode) >= firstStart) {
				firstStart = taskTime.get(index)[2] + edgeMap.get(index).get(firstNode);
			}
		}

		for (int i = path.size() - 1; i >= 0; i--) {

			if (nodeParents.containsKey(path.get(i))) {
				for (Integer index : nodeParents.get(path.get(i))) {
					if (taskTime.get(index)[2] + edgeMap.get(index).get(path.get(i)) >= exec) {
						exec = taskTime.get(index)[2] + edgeMap.get(index).get(path.get(i));
					}
				}
			}
			// List<Double> start_endList = new ArrayList<>();
			// start_endList.add(firstEFT);
			// start_endList.add(firstEFT +
			// execTime.get(path.get(i)).get(server));
			exec += execTime.get(path.get(i)).get(server);
		}
		
		result[0] = firstStart;
		result[1] = exec;
		return result;
		// return exec-firstStart;
	}

	private ServerandCost assignPCPToNewInstance(List<Integer> path) {
		String assignedServer = "";
		double cost = java.lang.Double.MAX_VALUE;
		ServerandCost sc = new ServerandCost();
		for (String server : server_cost.keySet()) {
			if (checkEachTaskFinishedWithLFT(path, server)) {

				double instanceCost = calculateCost(path, server);
				if (instanceCost <= cost) {
					sc.cost = instanceCost;
					sc.server = server;
				}

			}
			;
		}
		return sc;
	}

	private boolean checkEachTaskFinishedWithLFT(List<Integer> path, String server) {
		// Integer firstNode = path.get(path.size() - 1);
		// calculate the AFT to which the LFT will compare
		// Map<Integer, double[]> localtaskTime = taskTime;
		Integer firstNode = path.get(path.size() - 1);
		double firstEFT = java.lang.Double.MAX_VALUE;
		;
		if (nodeParents.containsKey(firstNode)) {
			for (Integer index : nodeParents.get(firstNode)) {
				if (taskTime.get(index)[2] + edgeMap.get(index).get(firstNode) < firstEFT) {
					firstEFT = taskTime.get(index)[2] + edgeMap.get(index).get(firstNode);
				}
			}
		}

		for (int i = path.size() - 1; i >= 0; i--) {
			if (firstEFT + execTime.get(path.get(i)).get(server) > taskTime.get(path.get(i))[3]) {
				return false;
			} else {
				firstEFT = firstEFT + execTime.get(path.get(i)).get(server);
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
			} else if (e.destTask == null) {
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
