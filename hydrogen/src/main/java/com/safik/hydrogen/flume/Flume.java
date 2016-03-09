package com.safik.hydrogen.flume;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.safik.hydrogen.engine.Hydride;
import com.safik.hydrogen.engine.HydrideContext;
import com.safik.hydrogen.engine.Script;
import com.safik.hydrogen.engine.ScriptRunner;
import com.safik.hydrogen.ex.ScriptException;

public class Flume extends Script {

	String template = null;

	public Flume() {

		try {
			ScriptRunner script = ScriptRunner.getInstance(this.getClass());
			script.start();
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Flume flumeRunner = new Flume();

		// RpcClient client =RpcClientFactory.getDefaultInstance("localhost",
		// 34545);

	}

	@Override
	public void init() throws Exception {
		List<String> l = IOUtils.readLines(this.getClass().getResourceAsStream("flume_dynamic.properties"));
		StringBuilder builder = new StringBuilder();
		for (String string : l) {
			builder.append(string);
		}

		template = builder.toString();
	}

	@Override
	public String create(HydrideContext context) {

		String domain = "data_profiling";
		Map map = context.getDomainSources(domain);
		for (Object k : map.keySet()) {
			String name = (String) k;
			String conf = template.toString();
			StringUtils.replace(conf, "${agent}", name);
			Map m = context.getDomainSource(domain, name);
			StringUtils.replace(conf, "${source-dir}", (String)m.get("source-dir"));
			StringUtils.replace(conf, "${hdfs-dir}", (String)m.get("hdfs-dir"));
			StringUtils.replace(conf, "${hdfs-error-dir}", (String)m.get("hdfs-error-dir"));
		}

		return null;
	}

}
