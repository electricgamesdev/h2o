package com.safik.hydrogen.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.safik.hydrogen.engine.Entity;

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

			stmt = dbCon
					.prepareStatement("insert into hcl(processId,process,status) values(?,?,?)");
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
	
	public static void insertEvent(String basename, long id, String desc,
			String status, String source, String pk) {
		try {

			Connection dbCon = null;
			PreparedStatement stmt = null;

			// getting database connection to MySQL server
			dbCon = DriverManager.getConnection(dbURL, username, password);

			stmt = dbCon
					.prepareStatement("insert into h2o(processId,basename,descrption,status,source,groupkey) values(?,?,?,?,?,?)");
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
	
 

	public static List<Entity> isAllEntityAsEvent(String pk, List<Entity> elist) {

		try {

			Connection dbCon = DriverManager.getConnection(dbURL, username, password);
			PreparedStatement stmt = dbCon.prepareStatement("select * from h2o where groupkey=? and status=?");
			stmt.setString(1, pk);
			stmt.setString(2, "INIT");
			insertEvent("kjknkj", 6666L, "stttt", "TRACK", "hghg", pk);
			
		ResultSet rs = stmt.executeQuery();
		insertEvent("kjknkj", 7777L, "stttt:"+elist.size(), "TRACK", "hghg:"+rs.getFetchSize(), pk);
			 int i=0;
			 		while(rs.next()){
					   insertEvent("kjknkj", 7711L, "stttt:"+elist.size(), "TRACK", "hghg:"+rs.getFetchSize(), pk);
						for (Entity entity : elist) {
								insertEvent("kjknkj", 7722L, "stttt:"+elist.size(), "TRACK", "hghg:"+rs.getFetchSize(), pk);
							Pattern pattern = Pattern.compile(entity.getPattern());
							Matcher matcher = pattern.matcher(rs.getString("basename"));
							String s=rs.getString("basename")+"."+rs.getString("processId")+".processing";
							if (matcher.matches()) {
								i++;
								entity.setOk(true);
								entity.setName(s);
								insertEvent(s, 8888L, "matching", "TRACK", "hghg", pk);
								break;
							}else{
								insertEvent(s, 9999L, "not matching", "TRACK", rs.getString("groupkey"), pk);
							}
							
							}
						}
						
					if(elist.size()==i){
						insertEvent("ggggggg", 9911L, "count match", "TRACK",pk, pk);
						boolean allOk=true;
						for (Entity entity : elist) {
							if(!entity.isOk()){
								allOk=false;
							    break;
							}
						}
						
						if(allOk){
							PreparedStatement stmt1 = dbCon.prepareStatement("update h2o set status=? where groupkey=?");
							stmt1.setString(1, "COMP");
							stmt1.setString(2, pk);
							stmt1.execute();
							return elist;
						}
						
					}else{
						
						insertEvent("ggggggg", 9922L, "count not match", "TRACK",pk, pk);
						
					}
					
			
					
			
		
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static String getProcess(String process) {
	
		try{
		Connection dbCon = null;
		PreparedStatement stmt = null;

		dbCon = DriverManager.getConnection(dbURL, username, password);

		stmt = dbCon.prepareStatement("select processId from hcl where process=? and status=?");
		stmt.setString(1, process);
		stmt.setString(2, "RUNNING");
		

		ResultSet rs = stmt.executeQuery();
		Long ps=null;
		if(rs.next()){
			ps=rs.getLong(1);
		}
		
		if(ps!=null){
			stmt = dbCon.prepareStatement("update hcl set status=? where process=? and status=?");
			stmt.setString(1, "STOPPED");
			stmt.setString(2, process);
			stmt.setString(3, "RUNNING");
			stmt.execute();
			return ps+"";
		}
		
		
		
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	
}
