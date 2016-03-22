package com.safik.hydrogen.engine;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.safik.hydrogen.ex.ScriptException;

public class ScriptFactory {

	private Script script = null;
	private HydrideContext context = null;

	private ScriptFactory(Script script, HydrideContext context) {
		this.script = script;
		this.context = context;
	}

	public static ScriptFactory getInstance(Class cls, HydrideContext context)
			throws Exception {

		Script script = (Script) cls.newInstance();
		script.setContext(context);
		return new ScriptFactory(script, context);
	}

	public Future<String> init(ExecutorService stage1) {

	Future<String> f= stage1.submit(new Callable<String>() {

			public String call() throws Exception {
				String scr = script.initialize(context);
				System.out.println("script=" + scr);
				return scr;
			}

		});
		return f;
	}

	public void start() throws ScriptException {
		script.execute();
	}

	public void shutdown() throws ScriptException {
		script.shutdown();
	}

}
