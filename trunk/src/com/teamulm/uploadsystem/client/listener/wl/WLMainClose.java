/*
 * Created on 26.04.2005
 */
package com.teamulm.uploadsystem.client.listener.wl;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.apache.log4j.Logger;

public class WLMainClose extends WindowAdapter {

	private static final Logger log = Logger.getLogger(WLMainClose.class);

	@Override
	public void windowClosing(WindowEvent arg0) {
		log.info("Programm terminating");
		log.info("--------------------");
		System.exit(0);
	}
}
