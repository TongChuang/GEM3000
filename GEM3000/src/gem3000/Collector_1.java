package gem3000;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executor;

import util.CommonUtil;
import cons.Constant;

//消息的生成和分发
public class Collector_1 implements IBuilder {

	ITransmitter mTransmitter;
	IReactor dh = new DataHandler();
	StringBuilder frameBuffer = new StringBuilder(300);
	Executor pool = CommonUtil.getThreadPool();

	boolean isSender = false;
	boolean isReceiver = true;
	// boolean isBusy = false;
	int maxCount = 6;
	int responeTimeOut = 25 * 1000;
	int responeInterval = 2; // 轮询间隔
	boolean isResponed = true; // 默认仪器已经回复过
	Thread responseTimer = null; // 轮询线程

	String str_temp;
	String lastSend;
	boolean needWait = false;
	String recSignal;

	@Override
	public void msgController(String str, ITransmitter transmitter) {
		if (transmitter != null) {
			mTransmitter = transmitter;
		}
		if (str == null || str.length() < 1) {
			return;
		}
		log("recevier:" + str);
		str_temp = str;

		boolean is_car = false;
		boolean is_lf = false;
//		boolean is_etx=false;
		String per = "";
		for (int i = 0; i < str_temp.length(); i++) {
			per = str_temp.substring(i, i + 1);
			boolean isSignal = per.equals(Constant.is_enq)
					|| per.equals(Constant.is_ack)
					|| per.equals(Constant.is_nak)
					|| per.equals(Constant.is_eot);
			if (isSignal) {
				recSignal = per;
				sessionControl(per); 
				continue;
			}
			frameBuffer.append(per);
			is_car = per.equals(Constant.is_car);
			is_lf = per.equals(Constant.is_ret);
			/*if(per.equals(Constant.is_etx)){
				is_etx=true;
			}*/

			if (is_car) {
				sendPrint(Constant.is_ack);
				mTransmitter.send(Constant.is_ack);
				write2File("send:ACK");
			}
			if (is_lf) {
				String frame = frameBuffer.toString();
				frameBuffer.delete(0, frameBuffer.length()); // 先删除
				receivePrint(frame);
				write2File("received:" + frame); // 保存到resdata
				boolean checkSum = checkSum(
						frame.substring(1, frame.length() - 4),
						frame.substring(frame.length() - 4, frame.length() - 2));
				if (!checkSum) {
					write2File("operator.check:checkSum error!");
					System.out.println("checksum error---------");
					mTransmitter.send(Constant.is_nak);
					write2File("send: NAK");
					return;
				}
				String sub = frame.substring(2, frame.length() - 5); // 掐头去尾;
				messageBuffer.append(sub);
			}
		}
	}

	StringBuilder messageBuffer = new StringBuilder(4096); // 存放消息帧，可能有多帧
	boolean hasDataSend = false;

	public void receivePrint(String text) {
		System.out.println(CommonUtil.getCurrentDateTime(new Date())
				+ " Received---" + text);
	}

	public void sendPrint(String text) {
		System.out.println(CommonUtil.getCurrentDateTime(new Date())
				+ " Send-------" + text);
	}

	private boolean checkSum(String text, String check) {
		boolean right = false;
		String binarySum = CommonUtil.getBinarySum(text);
		System.out.println("--binarySum--" + binarySum);
		if (binarySum.equals(check)) {
			right = true;
		}
		return right;
	}

	private void messageDispatcher(String msg) {
		if (isReceiver) { // 还未收到eot
			return;
		}
		if (msg.contains(Constant.is_car + "R|")) { // 结果解析
			parseResult(msg);
			// isBusy = false;
		} else if (msg.contains(Constant.is_car + "Q|")) {
			hasDataSend = true;
			data = dh.queryData(msg);
			if (data == null || data.length() == 0) {
				hasDataSend = false;
				return;
			}
			msgList = separateFrame(data);
			frameCount = msgList.size();
			mTransmitter.send(Constant.is_enq); // 寻求建立连接
			lastSend = Constant.is_enq;
			write2File("send:ENQ");
			sendPrint(Constant.is_enq);
		}
	}

	private ArrayList<String> msgList = new ArrayList<String>();

	private ArrayList<String> separateFrame(String str) {
		ArrayList<String> arrayList = new ArrayList<String>();
		int length = str.length();
		int frames = length % 233 == 0 ? length / 233 : (length / 233) + 1;
		String packageFrame;
		// System.out.println("frames:" + frames);
		for (int i = 1; i <= frames; i++) {
			int c = i % 8; // 序列号
			if (i == frames) {
				packageFrame = packageFrame(
						str.substring((i - 1) * 233, length) + Constant.is_etx,
						"" + c);
			} else {
				packageFrame = packageFrame(
						str.substring((i - 1) * 233, i * 233) + Constant.is_etb,
						"" + c);
			}
			arrayList.add(Constant.is_stx + packageFrame);
		}
		return arrayList;
	}

	public String packageFrame(String data, String frameHeader) {
		StringBuilder frameSb = new StringBuilder();
		String frameTrailer = CommonUtil.getBinarySum(frameHeader + data)
				+ Constant.is_car + Constant.is_ret;
		frameSb.append(frameHeader).append(data).append(frameTrailer);
		return frameSb.toString();
	}

	private String data;
	private int frameCount = 0;

	private void sessionControl(String msg) {
		if (msg.equals(Constant.is_eot)) {
			write2File("received:EOT"); // 会话结束
			receivePrint(Constant.is_eot);
			if (isSender) {
				needWait = true; // 对方有数据发，下次会话权交给对方
			}
			if (isReceiver) {
				isReceiver = false; // 从接收状态变为无状态
				// isBusy = true; // 有数据要处理
				String message = messageBuffer.toString();
				messageBuffer.delete(0, messageBuffer.length());
				messageDispatcher(message);
			}

		}
		if (msg.equals(Constant.is_enq)) {
			// 被寻求会话，如若空闲，则肯定回答
			receivePrint(Constant.is_enq);
			write2File("received:ENQ");
			if (lastSend != null && lastSend.equals(Constant.is_enq)) { // 有冲突，等20S
				lastSend = "";
				while (hasDataSend) {
					// isBusy = false;
					sleep(10 * 1000);
					if (hasDataSend) {
						mTransmitter.send(Constant.is_enq);
						sendPrint(Constant.is_enq);
						write2File("send:ENQ");
					}
				}
			} else {
				mTransmitter.send(Constant.is_ack);
				isReceiver = true; // 无状态进入接收状态
				sendPrint(Constant.is_ack);
				write2File("send:ACK");
			}
		}

		if (msg.equals(Constant.is_ack)) {
			/*
			 * isResponed = true; terminatePoll(responseTimer);
			 */
			write2File("received:ACK");
			receivePrint(Constant.is_ack);
			if (lastSend != null && lastSend.equals(Constant.is_enq)) {
				isSender = true; // 无状态变为发送者
			}
			if (isSender && frameCount > 0) {
				String text = msgList.get(0);
				msgList.remove(msgList.get(0)); // 从集合中移除
				mTransmitter.send(text);
				sendPrint(text);
				responeMonitor(text); // 响应定时
				frameCount--;
			} else if (isSender && frameCount == 0) {
				mTransmitter.send(Constant.is_eot); // 传输结束
				hasDataSend = false;
				isSender = false; // 释放连接 变无状态
				sendPrint(Constant.is_eot);
				write2File("send:EOT");
			}
		}

		if (msg.equals(Constant.is_nak)) {
			write2File("received:NAK");
			if (lastSend != null && lastSend.equals(Constant.is_enq)) { // 对方很忙,先等
				while (hasDataSend) {
					sleep(10 * 1000);
					mTransmitter.send(Constant.is_enq);
					sendPrint(Constant.is_enq);
				}
			} else {
				// isResponed = true;
				terminatePoll(responseTimer);
				sendPrint(lastSend);
				mTransmitter.send(lastSend); // 重发
				responeMonitor(lastSend);
			}
		}
	}

	private void sleep(final int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void responeMonitor(String str) {
		write2File("send:" + str);
		lastSend = str;
		/*
		 * isResponed = false; responseTimer = new Thread(responseTimerRun); //
		 * 等待响应 responseTimer.start();
		 */
	}

	// 日志
	private void log(final String text) {
		pool.execute(new Runnable() {
			@Override
			public void run() {
				CommonUtil.log(text, true);
			}
		});
	}

	private void terminatePoll(Thread thread) {
		try {
			if (thread != null && thread.isAlive()) {
				thread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void parseResult(String msg) {
		final String text = msg;
		pool.execute(new Runnable() {
			@Override
			public void run() {
				dh.parseMsg(text);
			}
		});
	}

	// 记录原始数据 resdata_ad
	private void write2File(String msg) {
		final String text = msg;
		pool.execute(new Runnable() {
			@Override
			public void run() {
				CommonUtil.write2File(text, Constant.resdata_ad, true, true);
			}
		});
	}

	Runnable responseTimerRun = new Runnable() {
		long beginTime = System.currentTimeMillis();
		int count = 0;
		long checkTime;

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(responeInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				checkTime = System.currentTimeMillis();
				if (!isResponed) {
					if (count <= maxCount) {
						if (checkTime - beginTime >= responeTimeOut) {
							mTransmitter.send(lastSend);
							beginTime = System.currentTimeMillis();
							count++;
							log("repeat send: " + lastSend + "##" + count);
						}
					} else { // 虽然还没有应答， 已经超过重发次数，放弃这条消息
						count = 0;
						write2File("abort the respone!");
						System.out.println("abort the respone!");
						break;
					}
				} else { // 已收到回复用，取消任务；
					count = 0;
					break;
				}
			}
		}
	};

}
