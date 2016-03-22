package com.safik.hydrogen.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.safik.hydrogen.db.DBHelper;
import com.safik.hydrogen.engine.Entity;
import com.safik.hydrogen.engine.Stages;
import com.safik.hydrogen.engine.Status;
import com.safik.hydrogen.model.Entity_Detail;
import com.safik.hydrogen.model.Source_Detail;
import com.safik.hydrogen.model.Source_Master;
import com.safik.hydrogen.model.Task_Detail;

public class EventJDBCHelper {

	private static String dbURL = "jdbc:mysql://localhost:3306/hydrodb";
	private static String username = "root";
	private static String password = "cloudera";

	public static void insertProcess(String process, long id, String status) {
		try {

			Connection dbCon = null;
			PreparedStatement stmt = null;

			// getting database connection to MySQL server
			dbCon = DriverManager.getConnection(dbURL, username, password);

			stmt = dbCon.prepareStatement("insert into hcl(processId,process,status) values(?,?,?)");
			stmt.setLong(1, id);
			stmt.setString(2, process);
			stmt.setString(3, status);

			boolean e = stmt.execute();
			if (e == true && !dbCon.getAutoCommit())
				dbCon.commit();
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	public static void insertEvent(String basename, long id, String desc, String status, String source, String pk) {
		try {

			Connection dbCon = null;
			PreparedStatement stmt = null;

			// getting database connection to MySQL server
			dbCon = DriverManager.getConnection(dbURL, username, password);

			stmt = dbCon.prepareStatement("insert into h2o(processId,basename,descrption,status,source,groupkey) values(?,?,?,?,?,?)");
			stmt.setLong(1, id);
			stmt.setString(2, basename);
			stmt.setString(3, desc);
			stmt.setString(4, status);
			stmt.setString(5, source);
			stmt.setString(6, pk);

			boolean e = stmt.execute();
			if (e == true && !dbCon.getAutoCommit())
				dbCon.commit();
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	public static List<Source_Detail> isAllEntityAsEvent(String pk, Source_Master master) {

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("dataset_filter_value", pk);
		map.put("status", Status.INIT);
		List<Source_Detail> details = DBHelper.findByCriteria(Source_Detail.class, map);
		List<Source_Detail> finals = new ArrayList<Source_Detail>();
		int i = 0;
		for (Entity_Detail entity : master.getEntity_Details()) {
			for (Source_Detail detail : details) {
				Pattern pattern = Pattern.compile(entity.getPattern());
				Matcher matcher = pattern.matcher(detail.getSource_header());
				String s = detail.getSource_header() + "." + detail.getFlume_unique_id() + ".processing";
				if (matcher.matches()) {
					i++;
					detail.setFlume_header(s);
					finals.add(detail);
					break;
				}

			}
			if (master.getEntity_Details().size() == finals.size())
				return finals;
		}

		return null;
	}

	public static List<Long> getProcess(String process) {
		List<Long> plist = new ArrayList<Long>();
		try {
			Connection dbCon = null;
			PreparedStatement stmt = null;

			dbCon = DriverManager.getConnection(dbURL, username, password);

			stmt = dbCon.prepareStatement("select processId from hcl where process=? and status=?");
			stmt.setString(1, process);
			stmt.setString(2, "RUNNING");

			ResultSet rs = stmt.executeQuery();
			Long ps = null;
			if (rs.next()) {
				plist.add(rs.getLong(1));
			}

			stmt = dbCon.prepareStatement("update hcl set status=? where process=? and status=?");
			stmt.setString(1, "STOPPED");
			stmt.setString(2, process);
			stmt.setString(3, "RUNNING");
			stmt.execute();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return plist;
	}

	public static List<Task_Detail> getCoordinatorJob() {
		Map<String,Object> map= new HashMap<String, Object>();
		map.put("stage", Stages.COORDINATOR.name());
		map.put("status", Status.RUNNING.name());
		List<Task_Detail> list= DBHelper.findByCriteria(Task_Detail.class, map);
		return list;
	}

	public static  List<Task_Detail>  getWorkflowJob() {
		Map<String,Object> map= new HashMap<String, Object>();
		map.put("stage", Stages.WORKFLOW.name());
		map.put("status", Status.RUNNING.name());
		List<Task_Detail> list= DBHelper.findByCriteria(Task_Detail.class, map);
		return list;
	}

	public static void updateOozieTask(Task_Detail td,String status) {
		td.setEvent_status(status);
		td.setUpdated_time(DBHelper.ts());
		DBHelper.merge(td);
	}

}
