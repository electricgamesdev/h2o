package com.safik.oxygen.web;

import java.net.URL;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class HydrideFactory extends AbsractHydridesFactory {

	public HydrideFactory(URL url) throws Exception {
		super(url);
	}
	
	protected void process(Map<String, Map> map,XMLStreamReader reader) throws Exception{
		Map tempMap = null;
		while (reader.hasNext()) {
			switch (reader.next()) {
			case XMLStreamConstants.START_ELEMENT:
				if ("hydride".equals(reader.getLocalName())) {
					parseAttr(map,reader);
					break;
				}
				if ("domains".equals(reader.getLocalName())) {
					tempMap = createChild(map,reader);
					break;
				}
				if ("domain".equals(reader.getLocalName())) {
					parseAttr(tempMap, reader);
					break;
				}
				if ("layouts".equals(reader.getLocalName())) {
					tempMap = createChild(map,reader);
					break;
				}
				if ("layout".equals(reader.getLocalName())) {
					parseAttr(tempMap, reader);
					break;
				}
			}

		}

	}
	
	

}
