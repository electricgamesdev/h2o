package com.safik.oxygen.web;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HydridesServlet extends HttpServlet {
	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	@Override
	public final void init() throws ServletException {
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing servlet '" + getServletName() + "'");
		}

		String input = getServletConfig().getInitParameter("hydrides");

		URL url = getServletConfig().getServletContext().getClassLoader().getResource(input);

		if (url != null) {
			try {

				// stage 1 : Loading hydrides - initial check
				Map<String, Map> hydrides = HydridesFactory.getInstance(Hydride.class, url).getData();
				System.out.println("printing hydride:" + printKeyValue("hydrides",hydrides));

				Map domains = hydrides.get("domains");
				Map layouts = hydrides.get("layouts");
				Map technology = hydrides.get("technology");

				// stage 2 : Loading layouts and domains -
				// technology/environment/infrastructure check
				url = new File((String) ((Map) domains.get("data_profiling")).get("path")).toURI().toURL();
				Map<String, Map> d1 = HydridesFactory.getInstance(Domain.class, url).getData();
				System.out.println("printing domain:" + printKeyValue("domain",d1));

				url = new File((String) ((Map) layouts.get("data_profiling")).get("path")).toURI().toURL();
				Map<String, Map> l1 = HydridesFactory.getInstance(Layout.class, url).getData();
				System.out.println("printing layout:" + printKeyValue("layout",l1));

				// stage 3 : Loading fundamental comps - implementation check

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Servlet '" + getServletName() + "' configured successfully");
		}
	}

	private StringBuilder printKeyValue(String elem,Map d1) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
	
		for (Object k : d1.keySet()) {
			Object v = d1.get(k);
			if (v instanceof String) {
				sb.append(elem+"["+k+"="+ v + "]\n");
			} else {
				sb.append(printKeyValue(elem+":"+k.toString(),(Map) v));
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

}
