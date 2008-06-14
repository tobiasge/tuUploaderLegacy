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

	public void setLocationsFile(String fileName) {
		String[] loc = Helper.getInstance().readLocFile(fileName, true);
		if (loc != null) {
			this.removeAllItems();
			this.addItem("    -- Bitte wählen --");
			for (String element : loc)
				this.addItem(element);
		} else {
			MainWindow.getInstance().addStatusLine(
					"Locationsdatei nicht gefunden");
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(140, 20);
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(140, 20);
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(140, 20);
	}
}
