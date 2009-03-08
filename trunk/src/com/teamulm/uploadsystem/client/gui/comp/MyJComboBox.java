package com.teamulm.uploadsystem.client.gui.comp;

import java.awt.Dimension;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;

import org.apache.commons.lang.ArrayUtils;

import com.teamulm.uploadsystem.client.Helper;
import com.teamulm.uploadsystem.client.gui.MainWindow;
import com.teamulm.uploadsystem.data.Location;

@SuppressWarnings("serial")
public class MyJComboBox extends JComboBox {

	public MyJComboBox() {
	}

	public String getSelectedLoc() {
		return (String) this.getSelectedItem();
	}

	public void setLocations(List<Location> locations) {
		List<String> locStrings = new ArrayList<String>();
		for (Location loc : locations) {
			locStrings.add(loc.getName());
		}
		Collections.sort(locStrings, Collator.getInstance());
		this.setLocations(locStrings.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
	}

	public void setLocations(byte[] data) {
		String[] loc = Helper.getInstance().readFileData(data, true);
		if (loc != null) {
			this.setLocations(loc);
		} else {
			MainWindow.getInstance().addStatusLine("Fehler im Locationsarray");
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
