package test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import gem3000.Operator;
import oracle_1.Device;
import oracle_1.NewDB;
import ui.ToTrayIcon;
import util.CommonUtil;
import cons.Constant;

public class Test01 {
	public static void main(String[] args) {
		init();
		ToTrayIcon toTrayIcon = ToTrayIcon.getTray();
		toTrayIcon.init("通讯程序", "GEM3000通讯");
		Device device = NewDB.getDevice("PREMIER300","ICU");
		if (device.getBaudrate() != null) {
			String parity = null;
			if (device.getParity().contains("N")) {
				parity = "0";
			}
			Operator.getOperator().init(Constant.is_ack,
					"COM" + device.getComport(), device.getBaudrate(),
					device.getDataBit(), device.getStopBit(), parity);
		} else {
			Operator.getOperator().init(Constant.is_ack);
		}	
	}

	public static void init() {
		CommonUtil.createDirs(Constant.resdata_ad);
		CommonUtil.write2File("=====Program Start ======", Constant.resdata_ad+getPastDate(new Date(), 0)+".txt",
				true, true);
		CommonUtil.log("", false);
		deletePastFile(10);
	}

	
	static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
	public static void deletePastFile(int n) {
		for (int i = n; i >n/2; i--) {
			CommonUtil.deleteFile(Constant.resdata_ad + getPastDate(new Date(), -i)+ ".txt");
		}
	}
	public static String getPastDate(Date date, int n) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, n);
		date = calendar.getTime();
		String dateString = formatter.format(date);
		return dateString;
	}
}
