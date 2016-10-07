package oracle_1;

public class Device {
	
	private String id;
	private String chname;
	private String comport;
	private String baudrate;
	private String dataBit;
	private String stopBit;
	private String parity;
	private String lab;
	
	
	
	public String getLab() {
		return lab;
	}

	public void setLab(String lab) {
		this.lab = lab;
	}

	@Override
	public String toString() {
		return  comport+","+baudrate+","+dataBit+","+stopBit+","+parity;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return chname;
	}
	
	public void setName(String name) {
		this.chname = name;
	}
	
	public String getComport() {
		return comport;
	}
	
	public void setComport(String comport) {
		this.comport = comport;
	}
	
	public String getBaudrate() {
		return baudrate;
	}
	
	public void setBaudrate(String baudrate) {
		this.baudrate = baudrate;
	}
	
	public String getDataBit() {
		return dataBit;
	}
	
	public void setDataBit(String dataBit) {
		this.dataBit = dataBit;
	}
	
	public String getStopBit() {
		return stopBit;
	}
	
	public void setStopBit(String stopBit) {
		this.stopBit = stopBit;
	}
	
	public String getParity() {
		return parity;
	}
	
	public void setParity(String parity) {
		this.parity = parity;
	}
}
