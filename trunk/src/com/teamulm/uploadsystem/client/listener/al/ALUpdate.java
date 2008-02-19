/* ALUpdate.java
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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.teamulm.uploadsystem.client.Helper;
import com.teamulm.uploadsystem.client.layout.comp.MyJComboBox;

public class ALUpdate implements ActionListener {

	private MyJComboBox myBox;

	public ALUpdate(MyJComboBox myBox) {
		this.myBox = myBox;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			URLConnection locationsURL = new URL(
					"http://www.team-ulm.de/fotos/locations.php")
					.openConnection();
			int contentLength = locationsURL.getContentLength();
			byte[] contentByteArray = new byte[contentLength];
			InputStream fromServer = locationsURL.getInputStream();
			int readBytes = fromServer.read(contentByteArray);
			if (readBytes != contentLength)
				throw new Exception("Content not fully read");
			FileOutputStream out = new FileOutputStream("locations.list");
			out.write(contentByteArray);
			out.flush();
			out.close();
			this.myBox.setLocationsFile("locations.list");
		} catch (Exception e2) {

			Helper.getInstance().systemCrashHandler(e2);
		}
	}
}