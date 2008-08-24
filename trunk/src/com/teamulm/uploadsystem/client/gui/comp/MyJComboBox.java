package com.teamulm.uploadsystem.client.gui.comp;

import java.awt.Dimension;

import javax.swing.JComboBox;

import com.teamulm.uploadsystem.client.Helper;
import com.teamulm.uploadsystem.client.gui.MainWindow;

@SuppressWarnings("serial")
public class MyJComboBox extends JComboBox {

	public MyJComboBox() {
	}

	public String getSelectedLoc() {
		return (String) this.getSelectedItem();
	}

	public void setLocations(String fileName) {
		String[] loc = Helper.getInstance().readFileData(fileName, true);
		if (loc != null) {
			this.setLocations(loc);
		} else {
			MainWindow.getInstance().addStatusLine(
					"Locationsdatei nicht gefunden");
		}
	}
	
	public void setLocations(byte[] data) {
		String[] loc = Helper.getInstance().readFileData(data, true);
		if (loc != null) {
			this.setLocations(loc);
		} else {
			MainWindow.getInstance().addStatusLine(
					"Fehler im Locationsarray");
		}
	}

	private void setLocations(String[] locations) {
		this.removeAllItems();
		this.addItem("    -- Bitte wählen --");
		for (String element : locations)
			this.addItem(element);

	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(135, 20);
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(135, 20);
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(135, 20);
	}
}
