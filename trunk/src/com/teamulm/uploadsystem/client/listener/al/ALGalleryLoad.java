package com.teamulm.uploadsystem.client.listener.al;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import com.teamulm.uploadsystem.client.layout.MainWindow;

public class ALGalleryLoad implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent arg0) {
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
