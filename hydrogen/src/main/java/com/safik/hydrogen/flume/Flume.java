package com.safik.hydrogen.flume;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.safik.hydrogen.engine.Hydride;
import com.safik.hydrogen.engine.HydrideContext;
import com.safik.hydrogen.engine.Runner;
import com.safik.hydrogen.engine.Script;
import com.safik.hydrogen.engine.ScriptRunner;
import com.safik.hydrogen.ex.ScriptException;

public class Flume extends Script {

	public Runner createRunner() throws ScriptException {

		ScriptRunner r = new ScriptRunner(Flume.class);

		return r;
	}

	public static void main(String[] args) {
		Flume flumeRunner = new Flume();

		// RpcClient client =RpcClientFactory.getDefaultInstance("localhost",
		// 34545);

	}

	private String template = null;

	public Flume() {
		List<String> l = null;
		try {
			l = IOUtils.readLines(this.getClass().getResourceAsStream(
					"flume_dynamic.properties"));
			template = new String();
			for (String string : l) {
				template = template + string + "\n";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public String initialize(HydrideContext context) {
		String domain = "data_profiling";

		Map map = context.getDomainSources(domain);
		for (Object k : map.keySet()) {

			String source = (String) k;
			String conf = template.toString();
			String entities = "";
			String patterns = "";
			Map emap = context.getDomainSourceEntities(domain, source);
			for (Object e : emap.keySet()) {
				String entity = (String) e;
				String id = entity.substring(entity.lastIndexOf("/") + 1);
				conf = conf + "agent.sources.source1.selector.mapping." + id+ " = channel1 \n";
				entities=entities+id+",";
				
				Map entMap = context.getDomainSourceEntity(domain, source, entity);
				if(entMap!=null){
					patterns=patterns+entMap.get("pattern")+",";
					System.out.println("pattern = "+patterns);
				}
			}
			conf=conf+"agent.sources.source1.interceptors.i2.entities = "+entities+" \n";
			conf=conf+"agent.sources.source1.interceptors.i2.patterns = "+patterns+" \n";
			conf=conf.replaceAll("agent", source);
			Map m = context.getDomainSource(domain, source);
			conf=conf.replaceAll("source-dir",context.getValue(m,"source-dir"));
			conf=conf.replaceAll("hdfs-dir", context.getValue(m,"hdfs-dir"));
			conf=conf.replaceAll("hdfs-error-dir",context.getValue(m,"hdfs-error-dir"));

			Map<String,String> c=new HashMap<String, String>();
			c.put("scriptToExecute", source+".flume");
			c.put("source", source);
			
			addScript(source+".flume", conf,c);

		}

		return "success";
	}

}
