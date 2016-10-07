package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import cons.Constant;

public class CommonUtil {

	public static boolean isRunning(String processName) {
		BufferedReader bufferedReader = null;
		try {
			Process proc = Runtime.getRuntime().exec(
					"tasklist /FI \"IMAGENAME eq " + processName + "\"");
			bufferedReader = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				log(line+"-----", true);
				if (line.contains(processName)) // 判断是否存在
				{
					return true;
				}
			}
			return false;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	public static void createDirs(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public static boolean deleteFile(String fileName) {
		File file = new File(fileName);
		// 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
		if (file.exists() && file.isFile()) {
			if (file.delete()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

	public static String getPastDate(Date date, int n) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(calendar.DATE, n);
		date = calendar.getTime();
		String dateString = formatter.format(date);
		return dateString;
	}

	public static boolean Checksum(String text) {
		int car = text.indexOf(String.valueOf(Constant.car));
		boolean is_check = CommonUtil.getBCC(text.substring(1, car - 1)) == text
				.charAt(car - 1);
		return is_check;
	}

	public static String getBinarySum(String str) {
		byte[] bytes = null;
		bytes = str.getBytes();
		int sum = 0;
		int a = 0;
		for (int i = 0; i < bytes.length; i++) {
			a = bytes[i];
			sum = sum + a;
		}
		sum = sum % 256;
		int check1 = sum / 16; // 高四位
		int check2 = sum % 16; // 低四位
		String check = Integer.toHexString(check1).toUpperCase()
				+ Integer.toHexString(check2).toUpperCase();
		return check;
	}

	public static int getBCC(String data) {
		char[] charArray = data.toCharArray();
		int i, j;
		for (i = j = 0; i < charArray.length; i++) {
			j = j + (int) charArray[i];
		}
		j = 64 - (j % 64);
		if (j < 32) {
			j = j + 64;
		}
		return j;
	}

	public int checkBCC(String data) {
		char[] charArray = data.toCharArray();
		int i, j;
		for (i = j = 0; i < charArray.length; i++) {
			j = j + (int) charArray[i];
		}
		return j % 64;
	}

	public static void log(String text, boolean append) {
		CommonUtil.write2File(text, Constant.log_ad, append, true);
	}

	public static Executor getThreadPool() {
		Executor threadPool = null;
		if (threadPool == null) {
			threadPool = Executors.newSingleThreadExecutor();
		}
		return threadPool;
	}

	public static boolean isNumeric(String str) {
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static String Hex2String(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(
						s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, "US-ASCII"); // US-ASCII
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}

	public static String Bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

	public static String hexString2BinaryString(String hexString) {
		if (hexString == null || hexString.length() % 2 != 0)
			return null;
		String bString = "", tmp;
		for (int i = 0; i < hexString.length(); i++) {
			tmp = "0000"
					+ Integer.toBinaryString(Integer.parseInt(
							hexString.substring(i, i + 1), 16));
			bString += tmp.substring(tmp.length() - 4);
		}
		return bString;
	}

	public static String getCurrentDateTime(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss-SSS");
		return df.format(date);
	}

	public static String getDateTime(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("YYYYMMddHHmmss");
		return df.format(date);
	}

	// US-ASCII
	public static void write2File(String str, String path, boolean append,
			boolean withTime) {
		try {
			File file = new File(path);
			File parentFile = file.getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream out = new FileOutputStream(file, append);
			StringBuffer sb = new StringBuffer();
			if (withTime) {
				sb.append(getCurrentDateTime(new Date()) + " ");
			}
			sb.append(str + "\r\n");
			out.write(sb.toString().getBytes("US-ASCII"));// 注意需要转换对应的字符集
			out.close();
		} catch (IOException ex) {
			System.out.println(ex.getStackTrace());
		}
	}

	public static String readFromFile(String path) {
		StringBuffer sb = new StringBuffer();
		String tempstr = null;
		try {
			File file = new File(path);
			if (!file.exists())
				throw new FileNotFoundException();
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			while ((tempstr = br.readLine()) != null)
				sb.append(tempstr);
		} catch (IOException ex) {
			System.out.println(ex.getStackTrace());
		}
		return sb.toString();
	}

	public static <T extends Object> ArrayList<T>[] deleteFromArr(
			ArrayList<T> list, int start, int offSet) {
		ArrayList<T>[] lists = new ArrayList[2];
		ArrayList<T> arrayList = new ArrayList<T>();
		for (int i = start; i < start + offSet; i++) {
			arrayList.add(list.get(i));
		}
		for (int i = start + offSet - 1; i >= start; i--) {
			list.remove(list.get(i));
		}
		lists[0] = list; // 移除后的集合
		lists[1] = arrayList; // 被称除的集合
		return lists;
	}
}
