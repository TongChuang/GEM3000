package gem3000;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Observable;
import java.util.TooManyListenersException;

import javax.swing.JOptionPane;

import ui.ToTrayIcon;
import util.CommonUtil;

public class Client extends Observable implements Runnable {
	static CommPortIdentifier commPort;
	int delayRead = 10; // 延迟读取
	int numBytes; // buffer中的实际数据字节数
	//private static byte[] readBuffer = new byte[512]; // 4k的buffer空间,缓存串口读入的数据
	@SuppressWarnings("rawtypes")
	static Enumeration portList;
	InputStream inputStream;
	OutputStream outputStream;
	static SerialPort serialPort;
	@SuppressWarnings("rawtypes")
	HashMap<String, Comparable> serialParams;
	Thread readThread;
	// 端口是否打开了
	boolean isOpen = false;
	public static final String PARAMS_DELAY = "delay read"; // 延时等待端口数据准备的时间
	public static final String PARAMS_TIMEOUT = "timeout"; // 超时时间
	public static final String PARAMS_PORT = "port name"; // 端口名称
	public static final String PARAMS_DATABITS = "data bits"; // 数据位
	public static final String PARAMS_STOPBITS = "stop bits"; // 停止位
	public static final String PARAMS_PARITY = "parity"; // 奇偶校验
	public static final String PARAMS_RATE = "rate"; // 波特率

	public boolean isOpen() {
		return isOpen;
	}

	public Client() {
		isOpen = false;
	}

	public void init(
			@SuppressWarnings("rawtypes") HashMap<String, Comparable> params) {
		serialParams = params;
		if (isOpen) {
			close();
		}
		try {
			// 参数初始化
			int timeout = Integer.parseInt(serialParams.get(PARAMS_TIMEOUT)
					.toString());
			int rate = Integer.parseInt(serialParams.get(PARAMS_RATE)
					.toString());
			int dataBits = Integer.parseInt(serialParams.get(PARAMS_DATABITS)
					.toString());
			int stopBits = Integer.parseInt(serialParams.get(PARAMS_STOPBITS)
					.toString());
			int parity = Integer.parseInt(serialParams.get(PARAMS_PARITY)
					.toString());
			delayRead = Integer.parseInt(serialParams.get(PARAMS_DELAY)
					.toString());
			String port = serialParams.get(PARAMS_PORT).toString();
			// 打开端口
			commPort = CommPortIdentifier.getPortIdentifier(port);
			serialPort = (SerialPort) commPort.open("SerialReader", timeout);
			serialPort.setSerialPortParams(rate, dataBits, stopBits, parity);
			
			System.out.println(port+dataBits+stopBits+rate+parity);
			
			serialPort.setInputBufferSize(1024);
			serialPort.setOutputBufferSize(1024);
			inputStream = serialPort.getInputStream();
			if (serialPort != null) {
				serialPort.addEventListener(eventListener);
				serialPort.notifyOnDataAvailable(true);
			}
			isOpen = true;
			ToTrayIcon.getTray().jt_net.setText(port+","+dataBits+","+stopBits+","+rate+","+parity);
		} catch (PortInUseException e) {
			JOptionPane.showMessageDialog(null, "请检查，通讯程序已经打开，请不要重复开户", "警告", JOptionPane.ERROR_MESSAGE); 
			CommonUtil.log("端口" + serialParams.get(PARAMS_PORT).toString()
					+ "已经被占用", true);
			System.exit(-1);

		} catch (TooManyListenersException e) {
			CommonUtil.log("端口" + serialParams.get(PARAMS_PORT).toString()
					+ "监听者过多", true);

		} catch (UnsupportedCommOperationException e) {
			CommonUtil.log("端口操作命令不支持", true);

		} catch (NoSuchPortException e) {
			CommonUtil.log("端口" + serialParams.get(PARAMS_PORT).toString()
					+ "不存在", true);

		} catch (IOException e) {
			CommonUtil.log("打开端口" + serialParams.get(PARAMS_PORT).toString()
					+ "失败", true);
		}
		serialParams.clear();
		readThread = new Thread(this);
		readThread.start();
	}

	public void run() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {

		}
	}

	public void start() {
		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
		}
	}

	public void sendMsg(String message) {
		if (isOpen) {
			if (message != null && message.length() != 0) {
				start();
				run(message);
				//System.out.println("main send:" + message);
			}
		} else {
			CommonUtil.log("SerialReader.senMsg()--- 端口尚未打开", true);
		}
	}

	public void run(String message) {
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
		}
		try {
			if (message != null && message.length() != 0) {
				outputStream.write(message.getBytes()); // 往串口发送数据，是双向通讯的。
			}
		} catch (IOException e) {
		}
	}

	public void close() {
		if (isOpen) {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
				if (serialPort != null) {
					// 注意位置
					serialPort.notifyOnDataAvailable(false);
					serialPort.removeEventListener();
					serialPort.close();
				}
				isOpen = false;
			} catch (IOException ex) {
				// "关闭串口失败";
				CommonUtil.log(serialPort.getName() + "串口关闭失败", true);
			}
		}
	}

	private SerialPortEventListener eventListener = new SerialPortEventListener() {
		@Override
		public void serialEvent(SerialPortEvent event) {
			sleep(delayRead);
			switch (event.getEventType()) {
			case SerialPortEvent.BI: // 10
			case SerialPortEvent.OE: // 7
			case SerialPortEvent.FE: // 9
			case SerialPortEvent.PE: // 8
			case SerialPortEvent.CD: // 6
			case SerialPortEvent.CTS: // 3
			case SerialPortEvent.DSR: // 4
			case SerialPortEvent.RI: // 5
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2
				break;
			case SerialPortEvent.DATA_AVAILABLE: // 1
				//numBytes=0;
				try {
					int size = inputStream.available();
					byte[] bts= new byte[size];
					int readSize=0;
					
					synchronized (this) {		
						while (readSize<size) {
							readSize =readSize+inputStream.read(bts,readSize,size-readSize);	
						}
						System.out.println("readSize= " + readSize);
					}
					String text = new String(bts, "US-ASCII");
					System.out.println(text);
					CommonUtil.log(text, true);
					changeMessage(bts, size);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	};

	
	private void sleep(int time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void changeMessage(byte[] message, int length) {
		setChanged();
		byte[] temp = new byte[length];
		System.arraycopy(message, 0, temp, 0, length);
		notifyObservers(temp);
	}
}
