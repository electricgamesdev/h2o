package com.safik.oxygen.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbsractHydridesFactory {

	Log log = LogFactory.getLog(AbsractHydridesFactory.class);

	Map<String, Map> data = new HashMap<String, Map>();

	private URL url = null;

	public AbsractHydridesFactory(URL url) throws Exception {
		this.url = url;
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(url.openStream());
		process(data, reader);
	}

	protected abstract void process(Map<String, Map> data, XMLStreamReader reader) throws Exception;

	public URL getUrl() {
		return url;
	}

	public Map<String, Map> getData() {
		return data;
	}

	protected Map<String, Object> parseAttr(Map<String, Map> map, XMLStreamReader reader) throws Exception {
		String key = "attr";
		String child = reader.getLocalName();
		Map<String, Object>  m = new HashMap<String, Object>();
		if (reader.getAttributeCount() > 0) {
			
			for (int i = 0; i < reader.getAttributeCount(); i++) {
				if ("path".equalsIgnoreCase(reader.getAttributeName(i).toString())) {
					File file = FileUtils.toFile(url);
					File parent = file.getParentFile();
					if (parent.isDirectory()) {

						File childE = new File(parent.getAbsolutePath() + File.separator + reader.getAttributeValue(i)
								+ "." + child + ".xml");
						if (childE.exists()) {
							key = reader.getAttributeValue(i);
							m.put(reader.getAttributeName(i).toString(), childE.getAbsolutePath());
						} else {
							log.error(child + " not found here :" + childE.getAbsolutePath(), new HydrideException());
						}
					} else {
						log.error("directory not found - " + parent.getAbsolutePath(), new HydrideException());
					}
				} else if ("id".equalsIgnoreCase(reader.getAttributeName(i).toString())) {
					key = reader.getAttributeValue(i);
					//m.put(reader.getAttributeName(i).toString(), reader.getAttributeValue(i));

				} else {
					m.put(reader.getAttributeName(i).toString(), reader.getAttributeValue(i));
				}
			}
			if(map!=null)
			map.put(key, m);
		}
		return m;
	}

	protected Map createChild(Map<String, Map> map, XMLStreamReader reader) {
		String string2 = reader.getLocalName();
		if (!map.containsKey(string2)) {
			Map<String, Map> child = new HashMap<String, Map>();
			map.put(string2, child);
			return child;
		} else {
			return (Map<String, Map>) map.get(string2);
		}
	}

}
