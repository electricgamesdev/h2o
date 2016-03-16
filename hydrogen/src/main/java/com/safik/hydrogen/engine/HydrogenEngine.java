package com.safik.hydrogen.engine;

import java.util.Map;
import java.util.TreeMap;

public class HydrogenEngine {
	
	private static HydrogenEngine engine=null;
	
	private Map<String,Hydride> hydrides = new TreeMap<String,Hydride>();
	private HydrideConnector connector = null;
	private HydrogenEngine(HydrideConnector connector){
		this.connector=connector;
	}
	
	public static HydrogenEngine getInstance(HydrideConnector connector){
		
		if(engine==null){
			engine = new HydrogenEngine(connector);
		}
		
		
		
		return engine;
	}
	
	public void addHydrides(String name,Map hydrides,Map<String,String> pMap){
		HydrideContext context =new HydrideContext(hydrides, connector,pMap);
		Hydride hydride=new Hydride(context);
		engine.add(name,hydride);
	}

	private void add(String name, Hydride hydride) {
		hydrides.put(name, hydride);
	}
	
	public void startHydrides(){
		for (String key  : hydrides.keySet()) {
			Hydride h = hydrides.get(key);
			h.start();
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
