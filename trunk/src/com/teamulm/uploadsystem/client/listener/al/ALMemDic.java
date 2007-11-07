/* ALMemDic.java
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



public class ALMemDic implements ActionListener {

	private JFileChooser fc;

	public ALMemDic() {
		this.fc = new JFileChooser();
		this.fc.setDialogTitle("Ordner Auswahl");
		this.fc.setDialogType(JFileChooser.OPEN_DIALOG);
		this.fc.removeChoosableFileFilter(this.fc.getAcceptAllFileFilter());
		this.fc.setFileFilter(new DicFilter());
		this.fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		this.fc.setMultiSelectionEnabled(false);
	}

	public void actionPerformed(ActionEvent e) {
		if (this.fc.showOpenDialog(MainWindow.getInstance()) == JFileChooser.APPROVE_OPTION)
			MainWindow.getInstance().setTMPDir(
					this.fc.getSelectedFile().getPath());
	}

	private class DicFilter extends FileFilter {

		@Override
		public boolean accept(File f) {
			return f.isDirectory();
		}

		@Override
		public String getDescription() {
			return "Alle Ordner";
		}
	}
}
