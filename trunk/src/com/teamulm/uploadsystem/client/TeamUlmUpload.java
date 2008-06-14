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

import java.io.File;
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

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.teamulm.uploadsystem.client.gui.MainWindow;

public class TeamUlmUpload {

	private static final Logger log = Logger.getLogger(TeamUlmUpload.class);

	public static final String CLIENTCONFFILE = "client.conf";

	public static final String LOGFILE = "TeamUlm.log";

	private static TeamUlmUpload instance = null;

	private static String appDataDir;

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
				Thread.currentThread().setName("EventQueue");
				MainWindow.getInstance();
			}
		});
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
			logConf.setProperty("log4j.appender.logfile.File", TeamUlmUpload
					.getAppDataDir()
					+ logConf.getProperty("log4j.appender.logfile.File",
							"TeamUlm.log"));
			PropertyConfigurator.configure(logConf);
			UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
		}
		System.setProperty("line.separator", "\n");
		TeamUlmUpload.getInstance();
	}

	public static String getAppDataDir() {
		String appData;
		if (null == TeamUlmUpload.appDataDir
				|| TeamUlmUpload.appDataDir.isEmpty()) {
			// Getting Windows AppData directory
			String appDataRoot = System.getenv("appdata");
			// if empty non Windows system, trying Mac/Linux user home directory
			if (appDataRoot.isEmpty()) {
				appDataRoot = System.getenv("home");
			}
			// empty? Should not be, using local install directory
			if (appDataRoot.isEmpty()) {
				TeamUlmUpload.appDataDir = "";
			} else {
				// Setting correct sub directory
				appData = appDataRoot + System.getProperty("file.separator")
						+ ".TUUploader" + System.getProperty("file.separator");
				File appDataDir = new File(appData);
				if (!appDataDir.exists()) {
					appDataDir.mkdirs();
				}
				TeamUlmUpload.appDataDir = appData;
			}
		}
		return TeamUlmUpload.appDataDir;
	}
}
