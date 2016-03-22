package com.safik.oxygen.web;

import java.net.URL;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class FormFoctory extends AbsractHydridesFactory {

	public FormFoctory(URL url) throws Exception {
		super(url);
	}
	
	protected void process(Map<String, Map> map,XMLStreamReader reader) throws Exception{
		Map tempMap = null;
		Map filterMap = null;
		Map frmMap = null;
		while (reader.hasNext()) {
			switch (reader.next()) {
			case XMLStreamConstants.START_ELEMENT:
				if ("form".equals(reader.getLocalName())) {
					parseAttr(map,reader);
					break;
				}
				if ("filter".equals(reader.getLocalName())) {
					parseAttr(filterMap, reader);
					break;
				}
			}

		}

	}
	
	

}
