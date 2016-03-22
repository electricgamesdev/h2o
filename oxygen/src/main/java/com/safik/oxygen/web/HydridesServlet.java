package com.safik.oxygen.web;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.safik.hydrogen.engine.HydrideConnector;
import com.safik.hydrogen.engine.HydrideContext;
import com.safik.hydrogen.engine.HydrogenEngine;
import com.safik.hydrogen.oozie.CopyOfOozie;

public class HydridesServlet extends HttpServlet implements HydrideConnector {
	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());
	private HydrogenEngine hydrogenEngine = null;

	@Override
	public final void init() throws ServletException {
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing servlet '" + getServletName() + "'");
		}

		// CopyOfOozie o=new CopyOfOozie();
		// o.main(null);

		hydrogenEngine = HydrogenEngine.getInstance(this);
		String input = getServletConfig().getInitParameter("hydrides");

		URL url = getServletConfig().getServletContext().getClassLoader().getResource(input);

		if (url != null) {
			try {

				Map<String, String> pMap = new HashMap<String, String>();
				String str = getServletConfig().getServletContext().getContextPath();
				String strw = getServletConfig().getServletContext().getRealPath("core-site.xml");

				pMap.put("core-site.xml", getServletConfig().getServletContext().getRealPath("WEB-INF/core-site.xml").toString());
				pMap.put("hdfs-site.xml", getServletConfig().getServletContext().getRealPath("WEB-INF/hdfs-site.xml").toString());
				pMap.put("mapred-site.xml", getServletConfig().getServletContext().getRealPath("WEB-INF/mapred-site.xml").toString());
				pMap.put("yarn-site.xml", getServletConfig().getServletContext().getRealPath("WEB-INF/yarn-site.xml").toString());

				// stage 1 : Loading hydrides
				Map<String, Map> hydrides = HydrogenFactory.getInstance(HydrideFactory.class, url).getData();

				hydrogenEngine.addHydrides("test", hydrides, pMap);
				hydrogenEngine.initHydrides();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Servlet '" + getServletName() + "' configured successfully");
		}
	}

	@Override
	public void destroy() {

		if (logger.isDebugEnabled()) {
			logger.debug("Servlet '" + getServletName() + "' configured successfully");
		}
	}

	private void test(Map<String, Map> hydrides) {
		System.out.println("printing hydride:" + printKeyValue("hydrides", hydrides));

		// stage 3 : Loading fundamental comps - implementation check
		HydrideContext context = new HydrideContext(hydrides, this, null);

		System.out.println(printKeyValue("domain", context.getDomain("data_profiling")));
		System.out.println(printKeyValue("layout", context.getLayout("data_profiling")));

		System.out.println(printKeyValue("form", context.getDomain("data_profiling")));
		System.out.println(printKeyValue("layout", context.getLayout("data_profiling")));
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

	/**
	 * Overridden method that simply returns {@code null} when no ServletConfig
	 * set yet.
	 * 
	 * @see #getServletConfig()
	 */
	@Override
	public final String getServletName() {
		return (getServletConfig() != null ? getServletConfig().getServletName() : null);
	}

	/**
	 * Overridden method that simply returns {@code null} when no ServletConfig
	 * set yet.
	 * 
	 * @see #getServletConfig()
	 */
	@Override
	public final ServletContext getServletContext() {
		return (getServletConfig() != null ? getServletConfig().getServletContext() : null);
	}

	@Override
	public Map getData(URL url) throws Exception {
		Map<String, Map> map = null;
		if (url.toString().endsWith(".domain.xml")) {
			map = HydrogenFactory.getInstance(DomainFactory.class, url).getData();
		} else if (url.toString().endsWith(".layout.xml")) {
			map = HydrogenFactory.getInstance(LayoutFactory.class, url).getData();
		} else if (url.toString().endsWith(".entity.xml")) {
			map = HydrogenFactory.getInstance(EntityFactory.class, url).getData();
			System.out.println(printKeyValue("entity", map));
		} else if (url.toString().endsWith(".form.xml")) {
			map = HydrogenFactory.getInstance(FormFoctory.class, url).getData();
			System.out.println(printKeyValue("form", map));
		} else if (url.toString().endsWith(".action.xml")) {
			map = HydrogenFactory.getInstance(ActionFactory.class, url).getData();
			System.out.println(printKeyValue("action", map));
		}

		return map;
	}

}
