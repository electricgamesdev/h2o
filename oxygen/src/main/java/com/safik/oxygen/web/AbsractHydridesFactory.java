package com.safik.oxygen.web;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;

public abstract class AbsractHydridesFactory {

	public AbsractHydridesFactory(InputStream inputStream) {
		
	}
	
	public abstract Map getData();
	
	
	protected Map<String, String> populateAttr(XMLStreamReader reader) throws Exception {
		return populateAttr(reader, false);
	}

	protected Map<String, String> populateAttr(XMLStreamReader reader, boolean addseqtokey) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		if (reader.getAttributeCount() > 0) {
			for (int i = 0; i < reader.getAttributeCount(); i++) {
				if (addseqtokey)
					map.put(reader.getAttributeName(i).toString() + "." + i, reader.getAttributeValue(i));
				else
					map.put(reader.getAttributeName(i).toString(), reader.getAttributeValue(i));
			}
		}
		return map;
	}


}
