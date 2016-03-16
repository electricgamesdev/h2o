package com.safik.hydrogen.engine;

import java.util.Map;

public interface Runner extends Runnable{

	public  void kill();

	public  void start(String name,Map<String,String> conf);
}
