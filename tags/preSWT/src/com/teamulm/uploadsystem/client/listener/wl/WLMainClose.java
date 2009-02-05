/*
 * Created on 26.04.2005
 */
package com.teamulm.uploadsystem.client.listener.wl;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;

public class WLMainClose extends WindowAdapter {

	private static final Logger log = Logger.getLogger(WLMainClose.class);

	@Override
	public void windowClosing(WindowEvent arg0) {
		Thread tmpThread = null;
		if (TrmEngine.getInstance().isConnected()) {

			tmpThread = new Thread("Logout") {
				@Override
				public void run() {
					log.info("Logging out");
					TrmEngine.getInstance().disconnect();
				}
			};
			tmpThread.start();

		}

		log.info("Programm terminating");
		log.info("--------------------");
		try {
			tmpThread.join();
		} catch (Exception e) {
		}
		System.exit(0);
	}
}
