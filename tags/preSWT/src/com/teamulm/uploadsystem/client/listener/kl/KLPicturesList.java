package com.teamulm.uploadsystem.client.listener.kl;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import com.teamulm.uploadsystem.client.gui.MainWindow;

public class KLPicturesList extends KeyAdapter {

	@Override
	public void keyReleased(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_DELETE) {
			MainWindow.getInstance().getFileList()
					.removeSelectedListPictureData();
		}
	}
}
