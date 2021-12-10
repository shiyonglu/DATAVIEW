package dataview.models;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * This is the NNWorkflow Class that extends Workflow, with getNNWorkflowSpecification method defined to return a NNworkflow JSON specs
 * @author Junwen Liu
*/
public class NNWorkflow extends Workflow {
	
	public NNWorkflow(String workflowName, String workflowDescription)
	{
		super(workflowName, workflowDescription);
	}
	
	public NNTask addNNTask(String NNTaskName, String location){
    	NNTask  newtask = null;
    	String taskTypeName;
    	int infeature = 0;
    	int outfeature = 0;
    	
    	//handle NNTasks require parameters as input
    	if(NNTaskName.indexOf("Linear")!=-1) {
    		taskTypeName = NNTaskName.substring(0, NNTaskName.indexOf("X") - 1);
    		infeature = Integer.parseInt((String) NNTaskName.substring(NNTaskName.indexOf("X")-1, NNTaskName.indexOf("X")));
    		outfeature = Integer.parseInt((String) NNTaskName.substring(NNTaskName.indexOf("X")+1));
    	}else {
    		taskTypeName = NNTaskName;
    	}
    	
    	System.out.println("Attemp to add one task of type "+taskTypeName);
		if(new File(location + "\\" + taskTypeName + ".jar").exists()){
			location = location + taskTypeName + ".jar";
		}
		File clazzPath = new File(location);
    	URL url = null;
		try {
			url = clazzPath.toURI().toURL();
			URL[] urls = new URL[] {url};
			Thread.currentThread().setContextClassLoader(new URLClassLoader(urls,Thread.currentThread().getContextClassLoader()));
			ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
			Class<URLClassLoader> classLoaderClass = URLClassLoader.class;
			Method method = classLoaderClass.getDeclaredMethod("addURL", new Class[] { URL.class });
			method.setAccessible(true);
			method.invoke(currentClassLoader, urls);
			Class<?> taskclass = Class.forName(taskTypeName);
			if(NNTaskName.indexOf("Linear")!=-1) {
				Constructor c = taskclass.getConstructor(int.class, int.class);
				newtask = (NNTask) c.newInstance(infeature, outfeature);
				//newtask = (NNTask) taskclass.getDeclaredConstructor().newInstance(infeature, outfeature);
			}else {
				Constructor c = taskclass.getConstructor();
				newtask = (NNTask) c.newInstance();
				//newtask = (NNTask) taskclass.getDeclaredConstructor().newInstance();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}catch (InstantiationException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}catch (InvocationTargetException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}catch (NoSuchMethodException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}catch (SecurityException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}
		
		return newtask;		
    }

}




