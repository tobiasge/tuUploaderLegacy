/* ALChoosePic.java
 *
 *******************************************************
 *
 * Beschreibung:
 *
 *
 * Autor: Wolfgang Holoch
 * (C) 2004
 *
 *******************************************************/
package com.teamulm.uploadsystem.client.listener.al;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.teamulm.uploadsystem.client.layout.MainWindow;

public class ALChoosePic implements ActionListener {

	private JFileChooser fc;

	public void actionPerformed(ActionEvent e) {
		this.fc = new JFileChooser();
		this.fc.setDialogTitle("Bilder auswählen");
		this.fc.setDialogType(JFileChooser.OPEN_DIALOG);
		this.fc.removeChoosableFileFilter(this.fc.getAcceptAllFileFilter());
		this.fc.setFileFilter(new JPGFileFilter());
		this.fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		this.fc.setMultiSelectionEnabled(true);
		if (JFileChooser.APPROVE_OPTION == this.fc.showOpenDialog(MainWindow
				.getInstance())) {
			MainWindow.getInstance().getFileList().setListPictureData(
					this.fc.getSelectedFiles());
			if (this.fc.getSelectedFiles().length > 0)
				MainWindow.getInstance().setTMPDir(
						this.fc.getSelectedFiles()[0].getParent()
								+ System.getProperty("file.separator"));
		}
	}

	private class JPGFileFilter extends FileFilter {

		@Override
		public boolean accept(File f) {
			String fileName = f.getName();
			return f.isDirectory() || fileName.endsWith("jpg")
					|| fileName.endsWith("jpeg") || fileName.endsWith("JPG")
					|| fileName.endsWith("JPEG");
		}

		@Override
		public String getDescription() {
			return "*.jpg und *.jpeg Dateien";
		}
	}
}
