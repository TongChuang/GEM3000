package ui;

import gem3000.Operator;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import oracle.NewDB;
import oracle.OldDB;

public class ToTrayIcon extends JFrame implements ActionListener,
		WindowListener {
	final int changeTime=10*60*1000;
	private static final long serialVersionUID = 1L;
	private PopupMenu pop;
	private MenuItem open, close;
	private TrayIcon trayicon;
	public JTextField jt_net;
	public final static DateButton dateButton = new DateButton();
	// 动态显示数据
	public JTextField jt_data;
	Font textFont = new Font("宋体", Font.PLAIN, 12);


	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	TimerTask task1 = new TimerTask() {
		@Override
		public void run() {
			if (dateButton.getText().split(" ")[0].equals(sdf
					.format(new Date()).split(" ")[0])) {
				dateButton.setText(sdf.format(new Date()));
			} else {
				try {
					Thread.sleep(changeTime);
					dateButton.setText(sdf.format(new Date()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	
	private ToTrayIcon() {

	}

	static ToTrayIcon trayIcon;

	public static ToTrayIcon getTray() {
		if (trayIcon == null) {
			trayIcon = new ToTrayIcon();
		}
		return trayIcon;
	}

	public void init(String frameTile, String labelText) {
		setTitle(frameTile);
		initComponents(labelText);
		addWindowListener(this);
	}

	private void initComponents(String text) {
		Image icon = getToolkit().getImage(
				getClass().getResource("/images/icon2.jpg")).getScaledInstance(
				15, 15, Image.SCALE_DEFAULT);
		setIconImage(icon);

		JLabel jl = new JLabel(text, SwingUtilities.CENTER);
		Font font = new Font("宋体", Font.BOLD, 18);
		jl.setFont(font);
		jl.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 0));
		this.add(jl, BorderLayout.NORTH);

		font = new Font("宋体", Font.PLAIN, 14);

		JLabel j_netstatus = new JLabel("参 数 状 态:", SwingConstants.RIGHT);
		j_netstatus.setFont(font);

		JLabel jl_data = new JLabel("当 前 数 据:", SwingConstants.RIGHT);
		jl_data.setFont(font);

		JLabel j_date = new JLabel("日 期 选 择:", SwingConstants.RIGHT);
		j_date.setFont(font);
		
		JPanel jp_center_left = new JPanel();    
		jp_center_left.setBorder(BorderFactory
				.createEmptyBorder(10, 10, 10, 10));
		jp_center_left.setLayout(new GridLayout(3, 1));
		jp_center_left.add(j_netstatus);
		jp_center_left.add(jl_data);
		jp_center_left.add(j_date);
		
		jt_net = new JTextField(SwingConstants.LEFT);
		jt_net.setFont(textFont);
		jt_net.setForeground(Color.RED);
		jt_data = new JTextField();
		jt_data.setFont(textFont);
		jt_data.setForeground(Color.RED);
		

		dateButton.setForeground(Color.BLUE);
		dateButton.setFont(new Font("宋体", Font.BOLD, 12));
		dateButton.setBackground(Color.RED);
		dateButton.setText(sdf.format(new Date()));
	    new Timer().schedule(task1, 0, 1000);
		
		JPanel jp_center_right = new JPanel();
		jp_center_right.setLayout(new GridLayout(3, 1));
		jp_center_right.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
				30));
		jp_center_right.add(jt_net);
		jp_center_right.add(jt_data);
        jp_center_right.add(dateButton);
		

		JPanel jp_center = new JPanel();
		jp_center.setLayout(new GridLayout(1, 2));
		jp_center.setBorder(BorderFactory.createEmptyBorder(5, 0, 20, 10));
		jp_center.add(jp_center_left);
		jp_center.add(jp_center_right);

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.add(jp_center);
		this.setVisible(true);
		this.setSize(440, 185);
		this.setResizable(true);
		this.setLocationRelativeTo(null);

		pop = new PopupMenu();
		open = new MenuItem("Open");
		open.addActionListener(this);
		close = new MenuItem("Close");
		close.addActionListener(this);
		pop.add(open);
		pop.add(close);

		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			trayicon = new TrayIcon(icon, "通讯程序", pop);
			trayicon.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						if (getExtendedState() == JFrame.ICONIFIED) {
							openFrame();// 还原窗口
						} else {
							setExtendedState(JFrame.ICONIFIED);
						}
					}
				}

				public void mouseEntered(MouseEvent e) {

				}

				public void mouseExited(MouseEvent e) {

				}

				public void mousePressed(MouseEvent e) {

				}

				public void mouseReleased(MouseEvent e) {

				}

			});

			try {
				tray.add(trayicon);
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == open) {
			openFrame();
		}
		if (e.getSource() == close) {
		    Operator.getOperator().close();
			System.out.println("release the port");	
			NewDB.closeQuery();
			NewDB.closeQuery();
			NewDB.closeQuery();
			OldDB.closeQuery();
			OldDB.closeQuery();
			OldDB.closeQuery();
			System.out.println("close the db");
			System.exit(-1);
		}
	}

	public void openFrame() {
		setVisible(true);// 设置为可见
		setAlwaysOnTop(true);// 设置置顶
		setExtendedState(JFrame.NORMAL);
	}

	public void windowActivated(WindowEvent arg0) {

	}

	public void windowClosed(WindowEvent arg0) {

	}

	public void windowClosing(WindowEvent arg0) {

	}

	public void windowDeactivated(WindowEvent arg0) {

	}

	public void windowDeiconified(WindowEvent arg0) {

	}

	// 窗口最小化
	public void windowIconified(WindowEvent arg0) {
		setVisible(true);// 设置为不可见
	}

	public void windowOpened(WindowEvent arg0) {

	}

}
