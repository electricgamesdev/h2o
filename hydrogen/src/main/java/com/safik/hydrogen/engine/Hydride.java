package com.safik.hydrogen.engine;

import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Hydride extends Thread{
	
	HydrideContext context;
	
	public Hydride(HydrideContext context) {
		this.context=context;
	}

	public HydrideContext getContext() {
		return context;
	}
	
	public void setContext(HydrideContext context) {
		this.context = context;
	}
	
	

	public void inialize() {
	
		 ExecutorService executor = Executors.newCachedThreadPool(); 
		    try {
		    	//context.getSourceEntities(source);
		        new ConfigWriter(executor, 5).readCells();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		    executor.shutdownNow();
	}
	
	class ConfigWriter {
	
	private int maxTasks;
	private ExecutorService executor;
	private CountDownLatch finished;
	private LinkedBlockingQueue<Writer> q;

	public ConfigWriter(ExecutorService executor, int maxTasks) {
	    this.executor = executor;
	    this.maxTasks = maxTasks;
	    finished = new CountDownLatch(maxTasks);
	    q = new LinkedBlockingQueue<Writer>();
	}

	public void readCells() throws Exception {

	    for (int i = 0; i < maxTasks; i++) {
	        executor.execute(new ExcellUrlParser(q, finished));
	    }
	    ExcellReader reader = new ExcellReader(getExampleUrls(10));
	    while (reader.hasNext()) {
	        q.add(reader.next());
	    }
	    for (int i = 0; i < maxTasks; i++) {
	        q.add(new Writer(null));
	    }   
	    System.out.println("Awaiting excell url cell tasks.");
	    finished.await();
	    System.out.println("Done.");
	}

	private URL[] getExampleUrls(int amount) throws Exception {

	    URL[] urls = new URL[amount];
	    for (int i = 0; i < amount; i++) {
	        urls[i] = new URL("http://localhost:" + (i + 2000) + "/");
	    }
	    return urls;
	}

	 class ExcellUrlParser implements Runnable {

	    private CountDownLatch finished;
	    private LinkedBlockingQueue<Writer> q;

	    public ExcellUrlParser(LinkedBlockingQueue<Writer> q, CountDownLatch finished) {
	        this.finished = finished;
	        this.q = q;
	    }
	    public void run() {

	        try {
	            while (true) {
	                Writer urlCell = q.take();
	                if (urlCell.isFinished()) {
	                    break;
	                }
	                processUrl(urlCell.getUrl());
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            finished.countDown();
	        }
	    }

	    private void processUrl(URL url) {
	        try { Thread.sleep(1); } catch (Exception ignored) {}
	        System.out.println(url);
	    }

	}

	 class ExcellReader implements Iterator<Writer> {

	    private URL[] urls;
	    private int index;

	    public ExcellReader(URL[] urls) {
	        this.urls = urls;
	    }

	    public boolean hasNext() {
	        return (index < urls.length);
	    }

	    public Writer next() {
	        Writer urlCell = new Writer(urls[index]);
	        index++;
	        return urlCell;
	    }

	    public void remove() {
	        throw new UnsupportedOperationException();
	    }

	}

	 class Writer {

	    private URL url;

	    public Writer(URL url) {
	        this.url = url;
	    }

	    public URL getUrl() {
	        return url;
	    }

	    public boolean isFinished() {
	        return (url == null);
	    }
	}
	}

}
