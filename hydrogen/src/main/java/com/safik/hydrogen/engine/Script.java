package com.safik.hydrogen.engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.safik.hydrogen.ex.ScriptException;

public abstract class Script extends Thread {
	
	Log log = LogFactory.getLog(Script.class);

	private HydrideContext context;

	public HydrideContext getContext() {
		return context;
	}

	public void setContext(HydrideContext context) {
		this.context = context;
	}

	public abstract String initialize(HydrideContext context);

	public abstract Runner createRunner() throws ScriptException;

	private ConcurrentLinkedQueue<Runner> rlist = new ConcurrentLinkedQueue<Runner>();

	public ConcurrentLinkedQueue<Runner> execute() throws ScriptException {
		for (String s : sMap.keySet()) {
			Map<String, String> conf = sMap.get(s);
			Runner r = createRunner();
			r.start(s, conf);
			rlist.add(r);
		}
		return rlist;
	}

	private Map<String, Map<String, String>> sMap = new HashMap<String, Map<String, String>>();

	public File addScript(String source, String content, Map<String, String> config) {

		File home = (File) getContext().getGV(GV.HOME);
		File file = new File(home.getAbsolutePath() + File.separator + source);

		log.info("Saving files to " + file.getAbsolutePath());
	
		try {
			FileWriter w = new FileWriter(file);
			IOUtils.write(content, w);
			w.close();
		} catch (IOException e) {
			log.error(e);
		}

		if (config != null)
			sMap.put(source, config);

		return file;
	}

	public Path addDFScript(String source, String content, Map<String, String> config) {

		FileSystem fileSystem = (FileSystem) getContext().getGV(GV.HDFS);
		Path to = (Path) getContext().getGV(GV.HDFS_HOME_SCRIPT);

		Path p = new Path(to.toString() + Path.SEPARATOR + source);
		log.info("HDFS Writing to "+p);
		try {

			FSDataOutputStream output = fileSystem.create(p);
			output.writeBytes(content);
			output.close();

		} catch (IOException e) {
			log.error(e);
		}

		if (config != null)
			sMap.put(source, config);

		return p;
	}

	public void shutdown() {
		for (Runner runner : rlist) {
			log.info("Stoping "+runner);
			runner.kill();
		}

	}

}
