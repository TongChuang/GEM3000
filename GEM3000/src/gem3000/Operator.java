package gem3000;

import gnu.io.SerialPort;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

public class Operator implements Observer {
	Client client = new Client();
	String port = "COM1";
	String rate = "9600";
	String dataBit = "" + SerialPort.DATABITS_8;
	String stopBit = "" + SerialPort.STOPBITS_1;
	String parity = "" + SerialPort.PARITY_NONE;
	int parityInt = SerialPort.PARITY_NONE;
    private static Operator operator;
	private Operator() {
		
	}

	public static Operator getOperator(){
		if (operator==null) {
			operator=new Operator();
		}
		return operator;
	}
	
	ITransmitter transmitter = new ITransmitter() {
		@Override
		public void send(String str) {
			client.sendMsg(str);
		}
	};

	private IBuilder iBuilder = new Collector();

	public void receiver(byte[] bts) {
		try {
			String text = new String(bts, "US-ASCII");
			//System.out.println(text);
			iBuilder.msgController(text,transmitter);
		} catch (UnsupportedEncodingException e) {
			
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		receiver((byte[]) arg);
	}

	
	public void init(String message, String port, String rate, String dataBits,
			String stopBits, String parity){
		HashMap<String, Comparable> params = new HashMap<String, Comparable>();
		params.put(Client.PARAMS_PORT, port.toUpperCase().trim()); // 端口名称
		params.put(Client.PARAMS_RATE, rate.trim()); // 波特率
		params.put(Client.PARAMS_DATABITS, dataBits.trim()); // 数据位
		params.put(Client.PARAMS_STOPBITS, stopBits.trim()); // 停止位
		params.put(Client.PARAMS_PARITY, parity.trim()); // 无奇偶校验
		params.put(Client.PARAMS_TIMEOUT, 1000); // 设备超时时间 1秒
		params.put(Client.PARAMS_DELAY, 10); // 端口数据准备时间
		try {
			client.init(params);
			client.addObserver(this);
			client.sendMsg(message);
		} catch (Exception e) {
		}
	}
	
	public void init(String message) {
		@SuppressWarnings("rawtypes")
		HashMap<String, Comparable> params = new HashMap<String, Comparable>();
		params.put(Client.PARAMS_PORT, port); // 端口名称
		params.put(Client.PARAMS_RATE, rate); // 波特率
		params.put(Client.PARAMS_DATABITS, dataBit); // 数据位
		params.put(Client.PARAMS_STOPBITS, stopBit); // 停止位
		params.put(Client.PARAMS_PARITY, parityInt); // 无奇偶校验
		params.put(Client.PARAMS_TIMEOUT, 1500); // 设备超时时间 1秒
		params.put(Client.PARAMS_DELAY, 1); // 端口数据准备时间 
		try {
			client.init(params);
			client.addObserver(this);
			client.sendMsg(message);
		} catch (Exception e) {
		}
	}

	public void close() {
		if (client.isOpen) {
			client.close();
		}
	}
}
