/* ALRemovePic.java
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

import com.teamulm.uploadsystem.client.gui.MainWindow;



public class ALRemovePic implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		MainWindow.getInstance().getFileList().removeSelectedListPictureData();
	}
}