package com.teamulm.uploadsystem.client.layout.comp;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class MyJProgressBar extends JProgressBar {

	public static int MAX = 1000;

	public static int MIN = 0;

	private int progress;

	private float percent;

	private Timer redrawTimer;

	private RedrawAction redrawAction;

	public MyJProgressBar() {
		super(SwingConstants.HORIZONTAL, MyJProgressBar.MIN, MyJProgressBar.MAX);
		this.setStringPainted(true);
		this.redrawAction = new RedrawAction();
		this.reset();
		this.redrawTimer = new Timer(300, this.redrawAction);
		this.redrawTimer.setRepeats(true);
		this.redrawTimer.start();
	}

	public void reset() {
		this.progress = 0;
	}

	public boolean setProgress(int value) {
		if (value <= MyJProgressBar.MAX && value >= MyJProgressBar.MIN) {
			this.progress = value;
			this.percent = (value * 100) / MyJProgressBar.MAX;
			return true;
		}
		return false;
	}

	public int getMIN() {
		return MyJProgressBar.MIN;
	}

	public int getMAX() {
		return MyJProgressBar.MAX;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(300, 20);
	}

	private class RedrawAction extends AbstractAction {
		public void actionPerformed(ActionEvent arg0) {
			MyJProgressBar.this.setValue(MyJProgressBar.this.progress);
			MyJProgressBar.this.setString(MyJProgressBar.this.percent + " %");
		};
	}
}
