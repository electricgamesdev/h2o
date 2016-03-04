package com.safik.oxygen.web;

import java.net.URL;

public class HydrideContext {

	private URL url = null;
	private HydridesFactory factory = null; 
	
	public HydrideContext(URL url) throws Exception {
	this.url=url;
	this.factory=HydridesFactory.getInstance("hydride", url);	
	System.out.println("data ="+factory.getData());	
	}

}
