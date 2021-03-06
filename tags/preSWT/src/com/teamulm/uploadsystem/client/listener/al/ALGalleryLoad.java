package com.teamulm.uploadsystem.client.listener.al;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.gui.GalleryDialog;
import com.teamulm.uploadsystem.client.gui.MainWindow;
import com.teamulm.uploadsystem.client.gui.UserPassDialog;
import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;

public class ALGalleryLoad implements ActionListener {

	private static final Logger log = Logger.getLogger(ALGalleryLoad.class);

	public void actionPerformed(ActionEvent arg0) {
		log.debug("starting gallery selection");

		if (!TrmEngine.getInstance().isConnected()) {
			if (!TrmEngine.getInstance().connect()) {
				MainWindow.getInstance().addStatusLine(
						"Konnte keine Verbindung herstellen");
				return;
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

		new GalleryDialog(MainWindow.getInstance().getDateEditor()
				.getDateString());
	}

}
