package com.safik.hydrogen.flume;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.safik.hydrogen.engine.Entity;
import com.safik.hydrogen.util.EventJDBCHelper;

public class HydroFlumeInterceptor implements Interceptor {

	private String source = null;
	private String regex = null;

	public HydroFlumeInterceptor(Context context) {

		source = context.getString("source");
		regex = context.getString("pattern");

	}

	public void close() {
		System.out.println("*************** closed");

	}

	List<Entity> elist = null;
	Expression exp = null;

	public void initialize() {
		elist = loadSource(source);
		ExpressionParser parser = new SpelExpressionParser();
		exp = parser.parseExpression(regex);
	}

	/**
	 * Dynamically load from hydrgen application
	 * 
	 * @param source
	 * @return
	 */
	private List<Entity> loadSource(String source) {

		List<Entity> elist = new ArrayList<Entity>();
		
		Entity e1 = new Entity();
		e1.setId("ctl");
		e1.setHeader(true);
		e1.setPattern("^(ord_?[a-z]{2}_?[a-z]{3}_?[a-z]{3}_?[a-z]{3}_?[0-9]{8}_?[0-9]{12}.ctl)$");
		e1.setSeperator("|");
		//e.setType("file");

		elist.add(e1);

		Entity e2 = new Entity();
		e2.setId("dat");
		e2.setHeader(true);
		e2.setPattern("^(ord_?[a-z]{2}_?[a-z]{3}_?[a-z]{3}_?[a-z]{3}_?[0-9]{8}_?[0-9]{12}.dat)$");
		e2.setSeperator("|");
		//e.setType("file");

		elist.add(e2);
		return elist;
	}

	public Event intercept(Event event) {

		List<Event> events = new ArrayList<Event>();

		events.add(event);

		events = intercept(events);

		if(events.size()>0){
			checkToTriggerOozie();
			return events.get(0);		
		}
		
		return null;

	}

	Set<String> keylist = new HashSet<String>();

	public List<Event> intercept(List<Event> events) {

		List<Event> evlist = process(elist, events);
		checkToTriggerOozie();
		return evlist;
	}
	
	private void checkToTriggerOozie(){
		for(String pk:keylist){
			if(EventJDBCHelper.isAllEntityAsEvent(pk,elist)!=null){
				EventJDBCHelper.insertEvent("oozie", 9999L, "not matching", "ACTION", pk, pk);
			}
		}
	}

	private synchronized List<Event> process(List<Entity> elist, List<Event> events) {
		String primary_key = null;
		List<Event> evlist = new ArrayList<Event>();

		for (Entity entity : elist) {
			for (Event event : events) {
		
				Map<String, String> map = event.getHeaders();
				String filename = map.get("basename");
				String eventId = map.get("timestamp");

				if(entity.getEventId()!=null && entity.getEventId().equalsIgnoreCase(eventId)){
					event.getHeaders().put("entity",entity.getId().toLowerCase());
					continue;
				}
				
				
				entity.setEventId(eventId);
				entity.setName(filename);
				try{
				
				
					Pattern pattern = Pattern.compile(entity.getPattern());
						Matcher matcher = pattern.matcher(filename);

						if (matcher.matches()) {
							StandardEvaluationContext fileContext = new StandardEvaluationContext(entity);
							primary_key = exp.getValue(fileContext, String.class);
							keylist.add(primary_key);
							
							event.getHeaders().put("entity",
									entity.getId().toLowerCase());
							
							EventJDBCHelper.insertEvent(
									filename,
									Long.parseLong(eventId),
							new String(event.getBody()), "INIT",source,primary_key);
						

						}
					
				
				
			}catch(Exception e){
				EventJDBCHelper.insertEvent(
						filename,
						Long.parseLong(eventId),
						"Pattern does not matching with "
								+ entity.getPattern()+" Java Error:"+e.getMessage(), "FAIL",source,primary_key);
	
			}
			}
		}
		
	
	
		return events;
	}

	public static class Builder implements Interceptor.Builder {

		private Context context;

		public void configure(Context context) {
			this.context = context;
		}

		public Interceptor build() {
			return new HydroFlumeInterceptor(context);
		}
	}

	public static void main(String[] args) {

		//EventJDBCHelper.insertEvent("test.txt", 100, "test", "INIT");

	}

}
