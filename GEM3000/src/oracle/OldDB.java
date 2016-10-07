package oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OldDB {

	static Connection query = null;
	public static Connection update=null;
	static Connection monitor=null;
	static boolean isOpen = false;
	static String  dbUrl = DBConfig.old_jdbc+DBConfig.old_host+DBConfig.old_dbName;
	static PreparedStatement ps_1 = null, ps_2 = null, ps_3 = null;
	static ResultSet rs_1 = null, rs_2 = null, rs_3 = null;

	public static String getSampleNo(String sampleid) {
		openUpdate();
		String sampleno = null;
		try {
			ps_2 = update.prepareStatement(OldSqls.getSampleNoByCode);
			ps_2.setString(1, sampleid);
			rs_2 = ps_2.executeQuery();
			while (rs_2.next()) {
				sampleno = rs_2.getString("ybbh");
			}
			rs_2.close();
			ps_2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sampleno;
	}
	
	public static String query(String sampleId) {    // 'A12000129836'
		openQuery();
		StringBuilder yqxmdhs = new StringBuilder();
		String yqdh = null, cdrq = null, ybbh = null, xmdh = null;
		String sql_1 = OldSqls.sql_1;
		try {
			ps_1 = query.prepareStatement(sql_1);
			ps_1.setString(1, sampleId.trim());
			rs_1 = ps_1.executeQuery();
			while (rs_1.next()) {
				ybbh = rs_1.getString("ybbh");
				if (ybbh==null) {
					System.out.println("该样本信息不存在于sqlserver库");
					return null;
				}
				yqdh = rs_1.getString("yqdh");
				cdrq = rs_1.getString("cdrq").substring(0, 10);
				
			//	System.out.println(yqdh + "," + cdrq + "," + ybbh);
			}
			rs_1.close();
			ps_1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String sql_2 = OldSqls.sql_2;
		try {
			ps_2 = query.prepareStatement(sql_2);
			ps_2.setString(1, yqdh);
			ps_2.setString(2, cdrq);
			ps_2.setString(3, ybbh);
			rs_2 = ps_2.executeQuery();
			while (rs_2.next()) {
				xmdh = rs_2.getString("xmdh");
				//System.out.println(xmdh);
				String sql_3 = OldSqls.sql_3;
				ps_3 = query.prepareStatement(sql_3);
				ps_3.setString(1, yqdh);
				ps_3.setString(2, xmdh);
				rs_3 = ps_3.executeQuery();
				while (rs_3.next()) {
					//System.out.println(rs_3.getString("yqxmdh"));
					yqxmdhs.append(rs_3.getString("yqxmdh") + ",");
				}
			}
			yqxmdhs.append(ybbh);
			rs_2.close();
			ps_2.close();
			rs_3.close();
			ps_3.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String sub = yqxmdhs.toString();
		return sub;
	}

	
	
	public String monitorNewSamp() {
		String query = OldSqls.monitor;
		StringBuilder sBuilder=new StringBuilder();
		String string = "";
		try {
			PreparedStatement ps_1 = monitor.prepareStatement(query);
			ps_1.setString(1, "XN-9000");
			ps_1.setString(2, getDate(new Date()));
			ps_1.setString(3, "j");
			ResultSet rs_1 = ps_1.executeQuery();
			while (rs_1.next()) {
				sBuilder.append(rs_1.getString("ybid")).append(",").append(rs_1.getString("brbq")).append("|");
			}
			rs_1.close();
			ps_1.close();
			string=sBuilder.toString();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return string;
	}

	
	public static void closeQuery() {
		try {
			if (query != null && !query.isClosed()) {
				query.close();
				query = null;
				System.out.println("query close!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void openQuery() {
		try {
			if (query == null || query.isClosed()) {
				Class.forName(DBConfig.old_driverName);
				System.out.println(dbUrl);
				query = DriverManager.getConnection(dbUrl, DBConfig.old_userName,
						DBConfig.old_userPwd);
				System.out.println("query open!");
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
	}
	
	public static void closeUpdate() {
		try {
			if (update != null && !update.isClosed()) {
				update.close();
				update = null;
				System.out.println("update close!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void openUpdate() {
		try {
			if (update == null || update.isClosed()) {
				Class.forName(DBConfig.old_driverName);
				System.out.println(dbUrl);
				update = DriverManager.getConnection(dbUrl, DBConfig.old_userName,
						DBConfig.old_userPwd);
				System.out.println("update open!");
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
	}
	private String getDate(Date date){
		SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd");
		return df.format(date);
	}
}
