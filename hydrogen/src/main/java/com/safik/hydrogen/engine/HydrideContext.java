package com.safik.hydrogen.engine;

import java.util.HashMap;
import java.util.Map;

public class HydrideContext {

	Map<String,String> map=new HashMap<String, String>();
	
	protected String getValue(String key){
		return map.get(key);
	}
	
}
