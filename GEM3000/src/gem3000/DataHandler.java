package gem3000;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import oracle.NewDB;
import oracle.ResultInfo;
import ui.ToTrayIcon;
import util.CommonUtil;
import cons.Constant;

//消息的分解和组装
public class DataHandler implements IReactor {
    
	SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	@Override
	public String parseMsg(String text) {
		if (text.contains("Error")) {
			return null;
		}
		if (text.toUpperCase().contains("MISSING")) {
			return null;
		}
		Date meauserTime=null;
		String newYbbh=null;
		String item = null;
		String ybbh = null;
		String jyxm = null;
		String value = null;
		ResultInfo ri=null;
		String order = Constant.is_car + "O|";
		int pos_order = text.indexOf(order);
		System.out.println("pos_order" + pos_order);
		if (pos_order >= 0) {
			int begin = text.indexOf("|", pos_order + order.length() + 1);
			int end = text.indexOf("|", begin + 1);
			ybbh = text.substring(begin + 1, end).trim();
			if (ybbh.contains("^")) {
				String[] split = ybbh.split("\\^");
				ybbh = split[1].trim();
			}
			ybbh=StringFilter(ybbh);
			newYbbh=NewDB.getSampleNoById(ybbh, getDate());
		}
		String[] results = text.split(Constant.is_car + "R\\|");
		String[] split = null;
		String result = null;
		String[] split2 = results[0].split(Constant.is_car + "P\\|")[0].split("\\|");
		try {
			meauserTime = sdFormat.parse(split2[split2.length-1]);
		} catch (ParseException e) {
			meauserTime = new Date();
			e.printStackTrace();
		}
		
		ArrayList<ResultInfo> list=new ArrayList<ResultInfo>();
		for (int i = 1; i < results.length; i++) { // 从1开始
			result = results[i].trim();
			System.out.println("---" + result);
			split = result.split("\\|");
			if (split[1].contains("^")) {
				String[] split3 = split[1].split("\\^");
				jyxm = split3[3];
			}
			value =StringFilter(split[2]);
			if (jyxm.equals("")) {
				
			}
			
			if (value != null && value.length() > 0) {
				if (!jyxm.equals("Temp")) {
					item = ybbh + "," + jyxm + "," + value + ",1";
					ri = new ResultInfo(newYbbh,jyxm,value,meauserTime);
					//NewDB.saveResult(ri);
					list.add(ri);
					System.out.println(ri.toString());
					ToTrayIcon.getTray().jt_data.setText(ri.toString());
					CommonUtil.write2File(item, Constant.data_ad, true, false);
				}
			}
		}
		list.add(new ResultInfo(ybbh, "%FiO2", "30",meauserTime));
		NewDB.batchSave(list);
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String queryData(String text) {

		return text;
	}

	SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
	public String getDate() {
		String date = ToTrayIcon.dateButton.getText().split(" ")[0].replace("-", "").trim();
		if (date==null) {
			date=sdf.format(new Date());
		}
		return date;
	}

	public String StringFilter(String str) throws PatternSyntaxException {
		return Pattern.compile("[\\s\t\r\n, ]")
				.matcher(str).replaceAll("").trim();
	}
	// 查询数据库，得到所要做的项目
	public String getItems(String sampleId, char identifer) {
		return sampleId;
	}
}
