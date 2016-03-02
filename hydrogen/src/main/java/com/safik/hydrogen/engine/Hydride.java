package com.safik.hydrogen.engine;

public class Hydride extends Thread{
	
	HydrideContext context;
	
	public Hydride(HydrideContext context) {
		this.context=context;
	}

	public HydrideContext getContext() {
		return context;
	}
	
	public void setContext(HydrideContext context) {
		this.context = context;
	}

}
