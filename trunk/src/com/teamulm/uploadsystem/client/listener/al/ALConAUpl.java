/* ALConAUpl.java
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

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.gui.MainWindow;
import com.teamulm.uploadsystem.client.gui.comp.UserPassDialog;
import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;

public class ALConAUpl implements ActionListener {

	private File[] files = null;

	private static final Logger log = Logger.getLogger(ALConAUpl.class);

	public void actionPerformed(ActionEvent e) {

		this.files = MainWindow.getInstance().getFileList().getFiles();
		if (null == this.files || this.files.length == 0) {
			MainWindow.getInstance().addStatusLine("Keine Dateien ausgewählt.");
			return;
		} else if (null == MainWindow.getInstance().getGallery()
				|| MainWindow.getInstance().getGallery().getTitle().length() < 1) {
			MainWindow.getInstance().addStatusLine("Bitte Titel angeben.");
			return;
		} else if (null == MainWindow.getInstance().getGallery()
				|| MainWindow.getInstance().getGallery().getDesc().length() < 1) {
			MainWindow.getInstance().addStatusLine(
					"Bitte Beschreibung angeben.");
			return;
		}
		if (MainWindow.getInstance().getDateEditor().isToday()) {
			int retVal = this.todayDialog();
			if ((JOptionPane.NO_OPTION == retVal || JOptionPane.CLOSED_OPTION == retVal)) {
				MainWindow.getInstance().addStatusLine("Abgebrochen.");
				return;
			}
		}
		if (!TrmEngine.getInstance().isConnected()) {
			if (!TrmEngine.getInstance().connect()) {
				MainWindow.getInstance().addStatusLine(
						"Konnte keine Verbindung herstellen");
			}
		}
		if (!TrmEngine.getInstance().isLoggedIn()) {
			UserPassDialog dialog = new UserPassDialog();
			int userSaidValue = dialog.passDialog();
			if (JOptionPane.NO_OPTION == userSaidValue
					|| JOptionPane.CLOSED_OPTION == userSaidValue) {
				MainWindow.getInstance().addStatusLine("Abgebrochen.");
				return;
			}
			log.debug("starte upload " + userSaidValue);
			if (!TrmEngine.getInstance().login(dialog.getUser(),
					dialog.getPass())) {
				MainWindow.getInstance().addStatusLine(
						"Username oder Passwort falsch.");
				return;
			}
		}

		if (!TrmEngine.getInstance().lockLocation(
				MainWindow.getInstance().getGallery())) {
			return;
		}

		TrmEngine.getInstance().setFiles(this.files);
		TrmEngine.getInstance().setGallery(
				MainWindow.getInstance().getGallery());
		TrmEngine.getInstance().start();
	}

	private int todayDialog() {
		Object[] options = { "Ja", "Nein" };
		return JOptionPane.showOptionDialog(MainWindow.getInstance(),
				"War das Event wirklich heute?", "Datum...?",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				options, options[1]);
	}
}
