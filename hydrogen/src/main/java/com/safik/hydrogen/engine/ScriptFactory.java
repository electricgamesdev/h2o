package com.safik.hydrogen.engine;

import java.util.Map;

public class ScriptFactory {

	private Script script=null;
	private HydrideContext context=null;
	
	private ScriptFactory(Script script,HydrideContext context){
		this.script=script;
		this.context=context;
	}
	
	public static ScriptFactory getInstance(Class cls,HydrideContext context) throws Exception{
	
		Script script = (Script)cls.newInstance();
		
		return new ScriptFactory(script,context);
	}

	public void create(){
		script.create(context);
	}
	
}
