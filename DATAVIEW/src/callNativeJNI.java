
import java.io.File;
import java.util.*;

/**
 *  This is the callNativeJNI class to load necessary DLL files for local GPU execution
 * @author Junwen Liu
 * */

public class callNativeJNI{
	private String fileLocation;
	private String DLLRepo;
	public native String simpleNN(String input);
	
	public callNativeJNI(String filelocation, String dLLRepo) {
		super();
		DLLRepo = dLLRepo;
		fileLocation = filelocation;
		
		System.load(fileLocation + "jsoncpp.dll");
		System.load(fileLocation + DLLRepo);

	}
	
}

