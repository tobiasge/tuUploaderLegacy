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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.widgets.Display;

import com.teamulm.uploadsystem.client.gui.MainWindow;
import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;

public class TeamUlmUpload {

	public static final String CLIENTCONFFILE = "client.conf"; //$NON-NLS-1$

	public static String logFileName;

	private static String appDataDir;

	private static TeamUlmUpload instance = null;

	private static final Logger log = Logger.getLogger(TeamUlmUpload.class);

	public static TeamUlmUpload getInstance() {
		if (null == TeamUlmUpload.instance)
			TeamUlmUpload.instance = new TeamUlmUpload();
		return TeamUlmUpload.instance;
	}

	public static void main(String[] args) {
		try {
			Properties logConf = new Properties();
			logConf.load(new FileInputStream("client.log4j.properties")); //$NON-NLS-1$
			TeamUlmUpload.logFileName = TeamUlmUpload.getAppDataDir()
				+ logConf.getProperty("log4j.appender.logfile.File", "TeamUlm.log"); //$NON-NLS-1$ //$NON-NLS-2$
			logConf.setProperty("log4j.appender.logfile.File", TeamUlmUpload.logFileName); //$NON-NLS-1$
			PropertyConfigurator.configure(logConf);
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
		}
		TeamUlmUpload.getInstance().start();
	}

	private static String getAppDataDir() {
		String appData;
		if (StringUtils.isBlank(TeamUlmUpload.appDataDir)) {
			// Getting Windows AppData directory
			String appDataRoot = System.getenv("APPDATA"); //$NON-NLS-1$
			// if empty non Windows system, trying Mac/Linux user home directory
			if (StringUtils.isBlank(appDataRoot)) {
				appDataRoot = System.getenv("HOME"); //$NON-NLS-1$
			}
			// empty? Should not be, using local install directory
			if (StringUtils.isBlank(appDataRoot)) {
				TeamUlmUpload.appDataDir = ""; //$NON-NLS-1$
			} else {
				// Setting correct sub directory
				appData = appDataRoot + System.getProperty("file.separator") + ".TUUploader" //$NON-NLS-1$ //$NON-NLS-2$
					+ System.getProperty("file.separator"); //$NON-NLS-1$
				File appDataDir = new File(appData);
				if (!appDataDir.exists()) {
					appDataDir.mkdirs();
				}
				TeamUlmUpload.appDataDir = appData;
			}
		}
		System.out.println("TeamUlmUpload.appDataDir = '" + TeamUlmUpload.appDataDir + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		return TeamUlmUpload.appDataDir;
	}

	private MainWindow mainWindow;

	private TeamUlmUpload() {
		Thread.currentThread().setName("Main"); //$NON-NLS-1$
		OperatingSystemMXBean sysInfo1 = ManagementFactory.getOperatingSystemMXBean();
		MemoryMXBean sysInfo2 = ManagementFactory.getMemoryMXBean();
		log.info("Program Startup - Version " + TrmEngine.VERSION); //$NON-NLS-1$
		log.info("Running on: " + sysInfo1.getName() + " " + sysInfo1.getVersion()); //$NON-NLS-1$ //$NON-NLS-2$
		log.info("Systemtype is " + sysInfo1.getArch() + " working on " + sysInfo1.getAvailableProcessors() //$NON-NLS-1$ //$NON-NLS-2$
			+ " CPU(s)"); //$NON-NLS-1$
		log.info("Memory Usage is: " + sysInfo2.getHeapMemoryUsage()); //$NON-NLS-1$
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

	public MainWindow getMainWindow() {
		return mainWindow;
	}

	public void setMainWindow(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	private void start() {
		this.mainWindow = new MainWindow();
		this.mainWindow.setBlockOnOpen(true);
		this.mainWindow.open();
		Display.getCurrent().dispose();
	}
}
