package com.safik.hydrogen.flume;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.apache.oozie.client.OozieClient;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.safik.hydrogen.db.DBHelper;
import com.safik.hydrogen.engine.Entity;
import com.safik.hydrogen.engine.Stages;
import com.safik.hydrogen.engine.Status;
import com.safik.hydrogen.engine.Tasks;
import com.safik.hydrogen.model.Entity_Detail;
import com.safik.hydrogen.model.Source_Detail;
import com.safik.hydrogen.model.Source_Master;
import com.safik.hydrogen.model.Task_Detail;
import com.safik.hydrogen.oozie.Oozie;
import com.safik.hydrogen.util.EventJDBCHelper;

public class HydroFlumeInterceptor implements Interceptor {

	Log log = LogFactory.getLog(HydroFlumeInterceptor.class);

	private Source_Master master = null;
	private String home = null;
	private Set<Entity_Detail> entitylist = null;
	
	public HydroFlumeInterceptor(Context context, Source_Master master,Set<Entity_Detail> entitylist) {
		home = System.getProperty("user.home");
		this.entitylist=entitylist;
		this.master = master;
	}

	public void close() {
		System.out.println("*************** closed");

	}

	private Expression exp = null;
	

	public void initialize() {
		log.info("Init----- " + master.getSource_id() + " entities " + master.getEntity_Details());
		
		for (Entity_Detail entity : entitylist) {
			log.info("Init----- " + entity.getEntity_id() + " entity pattern = " + entity.getPattern());
		}
		ExpressionParser parser = new SpelExpressionParser();
		exp = parser.parseExpression(master.getDataset_filter());
	}

	public Event intercept(Event event) {

		List<Event> events = new ArrayList<Event>();

		events.add(event);

		List<Source_Detail> evlist = process(events);
		checkToTriggerOozie(evlist);

		return event;

	}

	Set<String> keylist = new HashSet<String>();

	public List<Event> intercept(List<Event> events) {

		List<Source_Detail> evlist = process(events);
		checkToTriggerOozie(evlist);
		return events;
	}

	private void checkToTriggerOozie(List<Source_Detail> evlist) {
		String jobId = null;
		for (String pk : keylist) {
			
			List<Source_Detail> edlist = EventJDBCHelper.isAllEntityAsEvent(pk, master);
			if (edlist != null) {
				try {
					log.info("Ooziee configuration : "+edlist);
					Properties p = new Properties();
					p.load(new FileReader(home + File.separator + "hydrogen" + File.separator + master.getSource_id() + ".properties"));
					for (Source_Detail entity : edlist) {
						p.setProperty(entity.getEntity_id(), entity.getFlume_header());
					}
					OozieClient wc = new OozieClient(p.getProperty("oozie.url"));

					log.info("Ooziee configuration : "+p);
					// create a workflow job configuration and set the workflow
					// application path
					Properties conf = wc.createConfiguration();
					for (Object key : p.keySet()) {
						conf.put(key, p.getProperty(key.toString()));
					}

					// submit and start the workflow job
					jobId = wc.run(conf);

					for (Source_Detail detail : edlist) {
						detail.setStage(Stages.COORDINATOR.name());
						detail.setStatus(Status.RUNNING.name());
						Task_Detail t = new Task_Detail();
						t.setEvent_name(Tasks.DATASET_SCAN.name());
						t.setEvent_description(jobId);
						t.setStart_time(DBHelper.ts());
						t.setCreated_time(DBHelper.ts());
						t.setUpdated_time(DBHelper.ts());
						t.setEvent_status(Status.RUNNING.name());
						t.setSource_Detail(detail);
						detail.setTask_Details(Arrays.asList(t));

						DBHelper.merge(detail);
					}

				} catch (Exception e) {
					for (Source_Detail detail : edlist) {
						detail.setStage(Stages.COORDINATOR.name());
						detail.setStatus(Status.ERROR.name());
						Task_Detail t = new Task_Detail();
						t.setEvent_name(Tasks.DATASET_SCAN.name());
						t.setStart_time(DBHelper.ts());
						t.setCreated_time(DBHelper.ts());
						t.setUpdated_time(DBHelper.ts());
						t.setEvent_status(Status.ERROR.name());
						t.setEvent_description(e.getMessage());
						t.setSource_Detail(detail);
						detail.setTask_Details(Arrays.asList(t));

						DBHelper.merge(detail);
					}

					e.printStackTrace();
				}
			}
		}
	}

	private synchronized List<Source_Detail> process(List<Event> events) {
		String primary_key = null;
		List<Source_Detail> evlist = new ArrayList<Source_Detail>();
		for (Event event : events) {

			Map<String, String> map = event.getHeaders();
			String filename = map.get("basename");
			String eventId = map.get("timestamp");

			log.info("Init----- event header : " + map);
			log.info("Init----- content : " + event.getBody());
			log.info("Init----- entitylist : " + entitylist);

			Source_Detail detail = new Source_Detail();
			detail.setFlume_unique_id(eventId);
			detail.setSource_header(filename);
			detail.setStage(master.getEntity_Details().size() + "");// Stages.FLUME.name());
			detail.setStatus(Status.INVALID.name());
			detail.setSource_Master(master);
			for (Entity_Detail entity : entitylist) {

				detail.setEntity_id(entity.getEntity_id());

				/*
				 * if (evlist.contains(detail)) {
				 * event.getHeaders().put("entity",
				 * entity.getEntity_id().toLowerCase());
				 * log.info("Init----- event header : "+map); continue; }
				 */

				try {

					Pattern pattern = Pattern.compile(entity.getPattern());
					Matcher matcher = pattern.matcher(filename);

					log.info("Init----- header check : " + entity.getPattern() + " --> " + filename);

					if (matcher.matches()) {
						StandardEvaluationContext fileContext = new StandardEvaluationContext(detail);
						primary_key = exp.getValue(fileContext, String.class);
						keylist.add(primary_key);
						event.getHeaders().put("entity", entity.getEntity_id().toLowerCase());

						log.info("Init----- header matched : " + primary_key);

						detail.setEntity_pattern(entity.getPattern());
						detail.setDataset_filter_value(primary_key);
						detail.setStage(Stages.FLUME.name());
						detail.setStatus(Status.INIT.name());

						Task_Detail t = new Task_Detail();
						t.setEvent_name(Tasks.ENTITY_PATTERN_MATCH.name());
						t.setStart_time(DBHelper.ts());
						t.setEnd_time(DBHelper.ts());
						t.setCreated_time(DBHelper.ts());
						t.setUpdated_time(DBHelper.ts());
						t.setEvent_status(Status.SUCCESS.name());
						t.setSource_Detail(detail);
						detail.setTask_Details(Arrays.asList(t));
						DBHelper.persist(detail);
					}

				} catch (Exception e) {
					e.printStackTrace();
					detail.setSource_header(e.getMessage());
					detail.setStatus(Status.ERROR.name());
					DBHelper.persist(detail);

				}
			}

			//DBHelper.persist(detail);

		}

		return evlist;
	}

	public static class Builder implements Interceptor.Builder {

		private Context context;
		private Source_Master master = null;
		private Set<Entity_Detail> entitylist = null;
		
		public void configure(Context context) {
			this.context = context;
			String source = context.getString("source");
			entitylist = new HashSet<Entity_Detail>();
			master = (Source_Master) DBHelper.findByKey(Source_Master.class, source);
			for (Entity_Detail entity : master.getEntity_Details()) {
				System.out.println("id="+entity.getEntity_id());
				entitylist.add(entity);
			}
			master.setFlume_status("RUNNING");
			DBHelper.merge(master);
		}

		public Interceptor build() {
			return new HydroFlumeInterceptor(context, master,entitylist);
		}
	}

	public static void main(String[] args) {

		// EventJDBCHelper.insertEvent("test.txt", 100, "test", "INIT");

	}

}
