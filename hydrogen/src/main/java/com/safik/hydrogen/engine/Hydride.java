package com.safik.hydrogen.engine;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.safik.hydrogen.ex.ScriptException;
import com.safik.hydrogen.flume.Flume;
import com.safik.hydrogen.oozie.Oozie;

public final class Hydride implements Runnable {

	private HydrideContext context = null;
	private String name = null;
	
	public String getName() {
		return name;
	}
	
	public Hydride(String name,HydrideContext context) {
		this.context = context;
		this.name=name;
		String home = System.getProperty("user.home");
		File file = new File(home+File.separator+"hydrogen");
		FileUtils.deleteQuietly(file);
		try {
			FileUtils.forceMkdir(file);
			context.setGV(GV.HOME,file);
			Path to =new Path("/user/hydrogen/app/"+name);
			Path dataDir =new Path("/user/hydrogen/app/"+name+"/data/valid");
			Path errDir =new Path("/user/hydrogen/app/"+name+"/data/invalid");
			Path scrDir =new Path("/user/hydrogen/app/"+name+"/script");
			
			context.setGV(GV.HDFS_HOME,to);
			context.setGV(GV.HDFS_HOME_SCRIPT,scrDir);
			context.setGV(GV.HDFS_HOME_DATA,dataDir);
			context.setGV(GV.HDFS_HOME_ERROR,errDir);
			FileSystem fileSystem = null;
			Configuration conf = new Configuration();
			conf.addResource(new Path(context.getValue("core-site.xml")));
			conf.addResource(new Path(context.getValue("hdfs-site.xml")));
			conf.addResource(new Path(context.getValue("mapred-site.xml")));
			conf.addResource(new Path(context.getValue("yarn-site.xml")));

			System.out.println("conf " + conf.toString());

			fileSystem = FileSystem.get(conf);

			context.setGV(GV.HDFS,fileSystem);
			System.out.println("clean up " + to.toString());
			if (!fileSystem.exists(to)){
				fileSystem.mkdirs(to);
			}else{
				fileSystem.delete(to, true);
				fileSystem.mkdirs(to);
			}			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public HydrideContext getContext() {
		return context;
	}

	private ScriptFactory flume = null,oozie=null;
	private List<Future<String>>  flist = new ArrayList<Future<String>>();
	
	private boolean checkStage(){
		for (Future<String> future : flist) {
			try {
				String str = future.get();
				System.out.println("&&&&&  str  "+str);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}
	
	public void inialize() {
		try {
			
			
			flume = ScriptFactory.getInstance(Flume.class, getContext());
			oozie = ScriptFactory.getInstance(Oozie.class, getContext());

			ExecutorService stage1 = Executors.newCachedThreadPool();

			flist.add(flume.init(stage1));
			flist.add(oozie.init(stage1));
			
			checkStage();
			
			stage1.shutdownNow();

			
			oozie.start();
			flume.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

			public ExcellUrlParser(LinkedBlockingQueue<Writer> q,
					CountDownLatch finished) {
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
				try {
					Thread.sleep(1);
				} catch (Exception ignored) {
				}
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

	public void shutdown() {
		try {
			flume.shutdown();
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}

}
