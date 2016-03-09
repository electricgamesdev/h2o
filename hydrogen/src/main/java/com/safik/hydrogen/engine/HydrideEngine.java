package com.safik.hydrogen.engine;

import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class HydrideEngine {
	
	private static HydrideEngine engine=null;
	
	private Map<String,Hydride> hydrides = new TreeMap<String,Hydride>();
	
	private HydrideEngine(){
		
	}
	
	public static HydrideEngine getInstance(String name,Map<String, Map> hydrides,HydrideConnector connector){
		
		if(engine==null){
			engine = new HydrideEngine();
		}
		
		HydrideContext context =new HydrideContext(hydrides, connector);
		Hydride hydride=new Hydride(context);
		engine.add(name,hydride);
		
		return engine;
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

}
