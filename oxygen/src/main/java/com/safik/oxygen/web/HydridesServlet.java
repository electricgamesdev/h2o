package com.safik.oxygen.web;

import java.net.URL;

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

		String input =	getServletConfig().getInitParameter("hydrides");
	
		URL url = getServletConfig().getServletContext().getClassLoader().getResource(input);
		
		if(url!=null){
			try {
				HydrideContext context = new HydrideContext(url);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			if (input != null)
				logger.info("file " + url.getFile());
			System.out.println("path--- "+url.getPath());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	//	logger.info("url " + url);

		if (logger.isDebugEnabled()) {
			logger.debug("Servlet '" + getServletName() + "' configured successfully");
		}
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
