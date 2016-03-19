package com.safik.hydrogen.engine;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HydrideContext {

	private Map hydrides = null;
	private HydrideConnector connector;

	public HydrideContext(Map hydrides, HydrideConnector connector, Map<String, String> pMap) {
		this.hydrides = hydrides;
		this.connector = connector;
		this.pMap=pMap;
	}

	public Map getDomains() {
		Map d = (Map) hydrides.get("domains");
		return d;
	}

	public Map getDomain(String domainName) {

		Map m = (Map) getDomains().get(domainName);

		return loadPath(m);

	}

	public Map getDomainSources(String domain) {
		Map m = getDomain(domain);
		return (Map) m.get("source-watcher");
	}
	
	public Map getDomainWorkflows(String domain) {
		Map m = getDomain(domain);
		return (Map) m.get("workflow");
	}
	
	public Map getWorkflowOfDomain(String domain,String wfname) {
		Map m = getDomainWorkflows(domain);
		return (Map) m.get(wfname);
	}
	
	public Map getDomainWorkflowActions(String domain,String wfname) {
		Map m = getWorkflowOfDomain(domain,wfname);
		return (Map) m.get("action");
	}
	
	public Map getDomainWorkflowAction(String domain,String wfname,String action) {
		Map m = getDomainWorkflowActions(domain,wfname);
		return loadPath((Map) m.get(action));
	}
	
	public Map getDomainWorkflowActionEntities(String domain,String wfname,String action) {
		Map m = getDomainWorkflowActions(domain,wfname);
		Map m2= (Map) m.get(action);
		return (Map)m2.get("entity");
	}
	
	
	public Map getDomainWorkflowActionEntity(String domain,String wfname,String action,String entity) {
		Map m = getDomainWorkflowActionEntities(domain,wfname,action);
		return loadPath((Map)m.get(entity));
	}
	

	public Map getDomainSource(String domain, String source) {
		Map m = getDomainSources(domain);
		return (Map) m.get(source);
	}

	public Map getDomainSourceEntities(String domain, String source) {
		Map m = getDomainSource(domain, source);
		return (Map) m.get("entity");
	}

	public Map getDomainSourceEntity(String domain, String source,
			String entity) {
		Map m = getDomainSourceEntities(domain, source);
		return loadPath((Map) m.get(entity));
	}

	Map<String,Map> cache = new HashMap<String,Map>();
	private Map loadPath(Map m) {
		String path = (String) m.get("path");

		try {
			if (path == null)
				throw new Exception("Path not found " + path);
			if(!cache.containsKey(path)){
			URL url = new File(path).toURI().toURL();
			System.out.println("Loading "+url);
			cache.put(path, connector.getData(url));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cache.get(path);
	}

	public Map getLayouts() {
		Map d = (Map) hydrides.get("layouts");
		return d;
	}
	
	public Map getLayout(String layout) {
		Map d = getLayouts();
		return loadPath((Map)d.get(layout));
	}
	
	public Map getLayoutDimensions(String layout) {
		Map d = getLayout(layout);
		return (Map)d.get("dimension");
	}

	public Map getLayoutDimension(String layout,String dim) {
		Map d = getLayoutDimensions(layout);
		return (Map)d.get(dim);
	}
	
	public Map getLayoutDimensionFilters(String layout,String dim) {
		Map d = getLayoutDimension(layout,dim);
		return (Map)d.get("filter");
	}
	
	public Map getLayoutDimensionForms(String layout,String dim) {
		Map d = getLayoutDimension(layout,dim);
		return (Map)d.get("forms");
	}
	
	public Map getLayoutDimensionForm(String layout,String dim,String form) {
		Map d = getLayoutDimensionForms(layout,dim);
		return loadPath((Map)d.get(form));
	}
	
	public Map getLayoutDimensionFormActions(String layout,String dim,String form) {
		Map d = getLayoutDimensionForms(layout,dim);
		Map f=(Map)d.get(form);
		return (Map)f.get("actions");
	}
	
	public Map getLayoutDimensionFormAction(String layout,String dim,String form,String action) {
		Map d = getLayoutDimensionFormActions(layout,dim,form);
		return loadPath((Map)d.get(action));
	}

	public String getValue(Map m, String string) {
		if(m.get(string)!=null)
			return (String)m.get(string);
		else{
			if(m.get("attr")!=null){
				Map m2=(Map)m.get("attr");
				if(m2.get(string)!=null)
					return (String)m2.get(string);
			}
		}
		return "";
	}

	private Map<String,String> pMap = new HashMap<String, String>();
	public String getValue(String string) {
		System.out.println("HydrideContext GET:"+string+"="+pMap.get(string));
		return pMap.get(string);
	}
	

}
