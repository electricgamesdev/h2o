package com.safik.hydrogen.engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.io.IOUtils;

import com.safik.hydrogen.ex.ScriptException;

public abstract class Script extends Thread {

	public abstract String initialize(HydrideContext context);

	public abstract Runner createRunner() throws ScriptException;

	private ConcurrentLinkedQueue<Runner> rlist = new ConcurrentLinkedQueue<Runner>();

	public ConcurrentLinkedQueue<Runner> execute() throws ScriptException {
		for (String s : sMap.keySet()) {
			Map<String,String> conf = sMap.get(s);
			Runner r = createRunner();
			r.start(s, conf);
			rlist.add(r);
		}
		return rlist;
	}

	private Map<String, Map<String,String>> sMap = new HashMap<String, Map<String,String>>();

	public void addScript(String source, String content, Map<String,String> config) {
		if (config!=null)
			sMap.put(source, config);
		
		File file = new File("hydrogen/" + source);
		System.out.println("Saving files to " + file.getAbsolutePath());
		System.out.println("Content " + content);

		try {
			FileWriter w = new FileWriter(file);
			IOUtils.write(content, w);
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void shutdown() {
		for (Runner runner : rlist) {
			runner.kill();
		}

	}

}
