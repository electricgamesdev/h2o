package com.safik.hydrogen.hdfs;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;

import com.safik.hydrogen.conf.wf.WF;
import com.safik.hydrogen.engine.Hydride;
import com.safik.hydrogen.engine.HydrideContext;
import com.safik.hydrogen.flume.Flume;

public class HDFSSetup  {

	public HDFSSetup(HydrideContext context) {
		super();
	}

	public void run() {
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
			fileSystem.deleteOnExit(to);
			fileSystem.mkdirs(to);
			
			System.out.println("copying workflows "+to.toString());
			fileSystem.copyFromLocalFile(new Path(WF.class.getResource("co.xml").toURI()), to);	
			fileSystem.copyFromLocalFile(new Path(WF.class.getResource("testjob.xml").toURI()), to);	
		
			System.out.println("completed "+to.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		HDFSSetup hdfs=new HDFSSetup(null);
		//hdfs.start();
	}

}
