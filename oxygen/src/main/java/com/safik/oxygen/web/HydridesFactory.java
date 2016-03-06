package com.safik.oxygen.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
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

	private Class comp;
	private AbsractHydridesFactory data;
	private static Map<String, HydridesFactory> map = new HashMap<String, HydridesFactory>();
	private static URL urlHydride = null;

	private HydridesFactory(Class comp, URL stream) throws Exception {
		this.comp = comp;
		Constructor<?> ctor = comp.getConstructor(URL.class);
		Object object = ctor.newInstance(new Object[] { stream });
		this.data = (AbsractHydridesFactory) object;
	}

	public static HydridesFactory getInstance(Class cls) throws Exception {

		return getInstance(cls, null);
	}

	public static HydridesFactory getInstance(Class cls, URL url) throws Exception {
		if (url != null) {
			map.put(cls.getSimpleName().toLowerCase(), new HydridesFactory(cls, url));
		}
		return map.get(cls.getSimpleName().toLowerCase());
	}

	public Map getData() {
		return data.getData();
	}

	

}
