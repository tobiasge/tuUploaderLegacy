/*
 * Created on 21.04.2005
 */
package com.teamulm.uploadsystem.client.listener.kl;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import com.teamulm.uploadsystem.client.layout.MainWindow;

public class KLEventTitle extends KeyAdapter {

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (KeyEvent.VK_ENTER == arg0.getKeyCode()) {
			MainWindow.getInstance().setFocus("eventDesc");
		}
	}
}
