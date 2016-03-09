package com.safik.oxygen.web;

import java.net.URL;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.safik.hydrogen.engine.HydrideConnector;
import com.safik.hydrogen.engine.HydrideContext;
import com.safik.hydrogen.engine.HydrogenEngine;

public class HydridesServlet extends HttpServlet implements HydrideConnector {
	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());
	private HydrogenEngine hydrogenEngine = null;

	@Override
	public final void init() throws ServletException {
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing servlet '" + getServletName() + "'");
		}

		hydrogenEngine =	 HydrogenEngine.getInstance(this);
		String input = getServletConfig().getInitParameter("hydrides");

		URL url = getServletConfig().getServletContext().getClassLoader()
				.getResource(input);

		if (url != null) {
			try {

				// stage 1 : Loading hydrides 
				Map<String, Map> hydrides = HydridesFactory.getInstance(
						Hydride.class, url).getData();
				
				hydrogenEngine.addHydrides("test", hydrides);
				hydrogenEngine.initHydrides();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Servlet '" + getServletName()
					+ "' configured successfully");
		}
	}
	
	private void test(Map<String, Map> hydrides){
		System.out.println("printing hydride:"
				+ printKeyValue("hydrides", hydrides));

		
		
		// stage 3 : Loading fundamental comps - implementation check
		HydrideContext context = new HydrideContext(hydrides, this);
		
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
		return (getServletConfig() != null ? getServletConfig()
				.getServletName() : null);
	}

	/**
	 * Overridden method that simply returns {@code null} when no ServletConfig
	 * set yet.
	 * 
	 * @see #getServletConfig()
	 */
	@Override
	public final ServletContext getServletContext() {
		return (getServletConfig() != null ? getServletConfig()
				.getServletContext() : null);
	}

	@Override
	public Map getData(URL url) throws Exception {
		Map<String, Map> map = null;
		if (url.toString().endsWith(".domain.xml")) {
			map = HydridesFactory.getInstance(Domain.class, url).getData();
			System.out.println("printing domain:"
					+ printKeyValue("domain", map));
		} else if (url.toString().endsWith(".layout.xml")) {
			map = HydridesFactory.getInstance(Layout.class, url).getData();
			System.out.println("printing layout:"
					+ printKeyValue("layout", map));
		}

		return map;
	}

}
