package dataview.workflowexecutors;

import java.sql.SQLException;

public class TaskExecutor_BetaCaller {
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		TaskExecutor_Beta msgserver = new TaskExecutor_Beta();
		msgserver.run();
	}
}
