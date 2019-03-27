package dataview.planners;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dataview.models.GlobalSchedule;
import dataview.models.LocalSchedule;
import dataview.models.TaskSchedule;
import dataview.models.Workflow;

/**
 * This E2C2D algorithm is implemented for the paper "sgx-e2c2d: Scheduling algorithm"
 * The algorithm needs to take the task execution time in different VM types and the data trasnfer time as the input.
 * @author Ishtiaq Ahmed
 */

public class SGX_E2C2D_TodayFri extends WorkflowPlanner{
	private double deadline;
	private int totalVMs;
	private double billingCycle;
	private int totalTasks;

	private final double INF = Integer.MAX_VALUE;
	private final double MIN = Integer.MIN_VALUE; 
	private final boolean FAL = false;
	private final boolean TRU = true;



	private double[][] transferTime;
	private boolean[][] graph;



	/* first one is the fastest machine while the last one is the cheapest.

	 */

	double[] costPerBillingCycle; 
	int[] taskAllocationMap;
	double[] EST;
	double[] EFT;
	double[] LFT;

	boolean isVisited[];



	/*for topological sorting*/
	boolean isColored[];



	ArrayList<Integer> list = new ArrayList<Integer>();
	ArrayList<Integer> confidentialTasks = new ArrayList<Integer>();



	private HashMap<Integer, Boolean> isSourceNode = new HashMap<Integer, Boolean>();
	private HashMap<Integer, Boolean> isDestinationNode = new HashMap<Integer, Boolean>();
	private HashMap<Integer, HashMap<String, Double>> execTime;
	private Map<Integer, Map<String, Double>> execTime1;
	private Map<Integer, Map<Integer, Double>> tt;
	ArrayList<VMI> existingAllocations = new ArrayList<VMI>();
	ArrayList<VMI> toBeDeletedVMi = new ArrayList<VMI>();





	public GlobalSchedule plan() {
		deadline = 10;
		totalVMs = 3;
		billingCycle = 10;
		totalTasks = w.getNumOfTasks();
		costPerBillingCycle = new double[]{5, 2, 1};
		
		execTime1 = w.getExecutionTime();
		tt = w.getTransferTime();

		/*execTime1 = new HashMap<Integer, HashMap<String, Double>>();
		HashMap<String, Double> et;
		et = new HashMap<String, Double>(); et.put("VM1", 0.0); et.put("VM2", 0.0); et.put("VM3", 0.0); execTime1.put(0, et);
		et = new HashMap<String, Double>(); et.put("VM1", 2.0); et.put("VM2", 5.0); et.put("VM3", 8.0); execTime1.put(1, et);
		et = new HashMap<String, Double>(); et.put("VM1", 5.0); et.put("VM2", 12.0); et.put("VM3", 16.0); execTime1.put(2, et);
		et = new HashMap<String, Double>(); et.put("VM1", 3.0); et.put("VM2", 5.0); et.put("VM3", 9.0); execTime1.put(3, et);
		et = new HashMap<String, Double>(); et.put("VM1", 4.0); et.put("VM2", 6.0); et.put("VM3", 10.0); execTime1.put(4, et);
		et = new HashMap<String, Double>(); et.put("VM1", 3.0); et.put("VM2", 8.0); et.put("VM3", 11.0); execTime1.put(5, et);
		et = new HashMap<String, Double>(); et.put("VM1", 4.0); et.put("VM2", 8.0); et.put("VM3", 11.0); execTime1.put(6, et);
		et = new HashMap<String, Double>(); et.put("VM1", 5.0); et.put("VM2", 8.0); et.put("VM3", 11.0); execTime1.put(7, et);
		et = new HashMap<String, Double>(); et.put("VM1", 3.0); et.put("VM2", 6.0); et.put("VM3", 8.0); execTime1.put(8, et);
		et = new HashMap<String, Double>(); et.put("VM1", 5.0); et.put("VM2", 8.0); et.put("VM3", 14.0); execTime1.put(9, et);
		et = new HashMap<String, Double>(); et.put("VM1", 0.0); et.put("VM2", 0.0); et.put("VM3", 0.0); execTime1.put(-1, et);

		tt = new HashMap<Integer, HashMap<Integer, Double>>();
		HashMap<Integer, Double> time;
		time = new HashMap<Integer, Double>(); time.put(1, 0.0); time.put(2, 0.0); time.put(3, 0.0); tt.put(0, time);
		time = new HashMap<Integer, Double>(); time.put(4, 1.0); tt.put(1, time);
		time = new HashMap<Integer, Double>(); time.put(5, 2.0); time.put(6, 2.0); tt.put(2, time);
		time = new HashMap<Integer, Double>(); time.put(6, 2.0); tt.put(3, time);
		time = new HashMap<Integer, Double>(); time.put(7, 1.0); time.put(8, 1.0); tt.put(4, time);
		time = new HashMap<Integer, Double>(); time.put(8, 4.0); tt.put(5, time);
		time = new HashMap<Integer, Double>(); time.put(9, 3.0); tt.put(6, time);
		time = new HashMap<Integer, Double>(); time.put(-1, 0.0); tt.put(7, time);
		time = new HashMap<Integer, Double>(); time.put(-1, 0.0); tt.put(8, time);
		time = new HashMap<Integer, Double>(); time.put(-1, 0.0); tt.put(9, time);
*/
		scheduleWorkflow();
		assignChildren();
		GlobalSchedule globalSchedule = createGlobalSchedule();
		return globalSchedule;
	}
	
	
	public SGX_E2C2D_TodayFri(Workflow workflow) {
		super(workflow);
	}
	


	
	private GlobalSchedule createGlobalSchedule() {

		HashSet<Integer> assigned = new HashSet<Integer>();
		GlobalSchedule globalSchedule = new GlobalSchedule();
		for (int i = 0; i < existingAllocations.size(); i++) {
			VMI vmi = existingAllocations.get(i);
			LocalSchedule localSchedule = new LocalSchedule();
			for (int j = 0; j < vmi.path.size(); j++) {
				int task = vmi.path.get(j);
				TaskSchedule taskSchedule = w.getTaskSchedule(w.getTask(task - 1));
				localSchedule.addTaskSchedule(taskSchedule);
				assigned.add(task);
				
			}
			String vmType = null;
			if (vmi.assignedVMIndex == 0) {
				vmType = "VM1";
			} else if (vmi.assignedVMIndex == 1) {
				vmType = "VM2";
			} else if (vmi.assignedVMIndex == 2) {
				vmType = "VM3";
			}
			localSchedule.setVmType(vmType);
			globalSchedule.addLocalSchedule(localSchedule);

		}
		
		
		for (int task = 1; task < totalTasks - 2; task++) {
			if (!assigned.contains(task)) {
				assigned.add(task);
				LocalSchedule localSchedule = new LocalSchedule();
				TaskSchedule taskSchedule = w.getTaskSchedule(w.getTask(task - 1));
				localSchedule.addTaskSchedule(taskSchedule);
				localSchedule.setVmType("VM1");
				globalSchedule.addLocalSchedule(localSchedule);
			}
		}

		
		return globalSchedule;
	}

	private void assignChildren() {
		ArrayList<Integer> list = topologicalSort1(); 
		for (Integer i : list) {
			System.out.println(i);
		}
		while(!list.isEmpty()) {
			ArrayList<Integer> criticalPath = new ArrayList<Integer>();
			int task = list.get(0);
			criticalPath.add(task);
			while(hasChild(task)) {
				task = criticalChild(task);
				criticalPath.add(task);
			}

			ArrayList<ArrayList<Integer>> paths = splitCriticalPathByConfidentialTask(criticalPath);

			for(ArrayList<Integer> path : paths) {
				allocateVirtualMachine(path);
				for (Integer t : path) {
					isVisited[t] = true;
				}
				for(int t : path) {
					calculateEST_EFT(t);
					calculateLFT(t);
				}

				/* removing all tasks and edges invloving tasks in path from G
				 * removing all tasks involving in path from list*/
				for (Integer t : path) {
					isVisited[t] = true;
					list.remove(t);
				}
			}			
		}
	}


	private boolean allocateVirtualMachine(ArrayList<Integer> path) {
		ArrayList<VMI> instancesWithoutUnusedTime = createVMIsWithoutUnusedTime(path);
		if (instancesWithoutUnusedTime.isEmpty()) {
			System.out.println("Schedule is not possible within the provided deadline");
			return false;
		}

		Collections.sort(instancesWithoutUnusedTime);
		VMI cheapestToken = instancesWithoutUnusedTime.get(0);
		ArrayList<VMI> instanceUtilizingUnusedTime = createVMIsUtilizingUnusedTime(instancesWithoutUnusedTime);
		if (!instanceUtilizingUnusedTime.isEmpty()) {
			Collections.sort(instanceUtilizingUnusedTime);
			VMI cheapestCombined = instanceUtilizingUnusedTime.get(0);
			if (cheapestToken.cost >= cheapestCombined.additionalCost) {
				cheapestToken = cheapestCombined;
			}
		}
		existingAllocations.add(cheapestToken);
		updateGlobalEST_EFT_LFT_vmMapping(cheapestToken);
		return true;

	}

	private ArrayList<VMI> createVMIsUtilizingUnusedTime(ArrayList<VMI> instancesWithoutUnusedTime) {
		ArrayList<VMI> vmiUtilizingUnusedTime = new ArrayList<VMI>();
		toBeDeletedVMi= new ArrayList<VMI>();
		for (VMI existingVmi : existingAllocations) {
			for (VMI token : instancesWithoutUnusedTime) {
				VMI vmi = insertAtEnd(existingVmi, token);
				if (vmi != null) {
					vmiUtilizingUnusedTime.add(vmi);
				}
			}
		}
		for (VMI vm : toBeDeletedVMi) {
			existingAllocations.remove(vm);
		}
		return vmiUtilizingUnusedTime;
	}

	private VMI insertAtEnd(VMI existingVmi, VMI token) {
		if (existingVmi.assignedVMIndex != token.assignedVMIndex) return null;
		/* checking the first task of token is the child of the last task of existingVmi*/
		/* Checking the first task of token has only one parent*/
		int lastTask = existingVmi.path.get(existingVmi.path.size() - 1);
		int firstTask = token.path.get(0);
		if (isChild(lastTask, firstTask)) {
			ArrayList<Integer> combinedPath = new ArrayList<Integer>(existingVmi.path);
			for (int i = 0; i < token.path.size(); i++) {
				combinedPath.add(token.path.get(i));
			}
			if (hasOnlyOneParent(firstTask)) {
				token.previous = existingVmi;
				double tt = transferTime[lastTask][firstTask];
				for (int i = 0; i < token.path.size(); i++) {
					token.EST[token.path.get(i)] -= tt;
					token.EFT[token.path.get(i)] -= tt;
				}
				/*double newFinishTime = token.LFT[token.path.get(token.path.size() - 1)];
				for(Integer t : combinedPath) {
					token.LFT[t] = newFinishTime;
					newFinishTime -= execTime.get(token.assignedVMIndex + "").get(t);
				}*/
			} 
			token.path = combinedPath;
			//calculating cost
			double totalExecutionTime = token.EFT[token.path.get(token.path.size() - 1)] - token.EST[token.path.get(0)];
			double cost = Math.ceil(totalExecutionTime / billingCycle) * costPerBillingCycle[token.assignedVMIndex];
			token.cost = cost;
			token.additionalCost = token.cost - existingVmi.cost;
			toBeDeletedVMi.add(existingVmi);
			return token;
		}

		return null;
	}

	private boolean isChild(int lastTask, int firstTask) {
		for (int i = 1; i < totalTasks - 1; i++) {
			if (graph[lastTask][firstTask]) {
				return true;
			}
		}
		return false;
	}

	private void updateGlobalEST_EFT_LFT_vmMapping(VMI instance) {
		EST = Arrays.copyOf(instance.EST, EST.length);
		EFT = Arrays.copyOf(instance.EFT, EFT.length);
		LFT = Arrays.copyOf(instance.LFT, LFT.length);
		ArrayList<Integer> path = instance.path;
		for (int i = 0; i < path.size(); i++) {
			taskAllocationMap[path.get(i)] = instance.assignedVMIndex;
		}
	}


	private ArrayList<VMI> createVMIsWithoutUnusedTime(ArrayList<Integer> path) {
		/* Trying to create new instance for each of the Machine*/
		ArrayList<VMI> instancesWithoutUnusedTime = new ArrayList<VMI>();
		for (int vm = 0; vm < totalVMs; vm++) {
			if(canSatisfy(path, vm)) {
				VMI dummyVMI = dummyAllocation(path, vm);
				if (dummyVMI != null) {
					instancesWithoutUnusedTime.add(dummyVMI);
				}
			}
		}
		return instancesWithoutUnusedTime;
	}

	/*to preserve the state*/
	private VMI dummyAllocation(ArrayList<Integer> path, int vm) {
		/* Before coming here, already passed the validation whether the intended state is correct*/
		VMI instance = new VMI();
		instance.EST = EST.clone();
		instance.EFT = EFT.clone();
		instance.LFT = LFT.clone();
		instance.assignedVMIndex = vm;
		instance.path = new ArrayList<Integer>(path);


		// updating instance EST, EFT
		double newStartTime = instance.EST[instance.path.get(0)];
		for (int i = 0; i < instance.path.size(); i++) {
			int task = instance.path.get(i);
			instance.EST[task] = newStartTime;
			double executionTime = execTime.get(task).get(vm + ""); 
			instance.EFT[task] = instance.EST[task] + executionTime;
			newStartTime = instance.EFT[task];
		}

		// updating instance LFT
		int lastTask = instance.path.get(instance.path.size() - 1);
		double newFinishTime = instance.LFT[lastTask];
		for (int i = instance.path.size() - 1; i >= 0; i--) {
			int task = instance.path.get(i);
			instance.LFT[task] = newFinishTime;
			newFinishTime -= execTime.get(task).get(vm + ""); 
		}


		int size = instance.path.size();
		double totalExecutionTime = instance.EFT[instance.path.get(size - 1)] - instance.EST[instance.path.get(0)];
		double cost = Math.ceil(totalExecutionTime / billingCycle) * costPerBillingCycle[vm];
		if (instance.previous != null) {
			instance.additionalCost = cost;
		}
		instance.cost = cost;
		return instance;
	}


	/* Checking whether each of the EFT meets the LFT*/
	private boolean canSatisfy(ArrayList<Integer> path, int vm) {

		double newStartTime = EST[path.get(0)];
		for (int i = 0; i < path.size(); i++) {
			int task = path.get(i);
			double executionTime = execTime.get(task).get(vm + "");  
			/* We also need to check whether the new EST is at least or greater than  previous EST*/
			if (EST[task] <= newStartTime && newStartTime + executionTime <= LFT[task]) {
				newStartTime += executionTime;
			} else {
//				return false;
			}
		}
		return true;
	}

	private boolean hasOnlyOneParent(int firstTask) {
		int parentCount = 0;
		for (int i = 1; i < totalTasks - 1; i++) {
			if(graph[i][firstTask]) {
				parentCount++;
			}
		}
		return (parentCount > 1) ? false : true;
	}


	private ArrayList<ArrayList<Integer>> splitCriticalPathByConfidentialTask(ArrayList<Integer> criticalPath) {
		ArrayList<ArrayList<Integer>> paths = new ArrayList<ArrayList<Integer>>();
		if (confidentialTasks.isEmpty()) {
			paths.add(criticalPath);
			return paths;
		}

		ArrayList<Integer> path = new ArrayList<Integer>();
		for (int i = 0; i < criticalPath.size(); i++) {
			if(confidentialTasks.contains(criticalPath.get(i))) {
				if(!path.isEmpty()) {
					paths.add(path);
				}

				path = new ArrayList<Integer>();
				/*Checking consecutive confidentialTasks*/
				for (int j = i; j < criticalPath.size(); j++) {
					if(confidentialTasks.contains(criticalPath.get(i))) {
						path.add(criticalPath.get(i));
					} else {
						paths.add(path);
						i = j;
						break;
					}
				}
				/*clearing the path list for next use*/
				path = new ArrayList<Integer>();
			} else {
				path.add(criticalPath.get(i));
			}
		}
		return paths;
	}

	private int criticalChild(int task) {
		int criticalChild = -1;
		double minimumLFT = Integer.MAX_VALUE;
		for (int i = 1; i < totalTasks - 1; i++) {
			if (graph[task][i] && !isVisited[i]) {
				if (LFT[i] < minimumLFT) {
					minimumLFT = LFT[i];
					criticalChild = i;
				}
			}
		}
		return criticalChild;
	}


	private boolean hasChild(int task) {
		for (int i = 1; i < totalTasks - 1; i++) {
			if (graph[task][i] && !isVisited[i]) {
				return true;
			}
		}
		return false;
	}

	ArrayList<Integer> topologicalSort1() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i = 1; i < totalTasks - 1; i++) {
			result.add(i);
		}
		for (int i = 0; i < result.size() - 1; i++) {
			for (int j = i + 1; j < result.size(); j++) {
				if (LFT[result.get(i)] > LFT[result.get(j)]) {
					Integer temp = result.get(i);
					result.set(i, result.get(j));
					result.set(j, temp);
				}
			}
		}
		return result;
	}

	private void scheduleWorkflow() {
		preprocessing();
		EST = new double[totalTasks];
		Arrays.fill(EST, MIN);
		EST[0] = 0;
		EFT = new double[totalTasks];
		Arrays.fill(EFT, MIN);
		EFT[0] = 0;
		LFT = new double[totalTasks];
		Arrays.fill(LFT, INF);
		LFT[totalTasks - 1] = deadline;
		calculateEST_EFT(0);
		calculateLFT(totalTasks - 1);
		/*Removing start and end nodes from the graph*/
		isVisited[0] = isVisited[totalTasks - 1] = true;

	}


	private void preprocessing() {
		/*
		 * Initializing isSourceNode and isDestinationNode
		 */
		for (int i = 1; i <= totalTasks; i++) {
			isSourceNode.put(i, true);
			isDestinationNode.put(i, true);
		}

		/* 
		 * Since we added two extra tasks, 'start', 'end', we added with 2; Therefore we added by 1 with each adjacency task index so simulate
		 * start node starts with zero and end index start with totalTasks;
		 */

		/*
		 * Constructing graph
		 */

		totalTasks += 2;
		graph = new boolean[totalTasks][totalTasks];

		/*

		 * Constructing Transfer time between tasks

		 */

		transferTime = new double[totalTasks][totalTasks];
		for (int i = 0; i < transferTime.length; i++) {
			Arrays.fill(transferTime[i], INF);
		}
		Iterator it = tt.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			System.out.println(pair.getKey() + " = " + pair.getValue());
			int source = (int) pair.getKey();
			HashMap<Integer, Double> vMap = (HashMap<Integer, Double>) pair.getValue();
			Iterator it1 = vMap.entrySet().iterator();
			while (it1.hasNext()) {
				Map.Entry pair1 = (Map.Entry)it1.next();
				System.out.println(pair1.getKey() + " = " + pair1.getValue());
				int destination = (int) pair1.getKey();
				if (destination == -1) {
					destination = totalTasks - 1;
				}
				transferTime[source][destination] = (double) pair1.getValue();
			}

		}




		/*

		 * iterating each of the adjacency list

		 */

		for (int i = 0; i < alist.length; i++) {

			List<Integer> dest = alist[i];
			for (int j = 0; j < dest.size(); j++) {
				System.out.println("source : " + (i + 1) + " destination : " + (dest.get(j) + 1) );
				graph[(i + 1)][dest.get(j) + 1] = true;
				isSourceNode.put(dest.get(j) + 1, false);
				isDestinationNode.put(i + 1, false);
			}

		}


		/*
		 * finding source Nodes
		 */
		ArrayList<Integer> sourceNodes = returnIndices(isSourceNode);
		/*
		 * finding destination Nodes
		 */
		ArrayList<Integer> destinationNodes = returnIndices(isDestinationNode);
		/*
		 * Now making connection between start node to source nodes
		 */

		for (int i = 0; i < sourceNodes.size(); i++) {
			System.out.println("source Node : " + sourceNodes.get(i));
			graph[0][sourceNodes.get(i)] = true;
			transferTime[0][sourceNodes.get(i)] = 0;
		}



		/*
		 * Now making connection between destination node to end nodes
		 */

		for (int i = 0; i < destinationNodes.size(); i++) {
			System.out.println("destination Node : " + destinationNodes.get(i));
			graph[destinationNodes.get(i)][totalTasks - 1] = true;
			transferTime[destinationNodes.get(i)][totalTasks - 1] = 0;
		}

		/*

		 * Registering execution time

		 */



		/*

		 * This is for fixed execution time of ConcreteWorkflowOne, we considered we have only three different virtual machine

		 */

		execTime = new HashMap<Integer, HashMap<String, Double>>();
		it = execTime1.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			System.out.println(pair.getKey() + " = " + pair.getValue());
			int task = (int) pair.getKey();
			if (task == -1) {
				task = totalTasks - 1;
			}
			HashMap<String, Double> et = (HashMap<String, Double>) pair.getValue();
			HashMap<String, Double> newEt = new HashMap<String, Double>();
			newEt.put("0", et.get("VM1")); 
			newEt.put("1", et.get("VM2")); 
			newEt.put("2", et.get("VM3"));
			execTime.put(task, newEt);

		}


		isVisited = new boolean[totalTasks];

		/*TaskAllocation to vm mapping initialization

		 */

		taskAllocationMap = new int[totalTasks];
		Arrays.fill(taskAllocationMap, -1);

		//TODO/*These are hardcoded confidential tasks*/

		//		confidentialTasks.add(-1);

	}


	private void calculateEST_EFT(int parent) {

		for (int child = 0; child < totalTasks; child++) {

			if (graph[parent][child] && !isVisited[child]) {				

				if (EST[child] < EFT[parent] + transferTime[parent][child]) {

					EST[child] = EFT[parent] + transferTime[parent][child];

					EFT[child] = EST[child] + findMinimumExecution(child);

					calculateEST_EFT(child);

				}

			}

		}

	}



	void calculateLFT(int child) {

		for (int parent = 0; parent < totalTasks; parent++) {

			if (graph[parent][child] && !isVisited[parent]) {

				if (LFT[parent] > LFT[child] -  findMinimumExecution(child) - transferTime[parent][child]) {

					LFT[parent] = LFT[child] -  findMinimumExecution(child) - transferTime[parent][child];

					calculateLFT(parent);

				}

			}

		}

	}



	private double findMinimumExecution(int task) {

		if (isVisited[task]) {

			return execTime.get(task).get(taskAllocationMap[task] + "");

		}

		double minimumExecution = Integer.MAX_VALUE;

		for (int vm = 0; vm < totalVMs; vm++) {

			if (minimumExecution > execTime.get(task).get(vm + "")) {

				minimumExecution = execTime.get(task).get(vm + "");

			}

		}

		return minimumExecution;

	}



	ArrayList<Integer> returnIndices(HashMap<Integer, Boolean> map) {

		ArrayList<Integer> indices = new ArrayList<Integer>();

		Iterator<?> it = map.entrySet().iterator();

		while (it.hasNext()) {

			Map.Entry pair = (Map.Entry)it.next();

			if ((boolean)pair.getValue()) {

				indices.add((Integer) pair.getKey());

			}

		}

		return indices;

	}

}

