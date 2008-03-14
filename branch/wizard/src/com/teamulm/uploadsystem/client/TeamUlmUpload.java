/* TeamUlmUpload.java
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
package com.teamulm.uploadsystem.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;
import com.teamulm.uploadsystem.client.wizard.UploadWizard;

public class TeamUlmUpload {

	private static final Logger log = Logger.getLogger(TeamUlmUpload.class);

	private TrmEngine trmEngine;

	public static final String CLIENTCONFFILE = "client.conf";

	public static final String LOGFILE = "TeamUlm.log";

	private static TeamUlmUpload instance = null;

	// Konstruktor
	private TeamUlmUpload() {
		OperatingSystemMXBean sysInfo1 = ManagementFactory
				.getOperatingSystemMXBean();
		MemoryMXBean sysInfo2 = ManagementFactory.getMemoryMXBean();
		log.info("Program Startup");
		log.info("Running on: " + sysInfo1.getName() + " "
				+ sysInfo1.getVersion());
		log.info("Systemtype is " + sysInfo1.getArch() + " working on "
				+ sysInfo1.getAvailableProcessors() + " CPU(s)");
		log.info("Memory Usage is: " + sysInfo2.getHeapMemoryUsage());
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new UploadWizard().show();
			}
		});
	}

	public void engineStart() {
		this.trmEngine.start();
	}

	public void engineKill() {
		this.trmEngine = null;
	}

	public static TeamUlmUpload getInstance() {
		if (null == TeamUlmUpload.instance)
			TeamUlmUpload.instance = new TeamUlmUpload();
		return TeamUlmUpload.instance;
	}

	public Properties getClientConf() {
		Properties serverConf = new Properties();
		try {
			serverConf.loadFromXML(new FileInputStream(
					TeamUlmUpload.CLIENTCONFFILE));
		} catch (IOException e) {
			Helper.getInstance().systemCrashHandler(e);
		}
		return serverConf;
	}

	public static void main(String[] args) {
		try {
			Properties logConf = new Properties();
			logConf.load(new FileInputStream("client.log4j.properties"));
			PropertyConfigurator.configure(logConf);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
		}
		System.setProperty("line.separator", "\n");
		TeamUlmUpload.getInstance();
	}
}
