package com.safik.oxygen.web;

import java.net.URL;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class ActionFactory extends AbsractHydridesFactory {

	public ActionFactory(URL url) throws Exception {
		super(url);
	}
	
	protected void process(Map<String, Map> map,XMLStreamReader reader) throws Exception{
		Map tempMap = null;
		Map filterMap = null;
		Map frmMap = null;
		while (reader.hasNext()) {
			switch (reader.next()) {
			case XMLStreamConstants.START_ELEMENT:
				if ("action".equals(reader.getLocalName())) {
					parseAttr(map,reader);
					break;
				}
				if ("rule".equals(reader.getLocalName())) {
					parseAttr(filterMap, reader);
					break;
				}
			}

		}

	}
	
	

}
