package oracle_1;

import java.io.Serializable;
import java.util.Date;

public class ResultInfo implements Cloneable, Serializable {
	// 从lis查询项目时，sampleNo为条码号;保存时，sampleNo为样本编号

	public ResultInfo clone() throws CloneNotSupportedException {
		
		return (ResultInfo)super.clone();
	}

	private String testId="";
	private String channel = ""; // 通道号
	private String result = "";

	private String sampleNo = "";
	private Date measuretime;
	private String sex = "";
	private String age = "";
	private String ageUit = "";
	private String cycle = "";
	
	private String sample_from; // sampletype
	private String name=""; // testname
	private String method="";
	private String unit="";
	
	private String refLo = ""; // 参考范围低值
	private String refHi = ""; // 参考范围高值
	
	private String resultFlag = ""; // 标注 检验结果 异常情况
	private String correctFlag = "3";
	private int isPrint;

	
	
	public int getIsPrint() {
		return isPrint;
	}

	public void setIsPrint(int isPrint) {
		this.isPrint = isPrint;
	}

	public String getSample_from() {
		return sample_from;
	}

	public void setSample_from(String sample_from) {
		this.sample_from = sample_from;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getTestId() {
		return testId;
	}

	public void setTestId(String testId) {
		this.testId = testId;
	}

	public String getAgeUit() {
		return ageUit;
	}

	public void setAgeUit(String ageUit) {
		this.ageUit = ageUit;
	}

	public String getCorrectFlag() {
		return correctFlag;
	}

	public void setCorrectFlag(String correctFlag) {
		this.correctFlag = correctFlag;
	}

	public String getSampleNo() {
		return sampleNo;
	}

	public void setSampleNo(String sampleNo) {
		this.sampleNo = sampleNo;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Date getMeasuretime() {
		return measuretime;
	}

	public void setMeasuretime(Date measuretime) {
		this.measuretime = measuretime;
	}

	public String getRefLo() {
		return refLo;
	}

	public void setRefLo(String refLo) {
		this.refLo = refLo;
	}

	public String getRefHi() {
		return refHi;
	}

	public void setRefHi(String refHi) {
		this.refHi = refHi;
	}

	public String getResultFlag() {
		return resultFlag;
	}

	public void setResultFlag(String resultFlag) {
		this.resultFlag = resultFlag;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getCycle() {
		return cycle;
	}

	public void setCycle(String cycle) {
		this.cycle = cycle;
	}

	@Override
	public String toString() {
		return sampleNo.toString() + "," + channel.toString() + "," + result
				+ ",1";
	}

}
