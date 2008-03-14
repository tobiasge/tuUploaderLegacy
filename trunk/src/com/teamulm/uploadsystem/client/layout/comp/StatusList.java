package com.teamulm.uploadsystem.client.layout.comp;

import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class StatusList extends JList {
	private Vector<String> addToList;

	private DefaultListModel status;

	private Timer redrawTimer;

	private RedrawAction redrawAction;

	public StatusList() {
		super();
		this.addToList = new Vector<String>();
		this.status = new DefaultListModel();
		this.setModel(this.status);
		this.redrawAction = new RedrawAction();
		this.redrawTimer = new Timer(300, this.redrawAction);
		this.redrawTimer.setRepeats(true);
		this.redrawTimer.start();
		this.setEnabled(false);
		this.addStatusLine("Copyright by ibTEC Team-Ulm GbR");
	}

	public void addStatusLine(String text) {
		this.addToList.add(text);
	}

	private class RedrawAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (StatusList.this.addToList.size() > 0) {
				StatusList.this.status.addElement(StatusList.this.addToList
						.remove(0));
				StatusList.this.ensureIndexIsVisible(StatusList.this.status
						.getSize() - 1);
			}
		}
	}
}
