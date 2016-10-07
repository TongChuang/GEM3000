package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DateButton  extends JButton implements ActionListener{
	private Calendar calendar;
	private DateChooser dateChooser;

	public DateButton() {
		this(Calendar.getInstance());
	}

	public DateButton(Calendar calendar) {
		this.calendar = calendar;
		flushCaption();
		setBorder(null);
		super.addActionListener(this);
	}

	// 覆盖父类的方法，使之无效
	// 使用者不能再给本类的对象添加同样的侦听器
	@Override
	public void addActionListener(ActionListener l) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (dateChooser == null)
			dateChooser = new DateChooser();
		Point p = getLocationOnScreen();
		p.y += getHeight();
		dateChooser.showDateChooser(p);
	}

	// 对外接口
	public Calendar getCalendar() {
		return (Calendar) calendar.clone();
	}

	// 刷新文本显示
	public void flushCaption() {
		if (calendar == null)
			return;
		int year=calendar.get(Calendar.YEAR);
		int month=calendar.get(Calendar.MONTH) + 1;
		int day=calendar.get(Calendar.DAY_OF_MONTH);
		int hour=calendar.get(Calendar.HOUR_OF_DAY);
				
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		String date=""+year+
				"-"+(month > 9 ? month : "0" + month)+
				"-"+(day > 9 ? day : "0" + day)+
				" "+(hour > 9 ? hour : "0" + hour)+
				":"+(minute > 9 ? minute : "0" + minute)+
				":"+(second > 9 ? second : "0" + second);
		setText(date);
	}

	// 内部类，显示日期面板
	private class DateChooser extends JPanel implements ActionListener,
			ChangeListener, WindowListener {

		private int WIDTH = 190, HEIGHT = 184;
		JSpinner spnYear;
		JSpinner spnMonth;
		JSpinner spnHour;
		JSpinner spnMinute;
		JDialog dialog = null;

		int startYear = 1970;
		int endYear = 2050;

		JButton[][] btnDays = new JButton[6][7]; // 日期选择按钮

		DateChooser() {
			JPanel pnlTop = getTopPanel();
			add(pnlTop, BorderLayout.NORTH);
			JPanel pnlBottom = getBottomPanel();
			add(pnlBottom, BorderLayout.CENTER);
			setBorder(new LineBorder(Color.orange, 2));
		}

		private JPanel getTopPanel() {
			int currentYear = calendar.get(Calendar.YEAR);
			int currentMonth = calendar.get(Calendar.MONTH) + 1;
			int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
			int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
			int currentMinute = calendar.get(Calendar.MINUTE);

			JPanel panel = new JPanel();
			panel.setBackground(Color.pink);
			panel.setLayout(new FlowLayout());
			spnYear = new JSpinner(new SpinnerNumberModel(currentYear,
					startYear, endYear, 1));
			spnYear.setEditor(new JSpinner.NumberEditor(spnYear, "####"));
			spnYear.setPreferredSize(new Dimension(48, 20));
			spnYear.addChangeListener(this);
			panel.add(spnYear);
			JLabel lblYear = new JLabel("年");
			lblYear.setForeground(Color.white);
			panel.add(lblYear);

			spnMonth = new JSpinner(new SpinnerNumberModel(currentMonth, 1, 12,
					1));
			spnMonth.setPreferredSize(new Dimension(35, 20));
			spnMonth.addChangeListener(this);
			panel.add(spnMonth);
			JLabel lblMonth = new JLabel("月");
			lblMonth.setForeground(Color.white);
			panel.add(lblMonth);

			spnHour = new JSpinner(
					new SpinnerNumberModel(currentHour, 0, 23, 1));
			spnHour.setPreferredSize(new Dimension(35, 20));
			spnHour.addChangeListener(this);
			panel.add(spnHour);
			JLabel lblHour = new JLabel("时");
			lblHour.setForeground(Color.white);
			panel.add(lblHour);

			spnMinute = new JSpinner(new SpinnerNumberModel(currentMinute, 0,
					59, 1));
			spnMinute.setPreferredSize(new Dimension(35, 20));
			spnMinute.addChangeListener(this);
			// panel.add(spnMinute);

			return panel;
		}

		private JPanel getBottomPanel() {
			String columns[] = { "日", "一", "二", "三", "四", "五", "六" };
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(7, 7));
			panel.setBackground(Color.white);
			panel.setFont(new Font("宋体", Font.PLAIN, 14));
			for (int i = 0; i < 7; i++) {
				JLabel cell = new JLabel(" " + columns[i]);
				if (i == 0 || i == 6) {
					cell.setForeground(Color.red);
				} else {
					cell.setForeground(Color.blue);
				}
				cell.setFont(new Font("宋体", Font.PLAIN, 17));
				panel.add(cell);
			}

			int actionCommandId = 0;
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 7; j++) {
					JButton button = new JButton();
					button.setBorder(null);
					button.setActionCommand(String.valueOf(actionCommandId));
					button.setHorizontalAlignment(SwingConstants.RIGHT);
					button.addActionListener(this);
					button.setBackground(Color.white);
					if (j == 0 || j == 6)
						button.setForeground(Color.red);
					else
						button.setForeground(Color.black);
					btnDays[i][j] = button;
					panel.add(button);
					actionCommandId++;
				}
			}

			return panel;
		}

		// 显示日期选择面板对话框
		public void showDateChooser(Point position) {
			if (dialog == null)
				dialog = createDialog();
			dialog.setLocation(position);
			flushDateChooser();// 正确显示日期与星期
			setDayColor();
			dialog.setVisible(true);
		}

		private JDialog createDialog() {
			Frame owner = (Frame) SwingUtilities
					.getWindowAncestor(DateButton.this);
			JDialog dialog = new JDialog();// (owner,false);
			dialog.setUndecorated(true);
			dialog.add(this, BorderLayout.CENTER);
			dialog.pack();
			dialog.setSize(WIDTH, HEIGHT);
			dialog.addWindowListener(this);
			return dialog;
		}

		// 使日期按正确的星期顺序排列
		private void flushDateChooser() {
			Calendar c = (Calendar) calendar.clone();
			c.set(Calendar.DAY_OF_MONTH, 1);
			int maxDayNo = c.getActualMaximum(Calendar.DAY_OF_MONTH);
			int dayNo = 2 - c.get(Calendar.DAY_OF_WEEK);
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 7; j++) {
					String caption = "";
					if (dayNo >= 1 && dayNo <= maxDayNo)
						caption = String.valueOf(dayNo);
					btnDays[i][j].setText(caption);
					dayNo++;
				}
			}
		}

		// 设置日期文本颜色
		private void setDayColor() {
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 7; j++) {
					if (btnDays[i][j].getText().length() == 0)
						continue;
					if (j == 0 || j == 6)
						btnDays[i][j].setForeground(Color.red);
					else
						btnDays[i][j].setForeground(Color.black);
					int dayNo = calendar.get(Calendar.DAY_OF_MONTH);
					if (dayNo == Integer.parseInt(btnDays[i][j].getText()))
						btnDays[i][j].setForeground(Color.yellow);
				}
			}
		}

		/*
		 * 处理特殊情况： 比如原来日期是1月31日 改变月份为2月，此时日期会变成3月3日或3月2日 正确做法是：使其变为2月28（29）日
		 */
		private void checkDate() {
			int month1 = ((Integer) spnMonth.getValue()).intValue();
			int month2 = calendar.get(Calendar.MONTH) + 1;
			if (month1 == month2)
				return;
			int dayDifference = 0 - calendar.get(Calendar.DAY_OF_MONTH);
			calendar.add(Calendar.DAY_OF_YEAR, dayDifference);
		}

		@Override
		public void stateChanged(ChangeEvent ce) {
			JSpinner spinner = (JSpinner) ce.getSource();
			Integer number = (Integer) spinner.getValue();
			if (spinner == spnYear) {
				calendar.set(Calendar.YEAR, number.intValue());
				checkDate();
			} else if (spinner == spnMonth) {
				calendar.set(Calendar.MONTH, number.intValue() - 1);
				checkDate();
			} else if (spinner == spnHour) {
				calendar.set(Calendar.HOUR_OF_DAY, number.intValue());
			} else if (spinner == spnMinute) {
				calendar.set(Calendar.MINUTE, number.intValue());
			}
			flushCaption(); // 按日期显示按钮的文本
			flushDateChooser(); // 排列日期，对应星期
			setDayColor(); // 使选择的日期变色，未选择的日期恢复原色
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			JButton button = (JButton) ae.getSource();
			String day = button.getText();
			if (day.length() == 0)
				return;
			int dayNo = Integer.parseInt(day);
			calendar.set(Calendar.DAY_OF_MONTH, dayNo);
			flushCaption();
			setDayColor();
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
			dialog.setVisible(false);
		}

		@Override
		public void windowClosed(WindowEvent e) {
		}

		@Override
		public void windowClosing(WindowEvent e) {
		}

		@Override
		public void windowOpened(WindowEvent e) {
		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowActivated(WindowEvent e) {
		}
	}
}
