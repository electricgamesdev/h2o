package com.safik.hydrogen.engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.safik.hydrogen.ex.ScriptException;
import com.safik.hydrogen.util.EventJDBCHelper;
import com.safik.hydrogen.util.UnixShellThread;

public final class ScriptRunner implements Runner {

	String script = null;
	Properties params = null;

	Class type = null;
	String pid = null;

	public ScriptRunner(Class type) throws ScriptException {
		this.type = type;
		try {
			script = IOUtils.readLines(type.getResourceAsStream("script")).get(
					0);
			params = new Properties();

			params.load(type.getResourceAsStream("params"));

			List<Long> pidlist = EventJDBCHelper.getProcess(type.getSimpleName());
			for (Long p:pidlist) {
				System.out.println("killing already running process:" + pid);
				pid=p.toString();
				kill();
			}

		} catch (Exception e) {
			throw new ScriptException(e);
		}
	}

	public void run() {
		try {
			Runtime rt = Runtime.getRuntime();
			for (Object key : params.keySet())
				script = script + " " + key.toString() + " "
						+ params.getProperty(key.toString());

			if (map != null) {
				for (String k : this.map.keySet()) {
					script = script.replaceAll(k, this.map.get(k));
				}
			}
			Process process = rt.exec(script);
			Field f = process.getClass().getDeclaredField("pid");
			f.setAccessible(true);
			pid = "" + f.getInt(process);
			System.out.println("Process id: " + pid);
			EventJDBCHelper.insertProcess(type.getSimpleName(),
					Long.parseLong(pid), "RUNNING");
			UnixShellThread errorGobbler = new UnixShellThread(
					process.getErrorStream(), type.getSimpleName() + ":" + pid);
			errorGobbler.start();
			int exitVal = process.waitFor();

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void kill() {
		Runtime rt = Runtime.getRuntime();
		try {
			Process process = rt.exec("kill -15 " + pid);
			UnixShellThread errorGobbler = new UnixShellThread(
					process.getErrorStream(), type.getSimpleName());
			errorGobbler.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Map<String, String> map = null;

	public void start(String source,Map<String, String> map) {
		this.map = map;

		Thread th = new Thread(this);
		th.start();
	}

}
