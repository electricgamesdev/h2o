package com.safik.oxygen.web;

import java.net.URL;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class LayoutFactory extends AbsractHydridesFactory {

	public LayoutFactory(URL url) throws Exception {
		super(url);
	}
	
	protected void process(Map<String, Map> map,XMLStreamReader reader) throws Exception{
		Map tempMap = null;
		Map filterMap = null;
		Map frmMap = null;
		while (reader.hasNext()) {
			switch (reader.next()) {
			case XMLStreamConstants.START_ELEMENT:
				if ("layout".equals(reader.getLocalName())) {
					parseAttr(map,reader);
					break;
				}
				if ("dimension".equals(reader.getLocalName())) {
					tempMap = createChild(map,reader);
					tempMap = parseAttr(tempMap,reader);
					break;
				}
				if ("filter".equals(reader.getLocalName())) {
					filterMap = createChild(tempMap,reader);
					break;
				}
				if ("field".equals(reader.getLocalName())) {
					parseAttr(filterMap, reader);
					break;
				}
				if ("forms".equals(reader.getLocalName())) {
					frmMap = createChild(tempMap,reader);
					break;
				}
				if ("form".equals(reader.getLocalName())) {
					tempMap = parseAttr(frmMap, reader);
					break;
				}
				if ("action".equals(reader.getLocalName())) {
					Map actMap=createChild(tempMap,reader);
					parseAttr(actMap, reader);
					break;
				}
			}

		}

	}
	
	

}
