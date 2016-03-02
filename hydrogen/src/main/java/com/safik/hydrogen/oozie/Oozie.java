package com.safik.hydrogen.oozie;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.WorkflowJob;

import com.safik.hydrogen.conf.wf.WF;
import com.safik.hydrogen.engine.Entity;
import com.safik.hydrogen.engine.Hydride;
import com.safik.hydrogen.engine.HydrideContext;

public class Oozie extends Hydride {

	
	public Oozie(HydrideContext context) {
		super(context);
	}

	@Override
	public void run() {
		try {

			Properties p = new Properties();
			p.load(Oozie.class.getResourceAsStream("co.properties"));

			OozieClient wc = new OozieClient(p.getProperty("oozie.url"));

			// create a workflow job configuration and set the workflow
			// application path
			Properties conf = wc.createConfiguration();
			for (Object key : p.keySet()) {
				conf.put(key, p.getProperty(key.toString()));
			}

			// submit and start the workflow job
			String jobId = wc.run(conf);

			System.out.println("Workflow job submitted");

			// wait until the workflow job finishes printing the status every 10
			// seconds
			while (wc.getJobInfo(jobId).getStatus() == WorkflowJob.Status.RUNNING) {
				System.out.println("Workflow job running ...");
				Thread.sleep(10 * 1000);
			}

			// print the final status o the workflow job
			System.out.println("Workflow job completed ...");
			System.out.println(wc.getJobInfo(jobId));
		} catch (OozieClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		List<Entity> elist = new ArrayList<Entity>();

		Entity e1 = new Entity();
		e1.setId("ctl");
		e1.setHeader(true);
		e1.setPattern("^(ord_?[a-z]{2}_?[a-z]{3}_?[a-z]{3}_?[a-z]{3}_?[0-9]{8}_?[0-9]{12}.ctl)$");
		e1.setSeperator("|");
		e1.setName("ord_in_crd_gcb_all_20150331_201312031234.ctl.1456708922528.processing");
	
		elist.add(e1);

		Entity e2 = new Entity();
		e2.setId("dat");
		e2.setHeader(true);
		e2.setPattern("^(ord_?[a-z]{2}_?[a-z]{3}_?[a-z]{3}_?[a-z]{3}_?[0-9]{8}_?[0-9]{12}.dat)$");
		e2.setSeperator("|");
		e2.setName("ord_in_crd_gcb_act_20150331_201312031234.dat.1456708922003.processing");

		elist.add(e2);
		Oozie oozie = new Oozie(null);
		oozie.initialize(elist);
		//oozie.start();
	}

	private void initialize(List<Entity> elist) {
		String co=null;
		String fc=null;
		String mv="<move source=\"${nameNode}/user/hydrogen/source/#1'\" target=\"${nameNode}/user/hydrogen/target/${wf:id()}/#1\"/>";
		try {
			StringBuilder ds = new StringBuilder();
			StringBuilder ie = new StringBuilder();
			StringBuilder fs = new StringBuilder();
			
			for (Entity entity : elist) {
				String s=IOUtils.toString(Oozie.class.getResourceAsStream("ds.xml"));
				s=StringUtils.replace(s, "#1", entity.getId());
				ds.append(StringUtils.replace(s, "#2", entity.getName()));
		
				String i=IOUtils.toString(Oozie.class.getResourceAsStream("ie.xml"));
				i=StringUtils.replace(i, "#1", entity.getId()+"IN");
				ie.append(StringUtils.replace(i, "#2", entity.getId()));
				
				fs.append(StringUtils.replace(mv, "#1", entity.getName()));
				  
			}
			
			co=IOUtils.toString(Oozie.class.getResourceAsStream("co.xml"));
			co=StringUtils.replace(co, "#1", ds.toString());
			co=StringUtils.replace(co, "#2", ie.toString());
			System.out.println("co="+co); 
			fc=IOUtils.toString(Oozie.class.getResourceAsStream("fc.xml"));
			fc=StringUtils.replace(fc, "#1", "dpf");
			fc=StringUtils.replace(fc, "#2", fs.toString());
			
			System.out.println("fc="+fc); 
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DistributedFileSystem fileSystem = null;
		Configuration conf = new Configuration();

		conf.addResource(new Path(
				"/home/cloudera/workspace/HydroTest/src/main/java/com/test/core-site.xml"));
		conf.addResource(new Path(
				"/home/cloudera/workspace/HydroTest/src/main/java/com/test/hdfs-site.xml"));
		conf.addResource(new Path(
				"/home/cloudera/workspace/HydroTest/src/main/java/com/test/mapred-site.xml"));
		conf.addResource(new Path(
				"/home/cloudera/workspace/HydroTest/src/main/java/com/test/yarn-site.xml"));
		System.out.println("conf " + conf.toString());
		try {
			fileSystem = (DistributedFileSystem) DistributedFileSystem
					.get(conf);

			Path to = new Path("/user/hydrogen/workflow");
			System.out.println("clean up "+to.toString());
			if(!fileSystem.exists(to))
			fileSystem.mkdirs(to);
			
			System.out.println("copying workflows "+to.toString());
			FSDataOutputStream output = fileSystem.create(new Path("/user/hydrogen/workflow/co.xml"));
			output.writeBytes(co);
			output.close();
			FSDataOutputStream output2 = fileSystem.create(new Path("/user/hydrogen/workflow/fc.xml"));
			output2.writeBytes(fc);
			output2.close();
		
			System.out.println("completed "+to.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
