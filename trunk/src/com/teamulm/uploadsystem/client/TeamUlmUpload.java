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

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.widgets.Display;

import com.teamulm.uploadsystem.client.gui.MainWindow;
import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;

public class TeamUlmUpload {

	private static final Logger log = Logger.getLogger(TeamUlmUpload.class);

	public static final String CLIENTCONFFILE = "client.conf";

	private static TeamUlmUpload instance = null;

	private static String appDataDir;

	public static String logFileName;

	private MainWindow mainWindow;

	// Konstruktor
	private TeamUlmUpload() {
		Thread.currentThread().setName("Main");
		OperatingSystemMXBean sysInfo1 = ManagementFactory.getOperatingSystemMXBean();
		MemoryMXBean sysInfo2 = ManagementFactory.getMemoryMXBean();
		log.info("Program Startup - Version " + TrmEngine.VERSION);
		log.info("Running on: " + sysInfo1.getName() + " " + sysInfo1.getVersion());
		log
			.info("Systemtype is " + sysInfo1.getArch() + " working on " + sysInfo1.getAvailableProcessors()
				+ " CPU(s)");
		log.info("Memory Usage is: " + sysInfo2.getHeapMemoryUsage());

		this.mainWindow = new MainWindow();
		this.mainWindow.setBlockOnOpen(true);
		this.mainWindow.open();
		Display.getCurrent().dispose();
	}

	public static TeamUlmUpload getInstance() {
		if (null == TeamUlmUpload.instance)
			TeamUlmUpload.instance = new TeamUlmUpload();
		return TeamUlmUpload.instance;
	}

	public Properties getClientConf() {
		Properties serverConf = new Properties();
		try {
			serverConf.loadFromXML(new FileInputStream(TeamUlmUpload.CLIENTCONFFILE));
		} catch (IOException e) {
			Helper.getInstance().systemCrashHandler(e);
		}
		return serverConf;
	}

	public static void main(String[] args) {
		try {
			Properties logConf = new Properties();
			logConf.load(new FileInputStream("client.log4j.properties"));
			TeamUlmUpload.logFileName = TeamUlmUpload.getAppDataDir()
				+ logConf.getProperty("log4j.appender.logfile.File", "TeamUlm.log");
			logConf.setProperty("log4j.appender.logfile.File", TeamUlmUpload.logFileName);
			PropertyConfigurator.configure(logConf);
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
		}
		System.setProperty("line.separator", "\n");
		TeamUlmUpload.getInstance();
	}

	public MainWindow getMainWindow() {
		return mainWindow;
	}

	public void setMainWindow(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	public static String getAppDataDir() {
		String appData;
		if (null == TeamUlmUpload.appDataDir || TeamUlmUpload.appDataDir.isEmpty()) {
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
				appData = appDataRoot + System.getProperty("file.separator") + ".TUUploader"
					+ System.getProperty("file.separator");
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
