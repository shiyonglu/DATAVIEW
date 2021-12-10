package dataview.gpuresourcemanagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.json.*;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * The GPUResourceManagement is the base class as an interface for variety GPU management
 * A particular GPUResourceManagement will implement this interface and override the execute method
 * fetchConfiguration() method will read the global configuration from config.json
 * It provide two basic methods for child classes to use: CallLocalGPUService, CallGPUClusterService
 * @author: Junwen Liu
 * */

public class GPUResourceManagement {
	private String API_Repo;
	private String specification;
	protected JSONObject configuration;

	
	public GPUResourceManagement(String specification) {
		this.specification = specification;
		this.configuration = fetchConfiguration();
	}	

	public GPUResourceManagement(String API_Repo, String specification) {
		this.API_Repo = API_Repo;
		this.specification = specification;
	}	
	
	public String execute() { 
		return "";
	}
	
	public JSONObject fetchConfiguration() {
		String configure = ""; 
		JSONObject configureJson; 
		String configRepo = System.getProperty("user.dir") + File.separator + "WebContent";
		Path path  = Paths.get(configRepo);  
		try {
			configure = new String(Files.readAllBytes(Paths.get(path+ File.separator+"config.json")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		JSONObject obj = null;
		try {
			obj = new JSONObject(configure);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return obj;
	}
	
	public String callLocalGPUService(String dll_Name) {
		String returnValue = null;
		//call native from JNI class instance, use reflection
		try {
			Class cl = Class.forName("callNativeJNI");
			Constructor c = cl.getConstructor(String.class, String.class);
			Object obj = (Object) c.newInstance(API_Repo, dll_Name);
			
			Method m1 = cl.getDeclaredMethod("simpleNN", String.class);
			
			
			returnValue = (String) m1.invoke(obj, specification);
			
		}catch (ClassNotFoundException e) { 
            e.printStackTrace(); 
        } catch (NoSuchMethodException e) { 
            e.printStackTrace(); 
        } catch (SecurityException e) { 
            e.printStackTrace(); 
        } catch (IllegalAccessException e) { 
            e.printStackTrace(); 
        } catch (IllegalArgumentException e) { 
            e.printStackTrace(); 
        } catch (InvocationTargetException e) { 
            e.printStackTrace(); 
        } catch (InstantiationException e) { 
            e.printStackTrace(); 
        }
		
		return returnValue;
	}
	
	public String callGPUClusterService(String IP, String cmd) {
		String returnValue = null;
		
		//prepare the output to format that conforms to command line
		specification = "\""+specification.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"")+"\"";

		
		JSch jsch = new JSch();
		try {
			Session session = jsch.getSession("jetson", IP);
			session.setPassword("jetson");
			
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			
			session.connect();
			
			Channel channel = session.openChannel("exec");
			InputStream in = channel.getInputStream();
			
			//clusterfile5 specifies 5-fold-cross validation
			//((ChannelExec) channel).setCommand("cd /home/jetson/Documents/dis_cudaNN && mpiexec --hostfile clusterfile ./cudaMPI");
			((ChannelExec) channel).setCommand(cmd + specification);
			((ChannelExec) channel).setErrStream(System.err);
			
			channel.connect();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			
			String line;
			
			while((line = reader.readLine()) != null){
				System.out.println(line);
			}
			
			channel.disconnect();
			session.disconnect();
			
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnValue;
	}
}
