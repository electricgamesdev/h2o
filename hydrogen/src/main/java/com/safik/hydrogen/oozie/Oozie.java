package com.safik.hydrogen.oozie;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.oozie.client.CoordinatorJob;
import org.apache.oozie.client.Job.Status;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.WorkflowJob;

import com.safik.hydrogen.engine.HydrideContext;
import com.safik.hydrogen.engine.Runner;
import com.safik.hydrogen.engine.Script;
import com.safik.hydrogen.ex.ScriptException;
import com.safik.hydrogen.model.Task_Detail;
import com.safik.hydrogen.util.EventJDBCHelper;

public class Oozie extends Script implements Runner {

	public Runner createRunner() throws ScriptException {

		return this;
	}

	public Oozie() {

	}

	private String oozieurl = null;

	@Override
	public void run() {

		OozieClient wc = new OozieClient(oozieurl);
		List<Task_Detail> cset = EventJDBCHelper.getCoordinatorJob();
		List<Task_Detail> wset = EventJDBCHelper.getWorkflowJob();
		System.out.println("Oozie Coordinator Running : " + cset.size());
		System.out.println("Oozie Workflow Running : " + wset.size());

		try {
			for (Task_Detail td : cset) {
				CoordinatorJob co = wc.getCoordJobInfo(td.getEvent_description());
				if (co.getStatus() != Status.RUNNING) {
					EventJDBCHelper.updateOozieTask(td, co.getStatus().name());
				}
			}

			for (Task_Detail td : wset) {
				WorkflowJob wf = wc.getJobInfo(td.getEvent_description());
				if (wf.getStatus() != WorkflowJob.Status.RUNNING) {
					EventJDBCHelper.updateOozieTask(td, wf.getStatus().name());
				}
			}

		} catch (OozieClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Configuration conf = null;
	private HydrideContext context = null;
	private String co = null;
	private String fc = null;
	private String source = null;

	public String initialize(HydrideContext context) {
		this.context = context;
		String domain = "data_profiling";

		Map sm = null;
		try {
			StringBuilder ds = new StringBuilder();
			StringBuilder ie = new StringBuilder();
			StringBuilder fs = new StringBuilder();

			Map map = context.getDomainSources(domain);
			for (Object k : map.keySet()) {

				source = (String) k;
				sm = context.getDomainSource(domain, source);

				String mv = "<move source=\"${nameNode}" + context.getValue(sm, "hdfs-dir") + "/${#1}'\" target=\"${nameNode}"
						+ context.getValue(sm, "hdfs-dir") + "/${wf:id()}/${#1}\"/>";

				Map emap = context.getDomainSourceEntities(domain, source);
				for (Object e : emap.keySet()) {
					String entity = (String) e;
					String id = entity.substring(entity.lastIndexOf("/") + 1);

					String s = IOUtils.toString(Oozie.class.getResourceAsStream("ds.xml"));
					s = StringUtils.replace(s, "#1", id);
					ds.append(StringUtils.replace(s, "#2", id));

					String i = IOUtils.toString(Oozie.class.getResourceAsStream("ie.xml"));
					i = StringUtils.replace(i, "#1", id + "IN");
					ie.append(StringUtils.replace(i, "#2", id));

					fs.append(StringUtils.replace(i, "#1", id));
					mv = StringUtils.replace(mv, "#1", id);
				}

				Properties srcProp = new Properties();
				srcProp.load(Oozie.class.getResourceAsStream("co.properties"));
				srcProp.setProperty("oozie.coord.application.path", "${nameNode}/user/hydrogen/workflow/" + source + "_coordinator.xml");
				srcProp.setProperty("workflowPath", "${nameNode}/user/hydrogen/workflow/" + source + "_workflow.xml");

				StringBuilder data = new StringBuilder();
				for (Object key : srcProp.keySet()) {
					data.append(key + "=" + srcProp.getProperty(key.toString()) + "\n");
				}

				Map<String, String> c = new HashMap<String, String>();

				addScript(source + ".properties", data.toString(), c);

				System.out.println("fc=" + fc);

				co = IOUtils.toString(Oozie.class.getResourceAsStream("co.xml"));
				co = StringUtils.replace(co, "#1", ds.toString());
				co = StringUtils.replace(co, "#2", ie.toString());
				co = StringUtils.replace(co, "coordinator1", source + "_coordinator");
				System.out.println("co=" + co);
				fc = IOUtils.toString(Oozie.class.getResourceAsStream("fc.xml"));
				fc = StringUtils.replace(fc, "#1", "dpf");
				fc = StringUtils.replace(fc, "#2", mv);
				fc = StringUtils.replace(fc, "hydridesdpf", source + "_workflow");
				fc = StringUtils.replace(fc, "mvdpf", source + "_move");

				addScript(source + "_coordinator.xml", co, null);
				addScript(source + "_workflow.xml", fc, null);

				System.out.println("copying workflows ");
				addDFScript(source + "_coordinator.xml", co, null);
				addDFScript(source + "_workflow.xml", fc, null);

				System.out.println("oozie script completed ");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "success";
	}

	public Configuration getConf() {
		// TODO Auto-generated method stub
		return conf;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;

	}

	private ScheduledExecutorService scheduledPool = null;

	public void start(String name, Map<String, String> conf) {

		scheduledPool = Executors.newScheduledThreadPool(1);

		scheduledPool.scheduleAtFixedRate(this, 5, 5, TimeUnit.SECONDS);

	}

	public void kill() {
		scheduledPool.shutdown();
	}

}
