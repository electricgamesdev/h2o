package com.safik.oxygen.web;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.safik.hydrogen.engine.HydrideConnector;
import com.safik.hydrogen.engine.HydrogenEngine;

public class HydridesTest implements HydrideConnector {

	protected final Log logger = LogFactory.getLog(getClass());
	private HydrogenEngine hydrogenEngine = null;

	public static void main(String[] args) throws Exception {
		HydridesTest test=new HydridesTest();
		test.init();

	}

	public final void init() throws Exception {

		hydrogenEngine = HydrogenEngine.getInstance(this);

		File url = new File("/home/cloudera/git/h2o2/oxygen/src/main/resources/dpf/dpf.hydride.xml");

		if (url != null) {
			try {

				Map<String, String> pMap = new HashMap<String, String>();

				pMap.put("core-site.xml", "/home/cloudera/git/h2o2/oxygen/src/main/webapp/WEB-INF/core-site.xml");
				pMap.put("hdfs-site.xml", "/home/cloudera/git/h2o2/oxygen/src/main/webapp/WEB-INF/hdfs-site.xml");
				pMap.put("mapred-site.xml", "/home/cloudera/git/h2o2/oxygen/src/main/webapp/WEB-INF/mapred-site.xml");
				pMap.put("yarn-site.xml", "/home/cloudera/git/h2o2/oxygen/src/main/webapp/WEB-INF/yarn-site.xml");

				// stage 1 : Loading hydrides
				Map<String, Map> hydrides = HydrogenFactory.getInstance(HydrideFactory.class, url.toURI().toURL()).getData();

				hydrogenEngine.addHydrides("test", hydrides, pMap);
				hydrogenEngine.initHydrides();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public Map getData(URL url) throws Exception {
		Map<String, Map> map = null;
		if (url.toString().endsWith(".domain.xml")) {
			map = HydrogenFactory.getInstance(DomainFactory.class, url).getData();
			System.out.println(printKeyValue("domain", map));
		} else if (url.toString().endsWith(".layout.xml")) {
			map = HydrogenFactory.getInstance(LayoutFactory.class, url).getData();
		} else if (url.toString().endsWith(".entity.xml")) {
			map = HydrogenFactory.getInstance(EntityFactory.class, url).getData();
			//System.out.println(printKeyValue("entity", map));
		} else if (url.toString().endsWith(".form.xml")) {
			map = HydrogenFactory.getInstance(FormFoctory.class, url).getData();
			//System.out.println(printKeyValue("form", map));
		} else if (url.toString().endsWith(".action.xml")) {
			map = HydrogenFactory.getInstance(ActionFactory.class, url).getData();
			//System.out.println(printKeyValue("action", map));
		}

		return map;
	}

	private StringBuilder printKeyValue(String elem, Map d1) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");

		for (Object k : d1.keySet()) {
			Object v = d1.get(k);
			if (v instanceof String) {
				sb.append(elem + "[" + k + "=" + v + "]\n");
			} else {
				sb.append(printKeyValue(elem + ":" + k.toString(), (Map) v));
			}
		}
		return sb;
	}
}
