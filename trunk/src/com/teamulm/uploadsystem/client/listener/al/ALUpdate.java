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
import com.teamulm.uploadsystem.client.layout.MainWindow;



public class ALUpdate implements ActionListener {

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
			MainWindow.getInstance().getLocations().setLocationsFile(
					"locations.list");
			MainWindow.getInstance().addStatusLine("Locations Update fertig");
		} catch (Exception e2) {
			MainWindow.getInstance()
					.addStatusLine("Konnte Liste nicht updaten");
			Helper.getInstance().systemCrashHandler(e2);
		}
	}
}