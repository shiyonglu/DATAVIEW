package dataview.planners;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dataview.models.Dataview;
import dataview.models.GlobalSchedule;
import dataview.models.LocalSchedule;
import dataview.models.TaskSchedule;
import dataview.models.Workflow;

/*Author : Ishtiaq Ahmed
 * 
 */
class VMI implements Comparable<VMI> {
	double[] EST;
	double[] EFT;
	double[] LFT;
	double cost;
	int assignedVMIndex;
	ArrayList<Integer> path;
	VMI previous;
	double additionalCost;

	@Override
	public int compareTo(VMI vmi) {

		if (this.previous != null && vmi.previous != null) {
			return (int)(this.additionalCost - vmi.additionalCost);
		} else if (this.previous != null) {
			return (int)(this.additionalCost - vmi.cost);
		} else if (vmi.previous != null) {
			return (int) (this.cost - vmi.additionalCost);
		}
		return (int) (this.cost - vmi.cost);
	}
}
 

public class WorkflowPLanner_E2C2D extends WorkflowPlanner{
	private double deadline = 30;
	private int totalVMs = 3;
	private double billingCycle = 10;
	private int totalTasks;


	private final double INF = Integer.MAX_VALUE;
	private final double MIN = Integer.MIN_VALUE; 
	private final boolean FAL = false;
	private final boolean TRU = true;

	//TODO fixed for Paper workflow
	double[][] transferTime = {
			// 0   1     2      3      4       5      6      7     8      9      10
			{INF, 0,     0,     0,     INF,  INF,   INF,   INF, INF,   INF,   INF},// 0
			{INF, INF,   INF,   INF,   1,    INF,   INF,   INF, INF,   INF,   INF}, //1
			{INF, INF,   INF,   INF,   INF,  2,     2,     INF, INF,   INF,   INF}, //2
			{INF, INF,   INF,   INF,   INF,  INF,   2,     INF, INF,   INF,   INF}, //3
			{INF, INF,   INF,   INF,   INF,  INF,   INF,   1,   1,     INF,   INF}, //4
			{INF, INF,   INF,   INF,   INF,  INF,   INF,   INF, 4,     INF,   INF}, //5
			{INF, INF,   INF,   INF,   INF,  INF,   INF,   INF, INF,   3,     INF}, //6
			{INF, INF,   INF,   INF,   INF,  INF,   INF,   INF, INF,   INF,   0  }, //7
			{INF, INF,   INF,   INF,   INF,  INF,   INF,   INF, INF,   INF,   0  }, //8
			{INF, INF,   INF,   INF,   INF,  INF,   INF,   INF, INF,   INF,   0  }, //9
			{INF, INF,   INF,   INF,   INF,  INF,   INF,   INF, INF,   INF,   INF}, //10


	};

	private boolean[][] graph = {
			// 0   1     2      3      4       5      6      7     8      9      10
			{FAL, TRU,   TRU,   TRU,   FAL,  FAL,   FAL,   FAL, FAL,   FAL,   FAL},// 0
			{FAL, FAL,   FAL,   FAL,   TRU,  FAL,   FAL,   FAL, FAL,   FAL,   FAL}, //1
			{FAL, FAL,   FAL,   FAL,   FAL,  TRU,   TRU,   FAL, FAL,   FAL,   FAL}, //2
			{FAL, FAL,   FAL,   FAL,   FAL,  FAL,   TRU,   FAL, FAL,   FAL,   FAL}, //3
			{FAL, FAL,   FAL,   FAL,   FAL,  FAL,   FAL,   TRU, TRU,   FAL,   FAL}, //4
			{FAL, FAL,   FAL,   FAL,   FAL,  FAL,   FAL,   FAL, TRU,   FAL,   FAL}, //5
			{FAL, FAL,   FAL,   FAL,   FAL,  FAL,   FAL,   FAL, FAL,   TRU,   FAL}, //6
			{FAL, FAL,   FAL,   FAL,   FAL,  FAL,   FAL,   FAL, FAL,   FAL,   TRU}, //7
			{FAL, FAL,   FAL,   FAL,   FAL,  FAL,   FAL,   FAL, FAL,   FAL,   TRU}, //8
			{FAL, FAL,   FAL,   FAL,   FAL,  FAL,   FAL,   FAL, FAL,   FAL,   TRU}, //9
			{FAL, FAL,   FAL,   FAL,   FAL,  FAL,   FAL,   FAL, FAL,   FAL,   FAL}, //10
	};

	/*
	 * first one is the fastest machine while the last one is the cheapest.
	 */
	double[] costPerBillingCycle = {5, 2, 1}; 
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
	ArrayList<VMI> existingAllocations = new ArrayList<VMI>();
	ArrayList<VMI> toBeDeletedVMi = new ArrayList<VMI>();

	public WorkflowPLanner_E2C2D(Workflow workflow) {
		super(workflow);
	}

	public GlobalSchedule plan() {
		Dataview.debugger.logSuccessfulMessage("SGX-E2C2D is started");
		scheduleWorkflow();
		assignChildren();
		GlobalSchedule globalSchedule = createGlobalSchedule();
		return globalSchedule;
	}

	private GlobalSchedule createGlobalSchedule() {
		for (int i = 1; i < totalTasks - 1; i++) {
			System.out.println("Task " + i + " " + EST[i] + " " + EFT[i] + " " + LFT[i]);
		}
		double totalCost = 0;
		for (int i = 0; i < existingAllocations.size(); i++) {
			VMI vmi = existingAllocations.get(i);
			System.out.println("Path : ");
			for (int j = 0; j < vmi.path.size(); j++) {
				System.out.print(vmi.path.get(j) + " ");
			}
			System.out.println("Assigned machine : " + vmi.assignedVMIndex + " Cost : " + vmi.cost);
			totalCost += vmi.cost;
		}
		System.out.println("Total cost : " + totalCost);


		GlobalSchedule globalSchedule = new GlobalSchedule();
		for(int i = 0; i <= totalVMs; i++) { /* last VM will be considered for SGX*/
			LocalSchedule localSchedule = new LocalSchedule();
			for (int task = 1; task < totalTasks - 1; task++) {
				if (taskAllocationMap[task] == i) {
					TaskSchedule taskSchedule = w.getTaskSchedule(w.getTask(task - 1));
					localSchedule.addTaskSchedule(taskSchedule);
				}
			}
			if(localSchedule.length() != 0) {
				globalSchedule.addLocalSchedule(localSchedule);
			}
		}
		return globalSchedule;
	}

	private void display() {
		for (int i = 1; i < totalTasks - 1; i++) {
			System.out.println("Task " + i + " " + EST[i] + " " + EFT[i] + " " + LFT[i]);
		}
		double totalCost = 0;
		for (int i = 0; i < existingAllocations.size(); i++) {
			VMI vmi = existingAllocations.get(i);
			System.out.println("Path : ");
			for (int j = 0; j < vmi.path.size(); j++) {
				System.out.print(vmi.path.get(j) + " ");
			}
			System.out.println("Assigned machine : " + vmi.assignedVMIndex + " Cost : " + vmi.cost);
			totalCost += vmi.cost;
		}
		System.out.println("Total cost : " + totalCost);
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

	private boolean hasOnlyOneParent(int firstTask) {
		int parentCount = 0;
		for (int i = 1; i < totalTasks - 1; i++) {
			if(graph[i][firstTask]) {
				parentCount++;
			}
		}
		return (parentCount > 1) ? false : true;
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
				return false;
			}
		}
		return true;
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

	ArrayList<Integer> topologicalSort() {
		isColored = new boolean[totalTasks];
		isColored[0] = isColored[totalTasks - 1] = true;
		/*for (int i = totalTasks -1; i >= 0; i--) {
			if(!isColored[i]) {
				findTopologicalOrder(i);
			}
		}*/
		for (int i = 1; i < totalTasks - 1; i++) {
			if(!isColored[i]) {
				findTopologicalOrder(i);
			}
		}
		int size = list.size();
		for (int i = 0; i <  size / 2; i++) {
			int temp = list.get(i);
			list.set(i, list.get(size - 1 - i));
			list.set(size - 1 - i, temp);
		}
		return list;
	}

	void findTopologicalOrder(int task) {
		for(int i = 1; i < totalTasks; i++) {
			if (graph[task][i] && !isColored[i]) {
				findTopologicalOrder(i);
			}
		}
		list.add(task);
		isColored[task] = true;
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
		totalTasks = w.getNumOfTasks();

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
		//TODO
		//		transferTime = new double[totalTasks][totalTasks];


		/*
		 * iterating each of the adjacency list
		 */
		for (int i = 0; i < alist.length; i++) {
			List<Integer> dest = alist[i];
			for (int j = 0; j < dest.size(); j++) {
				Dataview.debugger.logSuccessfulMessage("source : " + (i + 1) + " destination : " + (dest.get(j) + 1) );
				graph[(i + 1)][dest.get(j) + 1] = true;

				//TODO Lets assume we have fixed number of transfer time for this experiment
				//				transferTime[(i + 1)][dest.get(j) + 1] = 5;

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
			Dataview.debugger.logSuccessfulMessage("source Node : " + sourceNodes.get(i));
			graph[0][sourceNodes.get(i)] = true;
			transferTime[0][sourceNodes.get(i)] = 0;
		}

		/*
		 * Now making connection between destination node to end nodes
		 */
		for (int i = 0; i < destinationNodes.size(); i++) {
			Dataview.debugger.logSuccessfulMessage("destination Node : " + destinationNodes.get(i));
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


		/*
		 * Registering execution time
		 */

		/*
		 * This is for fixed execution time of ConcreteWorkflowOne, we considered we have only three different virtual machine
		 */
		//TODO  new HashMap<Integer(index of task), HashMap<String(vm type), Double(execution time))

		for (int task = 0; task < totalTasks; task++ ) {
			HashMap<String, Double> et = new HashMap<String, Double>();
			if (task == 0) {
				et.put("0", 0d); et.put("1", 0d); et.put("2", 0d);
			} else if (task == 1) {
				et.put("0", 2d); et.put("1", 5d); et.put("2", 8d);
			} else if (task == 2) {
				et.put("0", 5d); et.put("1", 12d); et.put("2", 16d);
			} else if (task == 3) {
				et.put("0", 3d); et.put("1", 5d); et.put("2", 9d);
			} else if (task == 4) {
				et.put("0", 4d); et.put("1", 6d); et.put("2", 10d);
			} else if (task == 5) {
				et.put("0", 3d); et.put("1", 8d); et.put("2", 11d);
			} else if (task == 6) {
				et.put("0", 4d); et.put("1", 8d); et.put("2", 11d);
			} else if (task == 7) {
				et.put("0", 5d); et.put("1", 8d); et.put("2", 11d);
			} else if (task == 8) {
				et.put("0", 3d); et.put("1", 6d); et.put("2", 8d);
			} else if (task == 9) {
				et.put("0", 5d); et.put("1", 8d); et.put("2", 14d);
			} else if (task == 10) {
				et.put("0", 0d); et.put("1", 0d); et.put("2", 0d);
			}
			execTime.put(task, et);
		}




		/*for (int i = 0; i < 3; i++) {
			HashMap<Integer, Double> et = new HashMap<Integer, Double>();
			if (i == 0) {
				et.put(0, 0d); et.put(1, 2d); et.put(2, 5d); et.put(3, 3d); et.put(4, 4d); et.put(5, 3d); et.put(6, 4d); et.put(7, 5d); et.put(8, 3d); et.put(9, 5d); et.put(10, 0d);
			} else if (i == 1) {
				et.put(0, 0d); et.put(1, 5d); et.put(2, 12d); et.put(3, 5d); et.put(4, 6d); et.put(5, 8d); et.put(6, 8d); et.put(7, 8d); et.put(8, 6d); et.put(9, 8d); et.put(10, 0d);
			} else {
				et.put(0, 0d); et.put(1, 8d); et.put(2, 16d); et.put(3, 9d); et.put(4, 10d); et.put(5, 11d); et.put(6, 11d); et.put(7, 11d); et.put(8, 8d); et.put(9, 14d); et.put(10, 0d);
			}

			/*if (i == 0) {
				et.put(0, 0d); et.put(1, 2d); et.put(2, 5d); et.put(3, 3d); et.put(4, 4d); et.put(5, 3d); et.put(6, 4d); et.put(7, 5d); et.put(8, 3d); et.put(9, 5d); et.put(10, 0d);
			} else if (i == 1) {
				et.put(0, 0d); et.put(1, 5d); et.put(2, 12d); et.put(3, 5d); et.put(4, 6d); et.put(5, 8d); et.put(6, 8d); et.put(7, 8d); et.put(8, 6d); et.put(9, 8d); et.put(10, 0d);
			} else {
				et.put(0, 0d); et.put(1, 8d); et.put(2, 16d); et.put(3, 9d); et.put(4, 10d); et.put(5, 11d); et.put(6, 11d); et.put(7, 11d); et.put(8, 8d); et.put(9, 14d); et.put(10, 0d);
			}*/

		/*execTime.put(i + "", et);
		}*/

		/* this variable keeps track about visiting*/
		isVisited = new boolean[totalTasks];


		//TODO/*These are hardcoded confidential tasks*/
		//		confidentialTasks.add(-1);

		/*TaskAllocation to vm mapping initialization
		 */

		taskAllocationMap = new int[totalTasks];
		Arrays.fill(taskAllocationMap, -1);
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
