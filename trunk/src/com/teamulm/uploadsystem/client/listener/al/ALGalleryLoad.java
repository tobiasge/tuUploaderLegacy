package com.teamulm.uploadsystem.client.listener.al;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.layout.MainWindow;
import com.teamulm.uploadsystem.client.layout.comp.UserPassDialog;
import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;
import com.teamulm.uploadsystem.data.Gallery;

public class ALGalleryLoad implements ActionListener {

	private static final Logger log = Logger.getLogger(ALGalleryLoad.class);

	public void actionPerformed(ActionEvent arg0) {
		log.debug("starting gallery selection");

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

		ArrayList<Gallery> galleryList = TrmEngine.getInstance()
				.getGalleriesFor(
						MainWindow.getInstance().getDateEditor()
								.getDateString());
		log.debug("Found " + galleryList.size() + " galleries");
		new GalleryDialog();
	}

	private class GalleryDialog extends JDialog {

		private static final long serialVersionUID = -9193707556220629559L;

		public GalleryDialog() {
			super(MainWindow.getInstance(), "Galerie auswählen", true);
			this.setPreferredSize(new Dimension(200, 200));
			this.setMinimumSize(new Dimension(300, 200));
			this.setResizable(false);
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			this.setLocation((d.width - getSize().width) / 2,
					(d.height - getSize().height) / 2);
			this.setVisible(true);
		}
	}
}
