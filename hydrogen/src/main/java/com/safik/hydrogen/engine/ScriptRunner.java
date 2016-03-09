package com.safik.hydrogen.engine;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.safik.hydrogen.ex.ScriptException;
import com.safik.hydrogen.util.EventJDBCHelper;
import com.safik.hydrogen.util.UnixShellThread;

public final class ScriptRunner extends Thread{

	private static Map<String, ScriptRunner> sfMap = new HashMap<String, ScriptRunner>();

	String script = null;
	Properties params = null;

	Class type=null;
	String pid = null;
	private ScriptRunner( Class type)
			throws ScriptException {
		this.type=type;
		try {
			script = IOUtils.readLines(type.getResourceAsStream("script")).get(0);
			params = new Properties();

			params.load(type.getResourceAsStream("params"));
			
			pid=EventJDBCHelper.getProcess(type.getSimpleName());
			if(pid!=null){
				System.out.println("killing already running process:"+pid);
				kill();
			}
			
		} catch (Exception e) {
			throw new ScriptException(e);
		}
	}

	public static ScriptRunner getInstance(Class type)
			throws ScriptException {
		if (!sfMap.containsKey(type)) {
			ScriptRunner sf = new ScriptRunner(type);
			sfMap.put(type.getSimpleName(), sf);
		}
		return sfMap.get(type.getSimpleName());
	}

	public void run() {
		try {
			Runtime rt = Runtime.getRuntime();
			for(Object key:params.keySet())
				script = script+" "+key.toString()+" "+params.getProperty(key.toString());
			System.out.println("Starting "+script);
			Process process = rt.exec(script);
			Field f = process.getClass().getDeclaredField("pid");
		    f.setAccessible(true);
		    pid = ""+f.getInt(process);
		    System.out.println("Process id: " + pid);
		    EventJDBCHelper.insertProcess(type.getSimpleName(), Long.parseLong(pid), "RUNNING");
			UnixShellThread errorGobbler = new 
					UnixShellThread(process.getErrorStream(), type.getSimpleName()+":"+pid);  
			errorGobbler.start();
			int exitVal = process.waitFor();
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public void kill(){
		Runtime rt = Runtime.getRuntime();
		try {
			Process process = rt.exec("kill -15 "+pid);
			UnixShellThread errorGobbler = new 
					UnixShellThread(process.getErrorStream(), type.getSimpleName());  
			errorGobbler.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
