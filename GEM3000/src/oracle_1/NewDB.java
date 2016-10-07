package oracle_1;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewDB {
	public static Connection query = null;
	public static Connection update = null;
	public static Connection monitor = null;
	static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	static String dbUrl = DBConfig.jdbc + DBConfig.host;
	static PreparedStatement ps_1 = null, ps_2 = null, ps_3 = null;
	static ResultSet rs_1 = null, rs_2 = null, rs_3 = null;

	static Map<String, String> ylxhMap = new HashMap<String, String>();
	static Map<String, String> testChannelMap = new HashMap<String, String>();
	static Map<String, String> testChannelMap_1 = new HashMap<String, String>();
	static Map<String, TestDescribe> labIndexMap = new HashMap<String, TestDescribe>();
	static Map<String, List<TestReference>> testReferenceMap = new HashMap<String, List<TestReference>>();

	public static void initTestReference(String testIds) {
		openQuery();
		try {
			StringBuilder sbBuilder = new StringBuilder("(");
			if (testIds.contains(",")) {
				String[] split = testIds.split(",");
				for (String str : split) {
					sbBuilder.append("'").append(str).append("'").append(",");
				}
			}
			String st = sbBuilder.toString();
			st = st.substring(0, st.length() - 1) + ")";
			System.out.println("-----" + st);

			ps_1 = query
					.prepareStatement("select * from lab_testreference where testid in "
							+ st + " order by testid, orderno asc");
			rs_1 = ps_1.executeQuery();
			List<TestReference> list = null;
			String testId = "";
			String lastId = "";
			while (rs_1.next()) {
				TestReference tr = new TestReference();
				tr.setAgeHigh(rs_1.getInt("agehigh"));
				tr.setTestId(rs_1.getString("testid"));
				tr.setAgeLow(rs_1.getInt("agelow"));
				tr.setSampleType(rs_1.getString("sampletype"));
				tr.setDirect(rs_1.getInt("direct"));
				tr.setAgeLowUnit(rs_1.getString("agelowunit"));
				tr.setAgeHighUnit(rs_1.getString("agehighunit"));
				tr.setDeviceId(DBConfig.deviceid);
				tr.setReference(rs_1.getString("reference"));
				tr.setOrderNO(rs_1.getInt("orderno"));
				tr.setSex(rs_1.getInt("sex"));

				testId = tr.getTestId();
				if (testId.equals(lastId)) {
					list.add(tr);
				} else {
					if (!lastId.isEmpty()) {
						testReferenceMap.put(lastId, list);
						System.out.println(testId);
						System.out.println(list.size());
					}
					list = new ArrayList<TestReference>();
					list.add(tr);
				}
				lastId = testId;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs_1);
			close(ps_1);
		}
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
		System.out.println("testreferenceMap = " + testReferenceMap.size());

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
			// 20161001ICU087
			if (sampleNo.length() > 3) {
				channels.append(sampleNo.substring(sampleNo.length() - 3)); // 最后保存样本号
			} else {
				channels.append(sampleNo);
			}

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

	// 高，低，和计算值
	public static ResultInfo getReference(ResultInfo ri) { // 参考值
		String value = ri.getResult();
		/*
		 * System.out.println("-----value----" + value);
		 * System.out.println("开始参考范围");
		 */
		if (ri.getAge().isEmpty() && ri.getSex().isEmpty()) { // 还没有录入基本信息
			// System.out.println("没有样本信息");
			if (testReferenceMap.containsKey(ri.getTestId())) {
				// System.out.println("---寻找参考范围---");
				List<TestReference> list = testReferenceMap.get(ri.getTestId());
				for (TestReference tr : list) {
					if (tr.getReference().indexOf("-") >= 0) {
						String[] refArr = tr.getReference().split("-");
						if (refArr.length == 2) {
							ri.setRefLo(refArr[0]);
							ri.setRefHi(refArr[1]);
						} else if (refArr.length == 3) {
							ri.setRefLo("-" + refArr[1]);
							ri.setRefHi(refArr[2]);
						} else {
							ri.setRefLo(tr.getReference());
							ri.setRefHi("");
						}
					} else if (tr.getReference().indexOf(">") == 0) {
						ri.setRefLo(tr.getReference().substring(1));
						ri.setRefHi("");
					} else if (tr.getReference().indexOf("<") == 0) {
						int round = tr.getReference().substring(1).split("[.]")[1]
								.length();
						StringBuilder sb = new StringBuilder("#0.");
						for (int i = 0; i < round; i++) {
							sb.append("0");
						}
						DecimalFormat df = new DecimalFormat(sb.toString());
						ri.setRefLo(df.format(0d));
						ri.setRefHi(tr.getReference().substring(1));
					} else {
						ri.setRefLo(tr.getReference());
						ri.setRefHi("");
					}
					break; // 没有年龄，性别信息时取第一条参考范围
				}
				String reflo = ri.getRefLo();
				try {
					if (value != null && value.length() > 0) {
						if (value.charAt(0) == '.') {
							value = "0" + value;
						}
						if (reflo != null) {
							System.out.println("=====reflo====" + reflo);
							if (reflo.contains(".")
									&& reflo.split("[.]").length > 1) {
								int round = reflo.split("[.]")[1].length();
								StringBuilder sb = new StringBuilder("#0.");
								for (int i = 0; i < round; i++) {
									sb.append("0");
								}
								DecimalFormat df = new DecimalFormat(
										sb.toString());
								value = df.format(Double.parseDouble(value));
							} else {
								if (value.split("[.]")[0].equals("-0")) {
									value = "0";
								} else {
									value = value.split("[.]")[0];
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				/*
				 * ri.setResult(value); System.out.println("----1----" +
				 * ri.getChannel() + "=" + ri.getResult() + "--------");
				 */
			}
		} else {
			System.out.println("有样本信息！");
			if (testReferenceMap.containsKey(ri.getTestId())) {
				Double ageLow = 0d;
				Double ageHigh = 0d;
				Double ageReal = 0d;
				List<TestReference> list = testReferenceMap.get(ri.getTestId());
				for (TestReference tr : list) {
					ageHigh = getAge(String.valueOf(tr.getAgeHigh()),
							String.valueOf(tr.getAgeHighUnit()));
					ageLow = getAge(String.valueOf(tr.getAgeLow()),
							String.valueOf(tr.getAgeLowUnit()));
					ageReal = getAge(ri.getAge(), ri.getAgeUit());
					if ((ri.getSex().equals("" + tr.getSex()) || "3".equals(""
							+ tr.getSex()))
							&& ri.getTestId().equals(tr.getTestId())
							&& ageReal > ageLow && ageReal <= ageHigh) {

						System.out.println("==============" + ageHigh);
						if (tr.getReference().indexOf("-") >= 0) {
							String[] refArr = tr.getReference().split("-");
							if (refArr.length == 2) {
								ri.setRefLo(refArr[0]);
								ri.setRefHi(refArr[1]);
							} else if (refArr.length == 3) {
								ri.setRefLo("-" + refArr[1]);
								ri.setRefHi(refArr[2]);
							} else {
								ri.setRefLo(tr.getReference());
								ri.setRefHi("");
							}
						} else if (tr.getReference().indexOf(">") == 0) {
							ri.setRefLo(tr.getReference().substring(1));
							ri.setRefHi("");
						} else if (tr.getReference().indexOf("<") == 0) {
							int round = tr.getReference().substring(1)
									.split("[.]")[1].length();
							StringBuilder sb = new StringBuilder("#0.");
							for (int i = 0; i < round; i++) {
								sb.append("0");
							}
							DecimalFormat df = new DecimalFormat(sb.toString());
							ri.setRefLo(df.format(0d));
							ri.setRefHi(tr.getReference().substring(1));
						} else {
							ri.setRefLo(tr.getReference());
							ri.setRefHi("");
						}
					}

				}
				String reflo = ri.getRefLo();
				try {
					if (value != null && value.length() > 0) {
						if (value.charAt(0) == '.') {
							value = "0" + value;
						}

						if (reflo != null) {
							System.out.println("=====reflo" + reflo);
							if (reflo.contains(".")
									&& reflo.split("[.]").length > 1) {
								int round = reflo.split("[.]")[1].length();
								StringBuilder sb = new StringBuilder("#0.");
								for (int i = 0; i < round; i++) {
									sb.append("0");
								}
								DecimalFormat df = new DecimalFormat(
										sb.toString());
								// System.out.println("----sb.tostring-------"+sb.toString());
								value = df.format(Double.parseDouble(value));
							} else {
								if (value.split("[.]")[0].equals("-0")) {
									value = "0";
								} else {
									value = value.split("[.]")[0];
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				/*
				 * ri.setResult(value); System.out.println("----2----" +
				 * ri.getChannel() + "=" + ri.getResult() + "--------");
				 */
			}

		}
		return ri;
	}

	public static ResultInfo getResultFlag(ResultInfo ri) {
		TestDescribe index = labIndexMap.get(ri.getTestId());
		String ls_result = ri.getResult();
		String ls_reflo = ri.getRefLo();
		String ls_refhi = ri.getRefHi();
		String resultFlag = ri.getResultFlag();
		char[] flags;
		if (resultFlag != null && resultFlag.length() == 6) {
			flags = ri.getResultFlag().toCharArray();
		} else {
			flags = new char[] { 'A', 'A', 'A', 'A', 'A', 'A' };
		}

		if (index != null) {
			ri.setIsPrint(index.isprint);
		} else {
			ri.setIsPrint(0);
		}

		if (ls_reflo == null || ls_reflo.trim().length() == 0) {
			ls_reflo = "";
			ri.setRefLo(ls_reflo);
		}
		if (ls_refhi == null || ls_refhi.trim().length() == 0) {
			ls_refhi = "";
			ri.setRefHi(ls_refhi);
		}
		if (ls_result == null) {
			ls_result = "";
		}
		if (ls_result.indexOf("<") == 0 || ls_result.indexOf(">") == 0) {
			ls_result = ls_result.substring(1);
		}

		if (isDouble(ls_result) && index != null && isDouble(ls_reflo)) {
			double ld_result = dbl(ls_result);
			if (ld_result < dbl(ls_reflo)) {
				flags[0] = 'C';
			} else if (ld_result > dbl(ls_refhi)) {
				flags[0] = 'B';
			} else {
				flags[0] = 'A';
			}
		} else {
			flags[1] = 'B';
			if (ls_result.indexOf("+") > -1 || ls_result.indexOf("阳") > -1) {
				flags[0] = 'B';
			} else if (ls_result.indexOf("-") > -1
					|| ls_result.indexOf("阴") > -1) {
				flags[0] = 'A';
			} else {
				flags[0] = 'B';
			}
		}
		// 把flags写回resultFlag
		ri.setResultFlag(String.valueOf(flags));
		return ri;
	}

	private static double dbl(String str) {
		try {
			return Double.parseDouble(str);
		} catch (Exception e) {
			return 0;
		}
	}

	private static boolean isDouble(String str) {
		try {
			Double.parseDouble(str);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static void batchSave(ArrayList<ResultInfo> list) {
		String ybbh = null;
		openUpdate();
		ArrayList<ResultInfo> insertList = new ArrayList<ResultInfo>();
		ArrayList<ResultInfo> insertList_log = new ArrayList<ResultInfo>();
		ArrayList<ResultInfo> updateList = new ArrayList<ResultInfo>();
		for (int i = 0; i < list.size(); i++) {
			ResultInfo ri = list.get(i);
			String testId = testChannelMap_1.get(ri.getChannel());
			if (testId == null || testId.length() == 0) {
				continue;
			}
			ri.setTestId(testId);
			TestDescribe td = labIndexMap.get(ri.getTestId());
			if (td != null) {
				ri.setName(td.name);
				ri.setMethod(td.method);
				ri.setUnit(td.unit);
				ri.setIsPrint(td.isprint);
				ri.setSample_from(td.sample_from);
			}
			if (ybbh == null) {
				ybbh = ri.getSampleNo();
			}

			// 状态，是否已经审核，值是否相同等 情况
			String status = getStatus(ri);
			if (status.equals("4")) {
				System.out.println("没有该testid");
				continue;
			} else if (status.equals("3")) {
				System.out.println("该项目与历史值相同，无需要修改");
			} else if (status.equals("2")) {
				System.out.println("该样本已经审核！");
				return;
			} else if (status.equals("0")) { // 插入
				insertList.add(ri);
			} else if (status.equals("5")){
				updateList.add(ri);
			}
		}

		// 批量插入或更新

		if (insertList.size() > 0) {
			try {
				update.setAutoCommit(false);
				ps_2 = update.prepareStatement(NewSqls.insert_TestResult);
				for (int i = 0; i < insertList.size(); i++) {
					ResultInfo ri = insertList.get(i);
					ps_2.setString(1, ri.getSampleNo());
					// testresult
					ps_2.setString(7, ri.getResult());
					// teststatus

					ps_2.setInt(8, ri.getSex().isEmpty() ? -1 : 6); // 有样本信息设为4，否则设为9
					// 时间 measuretime;
					ps_2.setTimestamp(4, new Timestamp(ri.getMeasuretime()
							.getTime()));
					// testid
					ps_2.setString(2, ri.getTestId());
					// deviceid
					ps_2.setString(3, DBConfig.deviceid);
					// operator
					ps_2.setString(5, DBConfig.deviceid);
					ps_2.setString(6, ri.getSample_from());
					ps_2.setString(9, ri.getUnit());
					ps_2.setString(10, ri.getName());
					ps_2.setString(11, ri.getMethod());
					ps_2.setString(12, ri.getCorrectFlag());
					ps_2.setString(13, ri.getRefHi());
					ps_2.setString(14, ri.getRefLo());
					ps_2.setString(15, ri.getResultFlag());
					ps_2.setInt(16, ri.getIsPrint());
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

		// log 表, 插入历史值
		synchronized (log_list) {
			if (log_list.size() > 0) {
				try {
					update.setAutoCommit(false);
					ps_2 = update.prepareStatement(NewSqls.insert_TestResult_log);
					for (int i = 0; i < log_list.size(); i++) {
						ResultLog rl = log_list.get(i);
						ps_2.setString(1, rl.SAMPLENO);
						ps_2.setString(2, rl.TESTID);
						ps_2.setString(3, rl.CORRECTFLAG);
						ps_2.setString(4, rl.DEVICEID);
						ps_2.setTimestamp(5, rl.MEASURETIME);
						
						ps_2.setString(6, rl.OPERATOR);
						ps_2.setString(7, rl.REFHI);
						ps_2.setString(8, rl.REFLO);
						ps_2.setString(9, rl.RESULTFLAG);
						ps_2.setString(10, rl.SAMPLETYPE);
						ps_2.setString(11, rl.TESTRESULT);
						ps_2.setInt(12, rl.TESTSTATUS);
						
						ps_2.setString(13, rl.UNIT);
						ps_2.setInt(14, rl.EDITMARK);
						ps_2.setInt(15, rl.ISPRINT);
						ps_2.setInt(16, rl.CLOUDMARK);
						
						ps_2.setString(17, rl.TESTNAME);
						ps_2.setString(18, rl.METHOD);
						
						
						ps_2.setString(19, rl.LOGIP);
						ps_2.setString(20, rl.LOGGER);
						ps_2.setString(21, rl.LOGOPERATE);
						
						ps_2.addBatch();
					}
					ps_2.executeBatch();
					update.commit();
					System.out.println("写入log!!!");
					ps_2.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					try {
						log_list.clear();          //清空
						update.setAutoCommit(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		

		if (updateList.size() > 0) {
			try {
				update.setAutoCommit(false);
				ps_2 = update.prepareStatement(NewSqls.new_update_result);
				for (int i = 0; i < updateList.size(); i++) {
					ResultInfo ri = updateList.get(i);
					ps_2.setString(1, ri.getResult());
					ps_2.setTimestamp(2, new Timestamp(ri.getMeasuretime()
							.getTime()));
					ps_2.setString(3, ri.getResultFlag());
					ps_2.setString(4, ri.getSampleNo());
					ps_2.setString(5, ri.getTestId());
					ps_2.addBatch();
				}
				ps_2.executeBatch();
				update.commit();

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					ps_2.close();
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

	static ArrayList<ResultLog> log_list = new ArrayList<ResultLog>();
	static ResultLog log = null;

	public static String getStatus(ResultInfo rio) {
		ResultInfo ri = getResultFlag(getReference(rio)); // 计算范围及修正值
		String status = "0";
		String testid = ri.getTestId();
		String oldResult = "";
		if (testid.length() == 0) {
			status = "4";
			return status; // 没有该testid
		}

		try {
			update.setAutoCommit(true);
			ps_2 = update.prepareStatement(NewSqls.hasTestResult_sql);
			ps_2.setString(1, ri.getSampleNo());
			ps_2.setString(2, testid);
			rs_2 = ps_2.executeQuery();
			while (rs_2.next()) {
				// 已审核
				if (rs_2.getInt("TESTSTATUS") >= 7) {
					status = "2";
					return status;
				} else {
					oldResult = rs_2.getString("testresult").trim();
				}
				
				if (oldResult.isEmpty()) {
					return status;
				} else if (!oldResult.equals(ri.getResult())) {
					status = "5";
					log = new ResultLog();
					log.CORRECTFLAG = rs_2.getString("CORRECTFLAG");
					log.CLOUDMARK = rs_2.getInt("CLOUDMARK");
					log.DEVICEID = rs_2.getString("DEVICEID");
					log.EDITMARK = rs_2.getInt("EDITMARK");
					log.HINT = rs_2.getString("HINT");
					log.ISPRINT = rs_2.getInt("ISPRINT");
					log.MEASURETIME = rs_2.getTimestamp("MEASURETIME");
					log.UNIT = rs_2.getString("UNIT");
					log.TESTSTATUS = rs_2.getInt("TESTSTATUS");
					log.TESTRESULT = rs_2.getString("TESTRESULT");
					log.TESTNAME = rs_2.getString("TESTNAME");
					log.TESTID = rs_2.getString("TESTID");
					log.SAMPLETYPE = rs_2.getString("SAMPLETYPE");
					log.SAMPLENO = rs_2.getString("SAMPLENO");
					log.RESULTFLAG = rs_2.getString("RESULTFLAG");
					log.REFHI = rs_2.getString("REFHI");
					log.REFLO = rs_2.getString("REFLO");
					log.METHOD = rs_2.getString("METHOD");
					log.OPERATOR = rs_2.getString("OPERATOR");
	//---------------------------------------------------------------------------------
					log.LOGIP=getIp();
					log.LOGGER="";
					log.LOGOPERATE="重传结果";
					log.LOGTIME= new Timestamp(new Date().getTime());
					log.ID=1;
					
					log_list.add(log);
					return status;
				} else {
					status = "3";
					return status; // 该项目所传的值与历史值相同，无需操作
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs_2);
			close(ps_2);
		}
		return status;
	}

	public static String getIp() {
		String ip = "127.0.0.1";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
			return ip;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ip;
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
				td.isprint = rs_1.getInt("isprint");
				labIndexMap.put(td.index_id, td);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs_1);
			close(ps_1);
		}
		Set<String> keySet = labIndexMap.keySet();
		StringBuilder sb = new StringBuilder();
		for (String testId : keySet) {
			sb.append(testId).append(",");
		}
		initTestReference(sb.toString());
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

	public static Double getAge(String age, String ageUnit) {
		if (ageUnit.equals("岁")) {
			return Double.parseDouble(age);
		} else if (ageUnit.equals("月")) {
			return Double.parseDouble(age) / 12;
		} else if (ageUnit.equals("周")) {
			return Double.parseDouble(age) * 7 / 360;
		} else if (ageUnit.equals("天")) {
			return Double.parseDouble(age) / 360;
		}
		return 0d;
	}

	private static ResultInfo getSampleByNO(String sampleNo) {
		openUpdate();
		ResultInfo ri = new ResultInfo();
		try {
			update.setAutoCommit(true);
			ps_2 = update.prepareStatement(NewSqls.getSampleByNO);
			ps_2.setString(1, sampleNo);
			rs_2 = ps_2.executeQuery();
			while (rs_2.next()) {
				ri.setSampleNo(rs_2.getString("sampleno"));
				ri.setAge(rs_2.getString("age"));
				ri.setAgeUit(rs_2.getString("ageunit"));
				ri.setCycle(rs_2.getString("cycle"));
				ri.setSex(rs_2.getString("sex"));
			}
			System.out.println("sampleNo======" + ri.getSampleNo() + "=====");
			if (ri.getSampleNo().isEmpty()) {
				ri.setSampleNo(sampleNo);
			}
			System.out.println("sampleNo======" + ri.getSampleNo() + "=====");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs_2);
			close(ps_2);
		}
		return ri;
	}

	private static ResultInfo getSampleByCode(String sampleid) {
		openUpdate();
		ResultInfo ri = new ResultInfo();
		String sampleno = null;
		try {
			update.setAutoCommit(true);
			ps_2 = update.prepareStatement(NewSqls.getSampleByCode);
			ps_2.setString(1, sampleid);
			rs_2 = ps_2.executeQuery();
			while (rs_2.next()) {
				sampleno = rs_2.getString("sampleno");
				ri.setSampleNo(rs_2.getString("sampleno"));
				ri.setAge(rs_2.getString("age"));
				ri.setAgeUit(rs_2.getString("ageunit"));
				ri.setCycle(rs_2.getString("cycle"));
				ri.setSex(rs_2.getString("sex"));
			}
			// ----------------------旧系统查询---------------------------------
			if (sampleno == null) {
				System.out.println("该样本不在oracle库中！");
				return OldDB.getSampleNo(sampleid);
			}
			// --------------------------------------------------------------
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs_2);
			close(ps_2);
		}
		return ri;
	}

	// 不管仪器传入的编号还是条码，parseMsg时调用，得到一个标本的编号,周期,年龄，性别，年龄单位
	public static ResultInfo getSampleBaseInfo(String sampleid, String date) {
		String sampleno = null;
		ResultInfo ri = null;
		int length = sampleid.length();
		if (length == 12) {
			ri = getSampleByCode(sampleid);
		} else if (length == 3) {
			sampleno = date + DBConfig.segment + sampleid;
			ri = getSampleByNO(sampleno);
		} else if (length == 2) {
			sampleno = date + DBConfig.segment + "0" + sampleid;
			ri = getSampleByNO(sampleno);
		} else if (length == 1) {
			sampleno = date + DBConfig.segment + "00" + sampleid;
			ri = getSampleByNO(sampleno);
		} else if (length >= 4 && length < 12) {
			sampleno = date + DBConfig.segment + sampleid.substring(length - 3);
			ri = getSampleByNO(sampleno);
		} 
		return ri;
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
				// <id,channel>
				testChannelMap.put(rs_1.getString("testid"),
						rs_1.getString("channel"));
				// <channel,id>
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

			if (sampleStatus <= 6) {
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
				ps_2.setInt(2, 6);
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
