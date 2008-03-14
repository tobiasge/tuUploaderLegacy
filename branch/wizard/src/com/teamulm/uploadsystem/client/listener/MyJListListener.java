package com.teamulm.uploadsystem.client.listener;

import javax.swing.DefaultListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.teamulm.uploadsystem.client.layout.MainWindow;

public class MyJListListener implements ListDataListener {

	public void contentsChanged(ListDataEvent arg0) {
		return;
	}

	public void intervalAdded(ListDataEvent arg0) {
		this.updateDisplay(arg0.getSource());
	}

	public void intervalRemoved(ListDataEvent arg0) {
		this.updateDisplay(arg0.getSource());
	}

	private void updateDisplay(Object source) {
		if (!(source instanceof DefaultListModel))
			return;
		int remainingPictures = ((DefaultListModel) source).getSize();
		MainWindow.getInstance().setSelectedPicText(
				"Ausgewählte Bilder (" + remainingPictures + "):");
	}
}
