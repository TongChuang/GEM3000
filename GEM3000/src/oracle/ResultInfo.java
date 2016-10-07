package oracle;

import java.util.Date;

public class ResultInfo {
	// 从lis查询项目时，sampleNo为条码号;保存时，sampleNo为样本编号
	public String sampleNo = "";
	public String channel = ""; // 通道号
	public String result = "";
	public Date measuretime;
	
	public String refLo; // 参考范围低值
	public String refHi; // 参考范围高值
	public String resultFlag; //标注 检验结果 异常情况


	public ResultInfo(String sampleNo, String channel, String result,
			Date measuretime) {
		super();
		this.sampleNo = sampleNo;
		this.channel = channel;
		this.result = result;
		this.measuretime = measuretime;
	}

	public ResultInfo() {

	}

	@Override
	public String toString() {
		return sampleNo.toString() + "," + channel.toString() + "," + result
				+ ",1";
	}
}
