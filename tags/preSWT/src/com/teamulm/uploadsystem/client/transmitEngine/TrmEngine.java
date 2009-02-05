package com.teamulm.uploadsystem.client.transmitEngine;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.Helper;
import com.teamulm.uploadsystem.client.gui.MainWindow;
import com.teamulm.uploadsystem.client.gui.comp.MyJProgressBar;
import com.teamulm.uploadsystem.data.Gallery;

public class TrmEngine extends Thread {

	private static final Logger log = Logger.getLogger(TrmEngine.class);

	private static TrmEngine instance;

	public static final String VERSION = "4.6";

	private Vector<File> toconvert;

	private Vector<File> totransmit;

	private Transmitter transmit;

	private long totalFiles;

	private long convertedFiles;

	private long transmitedFiles;

	private boolean stopRequested, loggedIn, connected;

	private int startNum;

	private Converter[] converters;

	private ReentrantLock picTransmitLock, picConvertLock, picNumLock;

	private Condition picToTransmit, startNumSetCond;

	private Gallery gallery;

	public static TrmEngine getInstance() {
		if (null == TrmEngine.instance) {
			TrmEngine.instance = new TrmEngine();
		}
		return TrmEngine.instance;
	}

	public static void kill() {
		if (null != TrmEngine.instance) {
			TrmEngine.instance.requestStop();
			TrmEngine.instance = null;
		}
	}

	public Gallery getGallery() {
		return gallery;
	}

	private TrmEngine() {
		super();
		this.setName("TrmEngine");
		this.picTransmitLock = new ReentrantLock(false);
		this.picConvertLock = new ReentrantLock(false);
		this.picNumLock = new ReentrantLock(false);
		this.picToTransmit = this.picTransmitLock.newCondition();
		this.startNumSetCond = this.picNumLock.newCondition();
		this.transmitedFiles = this.convertedFiles = 0;
		this.totransmit = new Vector<File>();
		this.toconvert = new Vector<File>();
		this.startNum = -1;
		this.stopRequested = false;
		this.loggedIn = false;
		this.connected = false;
		OperatingSystemMXBean sysInfo1 = ManagementFactory.getOperatingSystemMXBean();
		this.converters = new Converter[sysInfo1.getAvailableProcessors()];
	}

	public File getNextToConvert() {
		File retVal = null;
		this.picConvertLock.lock();
		if (!this.toconvert.isEmpty())
			retVal = this.toconvert.remove(0);
		this.picConvertLock.unlock();
		return retVal;
	}

	private synchronized void reset() {
		int length = this.converters.length;
		for (int i = 0; i < length; i++)
			this.converters[i] = null;
		this.transmit = null;
		TrmEngine.instance = null;
	}

	protected File getNextToTransmit() {
		File retVal = null;
		this.picTransmitLock.lock();
		try {
			while (this.totransmit.isEmpty() && this.isThereSomethingToTtansmit()) {
				picToTransmit.await();
			}
			this.transmitedFiles++;
			retVal = this.totransmit.remove(0);
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
		} finally {
			this.picTransmitLock.unlock();
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainWindow.getInstance().setUploadProgress(
						(int) ((MyJProgressBar.MAX / TrmEngine.this.totalFiles) * TrmEngine.this.transmitedFiles));
			};
		});

		return retVal;
	}

	protected synchronized boolean isThereSomethingToTtansmit() {
		return this.totalFiles > this.transmitedFiles;
	}

	protected void setToTransmit(File file) {
		this.picTransmitLock.lock();
		this.totransmit.add(file);
		this.convertedFiles++;
		this.picToTransmit.signal();
		this.picTransmitLock.unlock();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainWindow.getInstance().setConvertProgress(
						(int) ((MyJProgressBar.MAX / TrmEngine.this.totalFiles) * TrmEngine.this.convertedFiles));
			};
		});
	}

	protected void setStartNumber(int start) {
		this.picNumLock.lock();
		this.startNum = start - 1;
		this.startNumSetCond.signal();
		this.picNumLock.unlock();
	}

	protected void unSetStartNumber() {
		this.picNumLock.lock();
		this.startNum = -1;
		this.picNumLock.unlock();
	}

	protected int getNextPicNum() {
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
		for (int i = 0; i < this.converters.length; i++) {
			this.converters[i] = new Converter(this, i);
			this.converters[i].setPriority(3);
		}
		try {
			log.info("Starte Converter.");
			for (Converter con : this.converters)
				con.start();
			log.info("Starte Transmitter.");
			this.transmit.start();
			for (Converter con : this.converters)
				if (con.isAlive())
					con.join();
			this.totalFiles = this.convertedFiles;
			this.picTransmitLock.lock();
			this.picToTransmit.signal();
			this.picTransmitLock.unlock();
			this.transmit.join();
			Thread.sleep(1000);
			this.reset();
			TrmEngine.instance = null;
			Thread.sleep(10);
			log.info("Beende Transmitter.");
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
		}
	}

	public synchronized void fileWasIgnored() {
		this.totalFiles = this.totalFiles - 2;
	}

	public void setFiles(File[] files) {
		this.totalFiles = files.length * 2;
		this.toconvert = new Vector<File>();
		for (File fi : files)
			this.toconvert.add(fi);
	}

	public boolean login(String userName, String passWord) {
		this.loggedIn = this.transmit.login(userName, passWord);
		return this.loggedIn;
	}

	public boolean connect() {
		this.transmit = new Transmitter(this);
		this.connected = this.transmit.verCheck();
		return this.connected;
	}

	public synchronized void disconnect() {
		if (null == this.transmit || !this.transmit.isConnected()) {
			return;
		}
		this.transmit.disconnect();
	}

	public synchronized ArrayList<Gallery> getGalleriesFor(String date) {
		if (null == this.transmit || !this.transmit.isConnected()) {
			return new ArrayList<Gallery>();
		}
		return this.transmit.getGalleriesFor(date);
	}

	public synchronized boolean lockLocation(Gallery gal) {
		if (null == this.transmit || !this.transmit.isConnected()) {
			return false;
		}
		this.gallery = gal;
		if (null != this.transmit)
			this.transmit.setGallery(gal);
		return this.transmit.lockLocation(gal);
	}

	public synchronized boolean unLockLocation(Gallery gal) {
		if (null == this.transmit || !this.transmit.isConnected()) {
			return false;
		}
		return this.transmit.unLockLocation(gal);
	}

	public synchronized Gallery newGallery(String location, String date) {
		return this.transmit.newGallery(location, date);
	}

	public boolean isLoggedIn() {
		return this.loggedIn;
	}

	public boolean isConnected() {
		return this.connected;
	}

	public void setGallery(Gallery gallery) {
		this.gallery = gallery;
		this.transmit.setGallery(gallery);
	}
}
