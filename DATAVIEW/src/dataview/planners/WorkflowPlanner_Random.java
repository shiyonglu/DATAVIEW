package dataview.planners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import dataview.models.GlobalSchedule;
import dataview.models.LocalSchedule;
import dataview.models.Task;
import dataview.models.TaskSchedule;
import dataview.models.Workflow;
import dataview.models.WorkflowEdge;

public class WorkflowPlanner_Random extends WorkflowPlanner {
	private Map<Integer, List<Integer>> nodeParents = new HashMap<Integer, List<Integer>>();
	private Map<Integer, Map<Integer, Double>> edgeMap = new HashMap<Integer, Map<Integer, Double>>();

	public WorkflowPlanner_Random(Workflow w) {
		super(w);
	}

	public GlobalSchedule plan() {
		this.process(w);
		Map<Integer, List<Integer>> planned_tasks = new LinkedHashMap<Integer, List<Integer>>();
		List<Integer> planned_tasks0 = new ArrayList<Integer>();
		planned_tasks0.add(0);
		planned_tasks0.add(-1);
		planned_tasks.put(0, planned_tasks0);

		GlobalSchedule gsch = new GlobalSchedule(w);
		int totalTasks = numnode;

		// Assign those tasks connected to the input files
		List<Integer> firstlevel = new ArrayList(edgeMap.get(0).keySet());
		for (Integer tmp : firstlevel) {
			List<Integer> parents = nodeParents.get(tmp);
			if (inPlannedAllParent(planned_tasks, parents)) {
				if (planned_tasks.keySet().size() == 1) {
					List<Integer> planned_task = new ArrayList<Integer>();
					planned_task.add(tmp);
					planned_tasks.put(1, planned_task);
					totalTasks--;
				} else {

					List<Integer> planned_task = planned_tasks.get(1);
					planned_task.add(tmp);
					totalTasks--;
				}

			}

		}

		firstlevel = planned_tasks.get(1);
		List<Integer> level = new ArrayList<Integer>();
		while (totalTasks != 0) {
			for (int i = 0; i < firstlevel.size(); i++) {
				Integer current = firstlevel.get(i);
				List<Integer> childrenTasks = new ArrayList(edgeMap.get(current).keySet());
				for (Integer tmp : childrenTasks) {
					if (!inPlanned_tasks(planned_tasks, tmp)) {
						// check tmp's parent task is alreay
						List<Integer> parents = nodeParents.get(tmp);
						if (inPlannedAllParent(planned_tasks, parents)) {
							// need to check if the tmp is the child of the
							// previous
							// assigned tasks, if yes a new vm will be launched
							Integer assignedIndex = hasChildRelationshipofLaunchedVM(planned_tasks, tmp);
							if (assignedIndex == -1) {
								List<Integer> planned_task = new ArrayList<Integer>();
								planned_task.add(tmp);
								planned_tasks.put(planned_tasks.keySet().size(), planned_task);
								level.add(tmp);
								totalTasks--;

							} else {
								List<Integer> planned_task = planned_tasks.get(assignedIndex);
								planned_task.add(tmp);
								level.add(tmp);
								totalTasks--;

							}
						}

					}

				}
			}
			firstlevel = level;
			level = new ArrayList();
		}
		
		for (int i = 1; i < planned_tasks.keySet().size(); i++) {
			List<Integer> tasks = planned_tasks.get(i);
			LocalSchedule lsch = new LocalSchedule();
			lsch.setVmType("t2.micro");  // this can be changed
			for(Integer task:tasks){
				TaskSchedule tsch = w.getTaskSchedule(w.getTask(task-1));
				lsch.addTaskSchedule(tsch);
			}
			gsch.addLocalSchedule(lsch);
		}
		
		return gsch;
		
	}

	// If the retrun value is -1, means that new vm instance should be launched,
	// otherwise, return the key, whose values are assigned to a vm
	private Integer hasChildRelationshipofLaunchedVM(Map<Integer, List<Integer>> planned_tasks, Integer tmp) {
		Integer assignIndex;
		List<Integer> keyset = new ArrayList<Integer>(planned_tasks.keySet());
		List<Integer> hasParent = new ArrayList<Integer>();

		for (int i = 1; i < keyset.size(); i++) {
			List<Integer> assignedTasks = planned_tasks.get(i);
			for (Integer task : assignedTasks) {
				if (edgeMap.get(task).containsKey(tmp)) {
					hasParent.add(i);
				}
			}
		}
		hasParent.add(0);
		keyset.removeAll(hasParent);
		if (keyset.size() == 0) {
			assignIndex = -1;
		} else {
			assignIndex = keyset.get(0);
		}
		return assignIndex;
	}

	private boolean inPlannedAllParent(Map<Integer, List<Integer>> planned_tasks, List<Integer> parents) {
		boolean in = true;

		for (Integer tmp : parents) {
			if (!inPlanned_tasks(planned_tasks, tmp)) {
				in = false;
			}
		}
		return in;
	}

	private boolean inPlanned_tasks(Map<Integer, List<Integer>> planned_tasks, Integer t) {
		boolean in = false;
		for (Integer tmp : planned_tasks.keySet()) {
			List<Integer> tasks = planned_tasks.get(tmp);
			if (tasks.contains(t)) {
				in = true;
			}

		}
		return in;
	}

	public void process(Workflow w) {
		for (WorkflowEdge e : w.getEdges()) {
			if (e.srcTask == null) {
				if (edgeMap.containsKey(0)) {
					Map<Integer, Double> edge = edgeMap.get(0);
					edge.put(w.getIndexOfTask(e.destTask) + 1, 0.0);
				} else {
					Map<Integer, Double> edge = new HashMap<Integer, Double>();
					edge.put(w.getIndexOfTask(e.destTask) + 1, 0.0);
					edgeMap.put(0, edge);
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
				if (edgeMap.containsKey(w.getIndexOfTask(e.srcTask) + 1)) {
					Map<Integer, Double> edge = edgeMap.get(w.getIndexOfTask(e.srcTask) + 1);
					edge.put(w.getIndexOfTask(e.destTask) + 1, 0.0);
				} else {
					Map<Integer, Double> edge = new HashMap<Integer, Double>();
					edge.put(w.getIndexOfTask(e.destTask) + 1, 0.0);
					edgeMap.put(w.getIndexOfTask(e.srcTask) + 1, edge);
				}
				if (nodeParents.containsKey(w.getIndexOfTask(e.destTask) + 1)) {
					nodeParents.get(w.getIndexOfTask(e.destTask) + 1).add(w.getIndexOfTask(e.srcTask) + 1);
				} else {
					List<Integer> tmp = new ArrayList<Integer>();
					tmp.add(w.getIndexOfTask(e.srcTask) + 1);
					nodeParents.put(w.getIndexOfTask(e.destTask) + 1, tmp);
				}
			}
		}
	}

}
