package dataview.workflowexecutors;



public class MoveDataToCloud {
//	public static void getDataReady(String Location, ArrayList<String> ips) throws Exception{
//		//
//		String destination_dir = "/home/ubuntu/";
//		for (int i = 0; i < ips.size(); i++) {
//			CmdLineDriver.copyFile(Location, destination_dir, ips.get(i).trim());
//			System.out.println("#Data on " + ips.get(i) + " is ready!!");
//		}
//	}
	public static void getDataReady(String Location, String ip) throws Exception{
		//
		String destination_dir = "/home/ubuntu/";
		
			CmdLineDriver.copyFile(Location, destination_dir, ip.trim());
			System.out.println("#Data on " + ip + " is ready!!");
	
	}
}
