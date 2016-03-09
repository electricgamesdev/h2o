package com.safik.hydrogen.flume;


import com.safik.hydrogen.engine.Hydride;
import com.safik.hydrogen.engine.HydrideContext;
import com.safik.hydrogen.engine.ScriptFactory;
import com.safik.hydrogen.ex.ScriptException;
public class Flume extends Hydride{

	
	
	public Flume(HydrideContext context){
		super(context);
		try {
			ScriptFactory script= ScriptFactory.getInstance(context, this.getClass());
			script.start();
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Flume flumeRunner=new Flume(null);
		
		//RpcClient client =RpcClientFactory.getDefaultInstance("localhost", 34545);
		
	}

}
