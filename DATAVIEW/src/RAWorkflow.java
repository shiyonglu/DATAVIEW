import dataview.models.DATAVIEW_BigFile;
import dataview.models.Task;
import dataview.models.Workflow;

public class RAWorkflow extends Workflow{		
	public RAWorkflow()
	{
		super("RAWorkflow", "This workflow Preforms relational algebra on a dataset");	
		wins = new Object[2];
		wouts = new Object[1];
//		wins[0] = new DATAVIEW_BigFile("tables/4Col.txt");
//		wins[1] = new DATAVIEW_BigFile("tables/1Col.txt");
		//wins[1] = new DATAVIEW_BigFile("rename/1.txt");
//		wins[0] = new DATAVIEW_BigFile("division/Dividend.txt");
//		wins[0] = new DATAVIEW_BigFile("division/noSol.txt");
//		wins[1] = new DATAVIEW_BigFile("division/Divisor.txt");
//		wins[0]= new DATAVIEW_BigFile("division/3ColDividend.txt");
//		wins[1] = new DATAVIEW_BigFile("division/2ColDivisor.txt");
//		wins[0] = new DATAVIEW_BigFile("school2.txt");
//		wins[0] = new DATAVIEW_BigFile("generalEdgeCases/emptyTable.txt");
//		wins[0] = new DATAVIEW_BigFile("tables/5Col.txt");
//		wins[0] = new DATAVIEW_BigFile("tables/2Col.txt");
//		wins[0] = new DATAVIEW_BigFile("tables/4Col2.txt");
//		wins[1] = new DATAVIEW_BigFile("tables/4Col.txt");
//		wins[2] = new DATAVIEW_BigFile("conditionalJoin/case2.txt");
//		wins[1] = new DATAVIEW_BigFile("tables/4Col2.txt");
		wins[0] = new DATAVIEW_BigFile("prof.txt");
		wins[1] = new DATAVIEW_BigFile("profCopy.txt");
	//	wins[2] = new DATAVIEW_BigFile("conditionalJoinInput3.txt");
		
		//wins[1] = new String("profname==\"Lu\"");
		//wins[1] = new DATAVIEW_BigFile("selectByName.txt");
		//wins[1] = new DATAVIEW_BigFile("rename.txt");
		
//		wins[1] = new DATAVIEW_BigFile("project/project5Cols.txt");
//		wins[1] = new DATAVIEW_BigFile("rename/1.txt");
//		wins[1] = new DATAVIEW_BigFile("rename/noColon.txt");
//		wins[1] = new DATAVIEW_BigFile("rename/wrongName.txt");
//		wins[1] = new DATAVIEW_BigFile("rename/emptyName.txt"); 
//		wins[0] = new DATAVIEW_BigFile("cartesianProduct/1Col.txt");
//		wins[0] = new DATAVIEW_BigFile("cartesianProduct/2Col.txt");
//		wins[1] = new DATAVIEW_BigFile("cartesianProduct/4Col.txt");
//		wins[1] = new DATAVIEW_BigFile("rename/5Col.txt");
//		wins[0] = new DATAVIEW_BigFile("school2.txt");
//		wins[1] = new DATAVIEW_BigFile("school3.txt");
//		wins[0] = new DATAVIEW_BigFile("Table.xlsx");
//		wins[1] = new DATAVIEW_BigFile("select/noParen.txt");
//		wins[1] = new DATAVIEW_BigFile("select/noParen.txt");
//		wins[1] = new DATAVIEW_BigFile("select/nonDouble.txt");
//		wins[1] = new DATAVIEW_BigFile("naturalJoinSchool.txt");
//		wins[1] = new DATAVIEW_BigFile("working.txt");
//		wins[1] = new DATAVIEW_BigFile("workandclass.txt");
//		wins[2] = new DATAVIEW_BigFile("RAConditionalJoin.txt");
//		wins[2] = new DATAVIEW_BigFile("RAEquijoin.txt");
//		wins[1] = new DATAVIEW_BigFile("rename.txt");
//		wins[0] = new DATAVIEW_BigFile("school3.txt");
//		wins[1] = new DATAVIEW_BigFile("school4.txt");
//		wins[0] = new DATAVIEW_BigFile("tables/school.txt");
//		wins[1] = new DATAVIEW_BigFile("tables/4Col.txt");
//		wins[2] = new DATAVIEW_BigFile("conditionalJoin/case1.txt");
		
//		wins[0] = new DATAVIEW_BigFile("tables/taught.txt");
//		wins[1] = new DATAVIEW_BigFile("tables/taught.txt");
//		wins[2] = new DATAVIEW_BigFile("project/ssn.txt");
//		wins[3] = new DATAVIEW_BigFile("select/6710.txt");
//		wins[4] = new DATAVIEW_BigFile("select/7710.txt");
		
//		wins[0] = new DATAVIEW_BigFile("tables/4Col.txt");
//		wins[1] = new DATAVIEW_BigFile("tables/4Col2.txt");
//		wins[2] = new DATAVIEW_BigFile("conditionalJoin/case1.txt");
		
//		wins[0] = new DATAVIEW_BigFile("unionDiffIntersec/4Col2.txt");
//		wins[1] = new DATAVIEW_BigFile("unionDiffIntersec/4Col3.txt");
		
//		wins[0] = new DATAVIEW_BigFile("tables/4Col.txt");
//		wins[1] = new DATAVIEW_BigFile("project/project2Cols.txt");
		
//		wins[0] = new DATAVIEW_BigFile("tables/4Col.txt");
//		wins[1] = new DATAVIEW_BigFile("tables/4Col2.txt");
//		wins[2] = new DATAVIEW_BigFile("conditionalJoin/case2.txt");
		
//		wins[0] = new DATAVIEW_BigFile("tables/4Col.txt");
//		wins[1] = new DATAVIEW_BigFile("select/case4.txt");

		
//		wins[0] = new DATAVIEW_BigFile("tables/4Col.txt");
//		wins[1] = new DATAVIEW_BigFile("tables/1Col.txt");
//		
		
		
		wouts[0] = new DATAVIEW_BigFile("RAresult.txt");
	}
	public void design()
	{  
//        create and add all the tasks
//		String currentTask = "RARename";
//		String currentTask = "RAProject";
//		String currentTask = "RACartesianProduct";
//		String currentTask = "RAUnion";
//		String currentTask = "RASelect";
//		String currentTask = "RADivision";
		String currentTask = "RASetDifference";
//		String currentTask = "RAIntersection";
		//String currentTask = "RAConditionalJoin";
//		String currentTask = "RAEquijoin";
//		String currentTask = "RATest";
//		String currentTask = "RANaturalJoin";
//		String currentTask = "Task1";
//		String currentTask = "RALeftJoin";
//		String currentTask = "RARightJoin";
//		String currentTask = "RAFullOuterJoin";
	
		Task RA = addTask(currentTask);
		/*
		addEdge(0,RA,0);
		addEdge(1,RA,1);
		addEdge(RA,0,0); */
		//Task RA = addTask("RASelect");
		//Task RA = addTask("RAProject");
		//Task RA = addTask("RARename");
		//Task RA = addTask("RANaturalJoin");
		
		addEdge(0,RA,0);
		addEdge(1,RA,1);
	//	addEdge(2,RA,2);
		addEdge(RA,0,0);
		
//		Task RAJoin = addTask(currentTask);
//		addEdge(0,RAJoin,0);
//		addEdge(1,RAJoin,1);
//		addEdge(2,RAJoin,2);
//		addEdge(RAJoin,0,0);
		
//		
//		Task project = addTask("RAProject");
//		Task select = addTask("RASelect");
//		addEdge(0,select,0);
//		addEdge(1,select,1);
//		addEdge(select,0,project,0);
//		addEdge(2,project,1);
//		addEdge(project,0,0);
		
//		Task project = addTask("RAProject");
//		Task union = addTask("RAUnion");
//		Task select1 = addTask("RASelect");
//		Task select2 = addTask("RASelect");
//		addEdge(0,select1,0);
//		addEdge(3,select1,1);
//		addEdge(select1,0,union,0);
//		addEdge(1,select2,0);
//		addEdge(4,select2,1);
//		addEdge(select2,0,union,1);
//		addEdge(union,0,0);
//		addEdge(union,0,project,0);
//		addEdge(2,project,1);
//		addEdge(project,0,0);
		
//		addEdge(0,union,0);
//		addEdge(1,union,1);
//		addEdge(union,0,project,0);
//		addEdge(2,project,1);
//		addEdge(project,0,0);
		
		
	}
}

