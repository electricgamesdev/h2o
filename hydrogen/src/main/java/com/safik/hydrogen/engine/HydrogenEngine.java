package com.safik.hydrogen.engine;

import java.util.Map;
import java.util.TreeMap;

import com.safik.hydrogen.db.DBHelper;

public class HydrogenEngine {
	
	private static HydrogenEngine engine=null;
	
	private Map<String,Hydride> hydrides = new TreeMap<String,Hydride>();
	private HydrideConnector connector = null;
	private HydrogenEngine(HydrideConnector connector){
		this.connector=connector;
	 
	 HydrogenEngine.initialize();	
	 ScriptRunner.initialize();
	 DBHelper.initialize();
	}
	
	public static void initialize(){
		//TODO setup hadoop env
	}
	
	public static HydrogenEngine getInstance(HydrideConnector connector){
		
		if(engine==null){
			engine = new HydrogenEngine(connector);
		}
		
		
		
		return engine;
	}
	
	public void addHydrides(String name,Map hydrides,Map<String,String> pMap){
		HydrideContext context =new HydrideContext(hydrides, connector,pMap);
		Hydride hydride=new Hydride(name,context);
		engine.add(name,hydride);
	}

	private void add(String name, Hydride hydride) {
		hydrides.put(name, hydride);
	}
	
	public void startHydrides(){
		for (String key  : hydrides.keySet()) {
			Hydride h = hydrides.get(key);
			Thread t=new Thread(h);
			t.start();
		}
	}

	public void initHydrides() {
		for (String key  : hydrides.keySet()) {
			Hydride h = hydrides.get(key);
			h.inialize();
		}
		
	}

	public void shutdown() {
		for (String key  : hydrides.keySet()) {
			Hydride h = hydrides.get(key);
			h.shutdown();
		}
		
	}

}
