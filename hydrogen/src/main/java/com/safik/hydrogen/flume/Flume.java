package com.safik.hydrogen.flume;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.safik.hydrogen.db.DBHelper;
import com.safik.hydrogen.engine.GV;
import com.safik.hydrogen.engine.Hydride;
import com.safik.hydrogen.engine.HydrideContext;
import com.safik.hydrogen.engine.Runner;
import com.safik.hydrogen.engine.Script;
import com.safik.hydrogen.engine.ScriptRunner;
import com.safik.hydrogen.ex.ScriptException;
import com.safik.hydrogen.model.Entity_Detail;
import com.safik.hydrogen.model.Source_Master;

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
			l = IOUtils.readLines(this.getClass().getResourceAsStream("flume_dynamic.properties"));
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

			Source_Master master = new Source_Master();
			Set<Entity_Detail> details = new HashSet<Entity_Detail>();

			String source = (String) k;
			master.setSource_id(context.getIdInPath(source));
			String conf = template.toString();
			String entities = "";
			String patterns = "";
			Map emap = context.getDomainSourceEntities(domain, source);
			for (Object e : emap.keySet()) {
				String entity = (String) e;
				String id = context.getIdInPath(entity);
				conf = conf + "agent.sources.source1.selector.mapping." + id + " = channel1 \n";
				entities = entities + id + ",";

				Map entMap = context.getDomainSourceEntity(domain, source, entity);

				patterns = patterns + context.getValue(entMap, "pattern") + ",";

				Entity_Detail detail = new Entity_Detail();
				detail.setEntity_id(id);
				detail.setPattern(context.getValue(entMap, "pattern"));
				detail.setHeader(Boolean.parseBoolean(context.getValue(entMap, "pattern")));
				// TODO
				
				detail.setSource_Masters(Arrays.asList(master));
				details.add(detail);
			}

			master.setEntity_Details(details);

			conf = conf + "agent.sources.source1.interceptors.i2.entities = " + entities + " \n";
			conf = conf + "agent.sources.source1.interceptors.i2.patterns = " + patterns + " \n";
			conf = conf.replaceAll("agent", source);
			Map m = context.getDomainSource(domain, source);
			String hdfsdatadir = context.getGV(GV.HDFS_HOME_DATA).toString();
			String hdfserrordir = context.getGV(GV.HDFS_HOME_ERROR).toString();
			conf = conf.replaceAll("source-dir", context.getValue(m, "source-dir"));
			conf = conf.replaceAll("hdfs-dir", hdfsdatadir);
			conf = conf.replaceAll("hdfs-error-dir", hdfserrordir);
			conf = conf.replaceAll("sourcefilter", context.getValue(m, "dataset-filter"));

			master.setSource_dir(context.getValue(m, "source-dir"));
			master.setHdfs_path(hdfsdatadir);
			master.setHdfs_error_path(hdfserrordir);
			master.setDataset_filter(context.getValue(m, "dataset-filter"));

			// clean up - fresh build
			try {
				File sourceDir = new File(context.getValue(m, "source-dir"));
				File data1 = new File(sourceDir.getParent() + File.separator + source + "_data1");
				File data2 = new File(sourceDir.getParent() + File.separator + source + "_data2");
				File cp1 = new File(sourceDir.getParent() + File.separator + source + "_cp1");
				File cp2 = new File(sourceDir.getParent() + File.separator + source + "_cp2");

				if (sourceDir.exists())
					FileUtils.forceDelete(sourceDir);
				if (data1.exists())
					FileUtils.forceDelete(data1);
				if (data2.exists())
					FileUtils.forceDelete(data2);
				if (cp1.exists())
					FileUtils.forceDelete(cp1);
				if (cp2.exists())
					FileUtils.forceDelete(cp2);

				FileUtils.forceMkdir(sourceDir);
				FileUtils.forceMkdir(data1);
				FileUtils.forceMkdir(data2);
				FileUtils.forceMkdir(cp1);
				FileUtils.forceMkdir(cp2);

				conf = conf.replaceAll("data1", data1.getAbsolutePath());
				conf = conf.replaceAll("data2", data2.getAbsolutePath());
				conf = conf.replaceAll("checkpoint1", cp1.getAbsolutePath());
				conf = conf.replaceAll("checkpoint2", cp2.getAbsolutePath());

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			File home = (File) context.getGV(GV.HOME);
			Map<String, String> c = new HashMap<String, String>();
			c.put("scriptToExecute", home.getAbsolutePath() + File.separator + source + ".flume");
			c.put("source", source);

			addScript(source + ".flume", conf, c);
			master.setFlume_status("CREATED");
			DBHelper.merge(master);
		}

		return "success";
	}

}
