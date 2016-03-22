package com.safik.oxygen.web;

import java.net.URL;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class DomainFactory extends AbsractHydridesFactory {

	public DomainFactory(URL url) throws Exception {
		super(url);
	}
	
	protected void process(Map<String, Map> map,XMLStreamReader reader) throws Exception{
		Map tempMap = null,wfMap=null,swMap=null,smap=null;
		while (reader.hasNext()) {
			switch (reader.next()) {
			case XMLStreamConstants.START_ELEMENT:
				if ("domain".equals(reader.getLocalName())) {
					parseAttr(map,reader);
					break;
				}
				if ("source-watcher".equals(reader.getLocalName())) {
					swMap = createChild(map,reader);
					break;
				}
				if ("source".equals(reader.getLocalName())) {
					//tempMap = createChild(map,reader);
					smap=parseAttr(swMap, reader);
					break;
				}
				if ("entity".equals(reader.getLocalName())) {
					Map tempMap1 = createChild(smap,reader);
					parseAttr(tempMap1, reader);
					break;
				}
				if ("workflow".equals(reader.getLocalName())) {
					tempMap = createChild(map,reader);
					wfMap = parseAttr(tempMap, reader);
					break;
				}
				if ("action".equals(reader.getLocalName())) {
					tempMap = createChild(wfMap,reader);
					tempMap = parseAttr(tempMap, reader);
					break;
				}
			}

		}

	}
	
	

}
