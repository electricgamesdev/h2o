package com.safik.oxygen.web;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class Hydrid extends AbsractHydridesFactory {

	private Map map = null;

	public Hydrid(InputStream input) throws Exception {
		super(input);
		map = new HashMap();
		Map<String, String> attributes = null;
		Map<String, String> domains = null;
		Map<String, String> layouts = null;

		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(input);

		while (reader.hasNext()) {
			switch (reader.next()) {
			case XMLStreamConstants.START_ELEMENT:
				if ("hydride".equals(reader.getLocalName())) {

					attributes = populateAttr(reader);

					break;
				}
				if ("domains".equals(reader.getLocalName())) {

					break;
				}
				if ("domain".equals(reader.getLocalName())) {
					domains = populateAttr(reader, true);
					break;
				}
				if ("layouts".equals(reader.getLocalName())) {

					break;
				}
				if ("layout".equals(reader.getLocalName())) {
					layouts = populateAttr(reader, true);
					break;
				}
			}

		}
		if (attributes != null) {
			map.put("attr", attributes);
			map.put("domains", domains);
			map.put("layouts", layouts);
		}

	}

	@Override
	public Map getData() {

		return map;
	}

}
