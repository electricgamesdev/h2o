package com.safik.oxygen.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class HydridesFactory {

	private String comp;
	private AbsractHydridesFactory data;
	private static Map<String,HydridesFactory> map=new HashMap<String,HydridesFactory>();
	
	private HydridesFactory(String comp,AbsractHydridesFactory data) {
		this.comp=comp;
		this.data=data;
	}

	public static HydridesFactory getInstance(String comp) throws Exception {
		return getInstance(comp, null);
	}
	
	public static HydridesFactory getInstance(String comp, URL url) throws Exception {
		if (url != null) {
			InputStream input = url.openStream();
			if (input.available() > 0) {
				if ("hydride".equalsIgnoreCase(comp)) {
					map.put(comp, new HydridesFactory(comp, new Hydrid(input)));
				}
			} else {
				new Exception(url + " : empty file");
			}
		}
		return map.get(comp);
	}
	
	public Map getData(){
		return data.getData();
	}
	




}
