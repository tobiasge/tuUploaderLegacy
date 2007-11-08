/* TrmEngine.java
 *
 *******************************************************
 *
 * Beschreibung:
 *
 *
 * Autor: Tobias Genannt
 * (C) 2005
 *
 *******************************************************/
package com.teamulm.uploadsystem.client.transmitEngine;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.TeamUlmUpload;
import com.teamulm.uploadsystem.client.layout.MainWindow;
import com.teamulm.uploadsystem.client.layout.comp.MyJProgressBar;

public class TrmEngine extends Thread {

	private static final Logger log = Logger.getLogger(TrmEngine.class);

	private File savePath;

	public static final String VERSION = "4.0";

	private Vector<File> toconvert;

	private Vector<File> totransmit;

	private Transmitter transmit;

	private long totalFiles;

	private long convertedFiles;

	private long transmitedFiles;

	private boolean intern, stopRequested;

	private int startNum;

	private boolean startNumSet;

	private Converter[] converters;

	private String username;

	private String password;

	private boolean passSet;

	private ReentrantLock picTransmitLock, picConvertLock, picNumLock;

	private Condition picToTransmit, startNumSetCond;

	public TrmEngine(File[] toconvert) {
		super();
		this.setName("TrmEngine");
		this.picTransmitLock = new ReentrantLock(false);
		this.picConvertLock = new ReentrantLock(false);
		this.picNumLock = new ReentrantLock(false);
		this.picToTransmit = this.picTransmitLock.newCondition();
		this.startNumSetCond = this.picNumLock.newCondition();
		this.transmitedFiles = this.convertedFiles = 0;
		this.toconvert = new Vector<File>();
		this.totransmit = new Vector<File>();
		this.totalFiles = toconvert.length * 2;
		this.savePath = new File(MainWindow.getInstance().getTMPDir());
		this.startNum = -1;
		this.startNumSet = false;
		this.passSet = false;
		this.stopRequested = false;
		OperatingSystemMXBean sysInfo1 = ManagementFactory
				.getOperatingSystemMXBean();
		this.converters = new Converter[sysInfo1.getAvailableProcessors()];
		for (File fi : toconvert)
			this.toconvert.add(fi);
		this.intern = MainWindow.getInstance().getIntern();
	}

	public File getNextToConvert() {
		File retVal = null;
		this.picConvertLock.lock();
		if (!this.toconvert.isEmpty())
			retVal = this.toconvert.remove(0);
		this.picConvertLock.unlock();
		return retVal;
	}

	public synchronized void setUserPass(String user, String password) {
		if (user.length() == 0 || password.length() == 0)
			return;
		this.username = user;
		this.password = password;
		this.passSet = true;
	}

	private synchronized void reset() {
		int length = this.converters.length;
		for (int i = 0; i < length; i++)
			this.converters[i] = null;
		this.transmit = null;
	}

	public File getNextToTransmit() {
		MainWindow
				.getInstance()
				.setUploadProgress(
						(int) ((MyJProgressBar.MAX / this.totalFiles) * this.transmitedFiles));
		File retVal = null;
		this.picTransmitLock.lock();
		try {
			while (this.totransmit.isEmpty()
					&& this.isThereSomethingToTtansmit()) {
				picToTransmit.await();
			}
			this.transmitedFiles++;
			retVal = this.totransmit.remove(0);
		} catch (Exception e) {
			TeamUlmUpload.getInstance().systemCrashHandler(e);
		} finally {
			this.picTransmitLock.unlock();
		}
		return retVal;
	}

	public synchronized boolean isThereSomethingToTtansmit() {
		return this.totalFiles > this.transmitedFiles;
	}

	public void setToTransmit(File file) {
		this.picTransmitLock.lock();
		this.totransmit.add(file);
		this.convertedFiles++;
		this.picToTransmit.signal();
		this.picTransmitLock.unlock();
		MainWindow
				.getInstance()
				.setConvertProgress(
						(int) ((MyJProgressBar.MAX / this.totalFiles) * this.convertedFiles));
	}

	public void setStartNumber(int start) {
		this.picNumLock.lock();
		this.startNum = start - 1;
		this.startNumSet = true;
		this.startNumSetCond.signal();
		this.picNumLock.unlock();
	}

	public int getNextPicNum() {
		this.picNumLock.lock();
		this.startNum++;
		int retVal = this.startNum;
		this.picNumLock.unlock();
		return retVal;
	}

	public void requestStop() {
		this.stopRequested = true;
	}

	@Override
	public void run() {
		if (this.stopRequested) {
			this.reset();
			return;
		}
		MainWindow.getInstance().addStatusLine(
				"Beginne Verbindung zum Server...");
		this.transmit = new Transmitter(this);
		this.transmit.setPriority(3);
		if (!this.transmit.isConnected()) {
			this.transmit.disconnect();
			return;
		} else if (!this.transmit.verCheck()) {
			MainWindow.getInstance().addStatusLine(
					"Falsche Programmversion: " + TrmEngine.VERSION);
			this.transmit.disconnect();
			return;
		} else if (!this.passSet
				|| !this.transmit.login(this.username, this.password)) {
			MainWindow.getInstance()
					.addStatusLine("User oder Passwort falsch.");
			this.transmit.disconnect();
			return;
		} else if (!this.transmit.setLocation()) {
			this.transmit.disconnect();
			return;
		}
		if (this.intern) {
			MainWindow.getInstance().addStatusLine("Erstelle interne Galerie.");
		}
		if (this.stopRequested) {
			this.reset();
			return;
		}
		for (int i = 0; i < this.converters.length; i++) {
			this.converters[i] = new Converter(this, this.savePath, i);
			this.converters[i].setPriority(3);
		}
		try {
			this.picNumLock.lock();
			while (!this.startNumSet)
				this.startNumSetCond.await();
			this.picNumLock.unlock();
			if (this.stopRequested) {
				this.reset();
				return;
			}
			log.info("Starte Converter.");
			for (Converter con : this.converters)
				con.start();
			log.info("Starte Transmitter.");
			this.transmit.start();
			for (Converter con : this.converters)
				if (con.isAlive())
					con.join();
			this.totalFiles = this.convertedFiles;
			MainWindow.getInstance().setConvertProgress(1000);
			this.picTransmitLock.lock();
			this.picToTransmit.signal();
			this.picTransmitLock.unlock();
			this.transmit.join();
			Thread.sleep(1000);
			this.reset();
			TeamUlmUpload.getInstance().engineKill();
			MainWindow.getInstance().addStatusLine("Aufräumen abgeschlossen.");
			Thread.sleep(10);
			MainWindow.getInstance().addStatusLine(
					"Das Programm kann geschlossen werden.");
			log.info("Beende Transmitter.");
		} catch (Exception e) {
			TeamUlmUpload.getInstance().systemCrashHandler(e);
		}
	}
}
