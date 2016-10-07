package oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewDB {
	public static Connection query = null;
	public static Connection update = null;
	public static Connection monitor = null;
	static SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd");
	static String dbUrl = DBConfig.jdbc + DBConfig.host;
	static PreparedStatement ps_1 = null, ps_2 = null, ps_3 = null;
	static ResultSet rs_1 = null, rs_2 = null, rs_3 = null;

	static Map<String, String> ylxhMap = new HashMap<String, String>();
	static Map<String, String> testChannelMap = new HashMap<String, String>();
	static Map<String, String> testChannelMap_1 = new HashMap<String, String>();
	static Map<String, TestDescribe> labIndexMap = new HashMap<String, TestDescribe>();
	static Map<String, List<TestReference>> testReferenceMap = new HashMap<String, List<TestReference>>();
	static Map<String, Formula> fromulaMap = new HashMap<String, Formula>();

	public static void initFromula() {

	}

	public static Device getDevice(String deviceId, String segment) {
		openQuery();
		DBConfig.segment = segment.toUpperCase();
		if (deviceId != null) {
			DBConfig.deviceid = deviceId.toUpperCase();
		}
		Device device = new Device();
		try {
			if (deviceId == null) {
				ps_1 = query.prepareStatement(NewSqls.l_device_d);
			} else {
				ps_1 = query.prepareStatement(NewSqls.l_device);
				ps_1.setString(1, deviceId);
			}
			rs_1 = ps_1.executeQuery();
			while (rs_1.next()) {
				device.setId(rs_1.getString("id"));
				device.setName(rs_1.getString("name"));
				device.setComport(rs_1.getString("comport"));
				device.setBaudrate(rs_1.getString("baudrate"));
				device.setDataBit(rs_1.getString("databit"));
				device.setStopBit(rs_1.getString("stopbit"));
				device.setParity(rs_1.getString("parity"));
				device.setLab(rs_1.getString("lab"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs_1);
			close(ps_1);
		}
		if (deviceId == null) {
			initYlxhMap(null);
		} else {
			initYlxhMap(device.getLab());
		}

		System.out.println(ylxhMap.size());
		initLabIndex(deviceId);
		System.out.println(labIndexMap.size());
		initTestChannelMap(deviceId);
		System.out.println(testChannelMap.size());
		return device;
	}

	// 新系统的查询方法 双向
	public static String query(String sampleId) {
		openQuery();
		long start = System.currentTimeMillis();
		StringBuilder channels = new StringBuilder();
		String ylxh = null;
		String sampleNo = "";
		try {
			ps_1 = query.prepareStatement(NewSqls.getSampleBy_code);
			ps_1.setString(1, sampleId);
			rs_1 = ps_1.executeQuery();
			while (rs_1.next()) {
				sampleNo = rs_1.getString("sampleno");
				ylxh = rs_1.getString("ylxh");
			}
			if (sampleNo == null) {
				System.out.println("该样本信息不存在于oracle库");
				// -------------------------老的查询方法----------------------------------------
				return channels.append(OldDB.query(sampleId)).toString(); // 返回
				// -------------------------------------------------------------------------
			}
			String testid = "";
			if (ylxh.indexOf("+") > 0) {
				for (String xh : ylxh.split("+")) {
					testid += ylxhMap.get(xh);
				}
			} else {
				testid += ylxhMap.get(ylxh);
			}

			if (testid.contains(",")) {
				for (String test : testid.split(",")) {
					if (testChannelMap.containsKey(test)) {
						channels.append(testChannelMap.get(test) + ",");
					}
				}
			} else {
				if (testChannelMap.containsKey(testid)) {
					channels.append(testChannelMap.get(testid) + ",");
				}
			}
			channels.append(sampleNo); // 最后保存样本号
			System.out.println("处理请求Q： " + (System.currentTimeMillis() - start)
					+ "毫秒");
		} catch (Exception e) {
			System.out.println("!!!!!!!!" + e.getMessage());
		} finally {
			close(rs_1);
			close(ps_1);
		}
		return channels.toString();
	}

	static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	public static void batchSave(ArrayList<ResultInfo> list) {
		String ybbh = null;
		openUpdate();
		ArrayList<ResultInfo> insertList = new ArrayList<ResultInfo>();
		ArrayList<ResultInfo> insertList_log = new ArrayList<ResultInfo>();
		ArrayList<ResultInfo> updateList = new ArrayList<ResultInfo>();
		for (int i = 0; i < list.size(); i++) {
			ResultInfo ri = list.get(i);
			if (ybbh == null) {
				ybbh = ri.sampleNo;
			}
			String status = getStatus(ri);
			if (status.equals("4")) {
				System.out.println("没有该testid");
				continue;
			} else if (status.equals("3")) {
				System.out.println("该项目与历史值相同，无需要修改");
			} else if (status.equals("2")) {
				System.out.println("该样本已经审核！");
				return;
			} else if (status.equals("0")) {
				insertList.add(ri); // 插入
			} else { // "1|12.3|201612030423"
				Date date = new Date();
				try {
					date = sdf.parse(status.split("\\|")[2]);
					insertList_log.add(new ResultInfo(ri.sampleNo, ri.channel,
							status.split("\\|")[1], date));
					updateList.add(ri);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

		// 批量插入或更新

		if (insertList.size() > 0) {
			try {
				update.setAutoCommit(false);
				ps_2 = update.prepareStatement(NewSqls.insert_TestResult);
				for (int i = 0; i < insertList.size(); i++) {
					ResultInfo ri = insertList.get(i);
					
					TestDescribe td = null;
					td = labIndexMap.get(testChannelMap_1.get(ri.channel));

					ps_2.setString(1, ri.sampleNo);
					// testresult
					ps_2.setString(7, ri.result);
					// teststatus
					ps_2.setInt(8, 4); // 设定为3
					// 时间 measuretime;
					ps_2.setTimestamp(4,
							new Timestamp(ri.measuretime.getTime()));
					if (td != null) {
						// testid
						ps_2.setString(2, td.index_id);
						// deviceid
						ps_2.setString(3, DBConfig.deviceid);
						// operator
						ps_2.setString(5, DBConfig.deviceid);
						ps_2.setString(6, td.sample_from);
						ps_2.setString(9, td.unit);
						ps_2.setString(10, td.name);
						ps_2.setString(11, td.method);
					} else {
						// testid
						ps_2.setString(2, "teid");
						// deviceid
						ps_2.setString(3, "deviceid");
						// operator
						ps_2.setString(5, "operator");
						ps_2.setString(6, "1");
						ps_2.setString(9, "unit");
						ps_2.setString(10, "name");
						ps_2.setString(11, "name");
					}
					ps_2.addBatch();
				}
				ps_2.executeBatch();
				update.commit();
				ps_2.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					update.setAutoCommit(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// log 表
		if (insertList_log.size() < 0) {
			try {
				update.setAutoCommit(false);
				ps_2 = update.prepareStatement(NewSqls.insert_TestResult_log);
				for (int i = 0; i < insertList_log.size(); i++) {
					ResultInfo ri = insertList_log.get(i);
					TestDescribe td = labIndexMap.get(testChannelMap_1
							.get(ri.channel));
					ps_2.setString(1, ri.sampleNo);
					// testresult
					ps_2.setString(7, ri.result);
					// teststatus
					ps_2.setInt(8, 4); // 设定为3
					// 时间 measuretime;
					ps_2.setTimestamp(4,
							new Timestamp(ri.measuretime.getTime()));
					if (td != null) {
						// testid
						ps_2.setString(2, td.index_id);
						// deviceid
						ps_2.setString(3, DBConfig.deviceid);
						// operator
						ps_2.setString(5, DBConfig.deviceid);
						ps_2.setString(6, td.sample_from);
						ps_2.setString(9, td.unit);
						ps_2.setString(10, td.name);
						ps_2.setString(11, td.method);
					} else {
						// testid
						ps_2.setString(2, "teid");
						// deviceid
						ps_2.setString(3, "deviceid");
						// operator
						ps_2.setString(5, "operator");
						ps_2.setString(6, "1");
						ps_2.setString(9, "unit");
						ps_2.setString(10, "name");
						ps_2.setString(11, "name");
					}
					ps_2.addBatch();
				}
				ps_2.executeBatch();
				update.commit();
				ps_2.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					update.setAutoCommit(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (updateList.size() > 0) {
			try {
				update.setAutoCommit(false);
				ps_2 = update.prepareStatement(NewSqls.new_update_result);
				for (int i = 0; i < updateList.size(); i++) {
					ResultInfo ri = updateList.get(i);
					TestDescribe td = labIndexMap.get(testChannelMap_1
							.get(ri.channel));
					ps_2.setString(1, ri.result);
					ps_2.setTimestamp(2,
							new Timestamp(ri.measuretime.getTime()));
					ps_2.setString(3, ri.sampleNo);
					ps_2.setString(4, td.index_id);
					ps_2.addBatch();
				}
				ps_2.executeBatch();
				update.commit();
				ps_2.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					update.setAutoCommit(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		try {
			update.setAutoCommit(true);
			updateSample(ybbh);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	static ResultLog log = null;

	public static String getStatus(ResultInfo ri) {
		String status = "0";
		String testid = "";
		String oldResult = "";
		// TestId

		for (String key : testChannelMap.keySet()) {
			if (ri.channel.equals(testChannelMap.get(key))) {
				testid = key;
				System.out.println(testid);
				break;
			}
		}

		if (testid.length() == 0) {
			status = "4";
			return status; // 没有该testid
		}

		try {
			update.setAutoCommit(true);
			ps_2 = update.prepareStatement(NewSqls.hasTestResult_sql);
			ps_2.setString(1, ri.sampleNo);
			ps_2.setString(2, testid);
			rs_2 = ps_2.executeQuery();
			while (rs_2.next()) {
				// 已审核，或者重做结果相同
				if (rs_2.getInt("TESTSTATUS") >= 5) {
					status = "2";
					return status;
				} else {
					oldResult = rs_2.getString("testresult").trim();
					String time = rs_2.getString("measuretime");
					if (oldResult == null) {
						return "0";
					} else if (!oldResult.equals(ri.result)) {
						log = new ResultLog();
						return "1" + "|" + oldResult + "|" + time;
					} else {
						return "3"; // 该项目所传的值与历史值相同，无需操作
					}
				}
			}
			rs_2.close();
			ps_2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return status;
	}

	public static void close(Object o) {
		if (o instanceof ResultSet) {
			if ((ResultSet) o != null) {
				try {
					((ResultSet) o).close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else if (o instanceof PreparedStatement) {
			if ((PreparedStatement) o != null) {
				try {
					((PreparedStatement) o).close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void initLabIndex(String deviceid) {
		openQuery();
		try {
			if (deviceid == null) {
				ps_1 = query.prepareStatement(NewSqls.lab_index_d);
			} else {
				ps_1 = query.prepareStatement(NewSqls.lab_index);
				ps_1.setString(1, "%" + deviceid + "%");
			}
			rs_1 = ps_1.executeQuery();
			while (rs_1.next()) {
				TestDescribe td = new TestDescribe();
				td.index_id = rs_1.getString("index_id");
				td.instrument = rs_1.getString("instrument");
				td.unit = rs_1.getString("unit");
				td.sample_from = rs_1.getString("sample_from");
				td.method = rs_1.getString("method");
				td.name = rs_1.getString("name");
				labIndexMap.put(td.index_id, td);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs_1);
			close(ps_1);
		}

	}

	private static void initYlxhMap(String ksdm) {
		openQuery();
		System.out.println("-----" + ksdm);
		try {
			if (ksdm == null) {
				ps_1 = query.prepareStatement(NewSqls.l_ylxhdescribe_d);
			} else {
				ps_1 = query.prepareStatement(NewSqls.l_ylxhdescribe);
				ps_1.setString(1, ksdm);
			}
			rs_1 = ps_1.executeQuery();
			while (rs_1.next()) {
				ylxhMap.put(rs_1.getString("ylxh"),
						rs_1.getString("profiletest"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs_1);
			close(ps_1);
		}
	}

	private static String getSampleNo(String sampleid) {
		openUpdate();
		ResultInfo ri = new ResultInfo();
		String sampleno = null;
		try {
			update.setAutoCommit(true);
			ps_2 = update.prepareStatement(NewSqls.getSampleNoByCode);
			ps_2.setString(1, sampleid);
			rs_2 = ps_2.executeQuery();
			while (rs_2.next()) {
				sampleno = rs_2.getString("sampleno");
			}
			// ----------------------旧系统查询---------------------------------
			if (sampleno == null) {
				System.out.println("该样本不在oracle库中！");
				return OldDB.getSampleNo(sampleid);
			}
			// -------------------------------------------------------
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs_2);
			close(ps_2);
		}
		return sampleno;
	}

	// 不管仪器传入的编号还是条码，parseMsg时调用，得到一个标本的编号
	public static String getSampleNoById(String sampleid, String date) {
		String sampleno = null;
		int length = sampleid.length();
		if (length == 12 && sampleid.startsWith("A")) {
			sampleno = getSampleNo(sampleid);
		} else if (length == 3) {
			sampleno = date + DBConfig.segment + sampleid;
		} else if (length == 2) {
			sampleno = date + DBConfig.segment + "0" + sampleid;
		} else if (length == 1) {
			sampleno = date + DBConfig.segment + "00" + sampleid;
		} else if (length >= 4 && length < 12) {
			sampleno = date + DBConfig.segment + sampleid.substring(length - 3);
		}
		return sampleno;
	}

	private static void initTestChannelMap(String deviceid) {
		openQuery();
		try {
			if (deviceid == null) {
				ps_1 = query.prepareStatement(NewSqls.lab_channel_d);
			} else {
				ps_1 = query.prepareStatement(NewSqls.lab_channel);
				ps_1.setString(1, deviceid);
			}
			rs_1 = ps_1.executeQuery();
			while (rs_1.next()) {
				testChannelMap.put(rs_1.getString("testid"),
						rs_1.getString("channel"));
				testChannelMap_1.put(rs_1.getString("channel"),
						rs_1.getString("testid"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs_1);
			close(ps_1);
		}
	}

	public static String monitorNewSamp(String deviceId) {
		openMonitor();
		StringBuilder sBuilder = new StringBuilder();
		try {
			ps_3 = monitor.prepareStatement(NewSqls.monitor_sql);
			ps_3.setString(1, deviceId);
			ps_3.setString(2, getDate(new Date()));
			ps_3.setString(3, "j");
			rs_3 = ps_3.executeQuery();
			while (rs_3.next()) {
				sBuilder.append(rs_3.getString("ybid")).append(",")
						.append(rs_3.getString("brbq")).append("|");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs_3);
			close(ps_3);
		}
		return sBuilder.toString();
	}

	public static String getDate(Date date) {
		return df.format(date);
	}

	// 要求传入的结果以样本编号为准,入库
	public static void saveResult(ResultInfo ri) {
		openUpdate();
		int insert = 0; // 0:插入;1:更新;2:什么都不干;
		String testid = "";
		String oldResult = "";
		// TestId
		for (String key : testChannelMap.keySet()) {
			if (ri.channel.equals(testChannelMap.get(key))) {
				testid = key;
				System.out.println(testid);
				break;
			}
		}

		if (testid.length() == 0) {
			return;
		}

		try {
			update.setAutoCommit(true);
			ps_2 = update.prepareStatement(NewSqls.hasTestResult_sql);
			ps_2.setString(1, ri.sampleNo);
			ps_2.setString(2, testid);
			rs_2 = ps_2.executeQuery();
			while (rs_2.next()) {
				// 已审核，或者重做结果相同
				if (rs_2.getInt("TESTSTATUS") >= 5) {
					insert = 2;
				} else {
					oldResult = rs_2.getString("testresult").trim();
					if (oldResult == null) {
						insert = 0;
					} else if (!oldResult.equals(ri.result)) {
						insert = 1;
					} else {
						insert = 2;
					}
				}
			}
			close(rs_2);
			close(ps_2);
			TestDescribe td = labIndexMap.get(testid);
			switch (insert) {
			case 0: // 插入
				ps_2 = update.prepareStatement(NewSqls.insert_TestResult);
				ps_2.setString(1, ri.sampleNo);
				// testresult
				ps_2.setString(7, ri.result);
				// teststatus
				ps_2.setInt(8, 4); // 设定为3
				// 时间 measuretime;
				ps_2.setTimestamp(4, new Timestamp(ri.measuretime.getTime()));
				if (td != null) {
					// testid
					ps_2.setString(2, td.index_id);
					// deviceid
					ps_2.setString(3, DBConfig.deviceid);
					// operator
					ps_2.setString(5, DBConfig.deviceid);
					ps_2.setString(6, td.sample_from);
					ps_2.setString(9, td.unit);
					ps_2.setString(10, td.name);
					ps_2.setString(11, td.method);
				} else {
					// testid
					ps_2.setString(2, "teid");
					// deviceid
					ps_2.setString(3, "deviceid");
					// operator
					ps_2.setString(5, "operator");
					ps_2.setString(6, "1");
					ps_2.setString(9, "unit");
					ps_2.setString(10, "name");
					ps_2.setString(11, "name");
				}
				ps_2.executeUpdate();
				close(ps_2);
				updateSample(ri.sampleNo);
				break;
			case 1: // 更新,先保存，再更新
				ps_2 = update.prepareStatement(NewSqls.insert_TestResult_log);
				ps_2.setString(1, ri.sampleNo);
				// testresult
				ps_2.setString(7, ri.result);
				// teststatus
				ps_2.setInt(8, 4); // 设定为4
				// 时间 measuretime;
				ps_2.setTimestamp(4, new Timestamp(ri.measuretime.getTime()));
				if (td != null) {
					// testid
					ps_2.setString(2, td.index_id);
					// deviceid
					ps_2.setString(3, DBConfig.deviceid);
					// operator
					ps_2.setString(5, DBConfig.deviceid);
					ps_2.setString(6, td.sample_from);
					ps_2.setString(9, td.unit);
					ps_2.setString(10, td.name);
					ps_2.setString(11, td.method);
				} else {
					// testid
					ps_2.setString(2, "teid");
					// deviceid
					ps_2.setString(3, "deviceid");
					// operator
					ps_2.setString(5, "operator");
					ps_2.setString(6, "1");
					ps_2.setString(9, "unit");
					ps_2.setString(10, "name");
					ps_2.setString(11, "name");
				}
				ps_2.executeUpdate();
				close(ps_2);

				// -------------------------------------------
				ps_2 = update.prepareStatement(NewSqls.update_TestResult);
				ps_2.setString(1, ri.result);
				ps_2.setTimestamp(3, new Timestamp(ri.measuretime.getTime()));
				ps_2.setString(5, ri.sampleNo);
				if (td != null) {
					ps_2.setString(3, td.instrument);
					// operator
					ps_2.setString(5, td.instrument);
					ps_2.setString(6, td.index_id);
				} else {
					ps_2.setString(2, "device");
					ps_2.setString(4, "instrument");
					ps_2.setString(6, "teid");
				}
				ps_2.executeUpdate();
				close(ps_2);
				updateSample(ri.sampleNo);
				break;
			case 2: //
				System.out.println("数据已审核，无需插入！");
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateSample(String ybbh) {
		openUpdate();
		int sampleStatus = 0;
		try {
			ps_2 = update.prepareStatement(NewSqls.has_l_sample_update);
			ps_2.setString(1, ybbh);
			rs_2 = ps_2.executeQuery();
			while (rs_2.next()) {
				sampleStatus = rs_2.getInt("samplestatus");
				System.out.println("samplestatus" + sampleStatus + "------");
			}
			close(rs_2);
			close(ps_2);

			if (sampleStatus < 4) {
				ps_2 = update.prepareStatement(NewSqls.get_checker);
				ps_2.setString(1, DBConfig.deviceid);
				ps_2.setString(2, DBConfig.segment);
				rs_2 = ps_2.executeQuery();
				String tester = "";
				while (rs_2.next()) {
					tester = rs_2.getString("tester");
				}
				close(rs_2);
				close(ps_2);

				ps_2 = update.prepareStatement(NewSqls.update_sample);
				ps_2.setInt(1, 0);
				ps_2.setInt(2, 4);
				ps_2.setString(3, tester);
				ps_2.setString(4, ybbh);
				ps_2.executeUpdate();

				System.out.println("-------update sample status ----成功---");
				close(ps_2);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void openUpdate() {
		try {
			if (update == null || update.isClosed()) {
				Class.forName(DBConfig.driverName);
				update = DriverManager.getConnection(dbUrl, DBConfig.userName,
						DBConfig.userPwd);
				System.out.println("update open!");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void openQuery() {
		try {
			if (query == null || query.isClosed()) {
				Class.forName(DBConfig.driverName);
				System.out.println(dbUrl);
				query = DriverManager.getConnection(dbUrl, DBConfig.userName,
						DBConfig.userPwd);
				System.out.println("query open!");
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public static void openMonitor() {
		try {
			if (monitor == null || monitor.isClosed()) {
				Class.forName(DBConfig.driverName);
				monitor = DriverManager.getConnection(dbUrl, DBConfig.userName,
						DBConfig.userPwd);
				System.out.println("monitor open");
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
				System.out.println("update close");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
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

	public static void closeMonitor() {
		try {
			if (monitor != null && !monitor.isClosed()) {
				monitor.close();
				monitor = null;
			}
			System.out.println("monitor close");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
