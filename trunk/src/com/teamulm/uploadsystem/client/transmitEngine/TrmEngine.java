package com.teamulm.uploadsystem.client.transmitEngine;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import com.teamulm.uploadsystem.client.Helper;
import com.teamulm.uploadsystem.client.TeamUlmUpload;
import com.teamulm.uploadsystem.client.gui.MainWindow;
import com.teamulm.uploadsystem.data.Gallery;
import com.teamulm.uploadsystem.data.Location;

public class TrmEngine extends Thread {

	public static final String VERSION = "5.2"; //$NON-NLS-1$

	private static TrmEngine instance = null;

	private static final Logger log = Logger.getLogger(TrmEngine.class);

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

	private long convertedFiles = 0;

	private Converter[] converters = null;

	private Gallery gallery = null;

	private Condition picToTransmit = null, startNumSetCond = null;

	private ReentrantLock picTransmitLock = null, picConvertLock = null, picNumLock = null;

	private int startNum = -1;

	private boolean stopRequested = false, loggedIn = false, connected = false;

	private Vector<File> toconvert = null;

	private long totalFiles = 0;

	private Vector<File> totransmit = null;

	private Transmitter transmit = null;

	private long transmitedFiles = 0;

	private String userName = "NotLoggedIn";

	private TrmEngine() {
		super();
		this.setName("TrmEngine"); //$NON-NLS-1$
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
		this.requestStop();
		this.reset();
	}

	public synchronized void fileWasIgnored() {
		this.totalFiles = this.totalFiles - 2;
	}

	public synchronized ArrayList<Gallery> getGalleriesFor(String date) {
		if (null == this.transmit || !this.transmit.isConnected()) {
			return new ArrayList<Gallery>();
		}
		return this.transmit.getGalleriesFor(date);
	}

	public Gallery getGallery() {
		return gallery;
	}

	public synchronized List<Location> getLocations() {
		if (null == this.transmit || !this.transmit.isConnected()) {
			return new ArrayList<Location>();
		}
		return this.transmit.getLocations();
	}

	public File getNextToConvert() {
		File retVal = null;
		this.picConvertLock.lock();
		if (!this.toconvert.isEmpty())
			retVal = this.toconvert.remove(0);
		this.picConvertLock.unlock();
		return retVal;
	}

	public String getUserName() {
		return userName;
	}

	public boolean isConnected() {
		return this.connected;
	}

	public boolean isLoggedIn() {
		return this.loggedIn;
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

	public boolean login(String userName, String passWord) {
		this.loggedIn = this.transmit.login(userName, passWord);
		if (this.loggedIn) {
			this.userName = userName;
		}
		return this.loggedIn;
	}

	public synchronized Gallery newGallery(String location, LocalDate date) {
		return this.transmit.newGallery(location, date);
	}

	public void requestStop() {
		this.stopRequested = true;
		if (null != this.transmit) {
			this.transmit.requestStop();
		}
		if (null != this.converters) {
			for (Converter con : this.converters) {
				if (null != con) {
					con.requestStop();
				}
			}
		}
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
			log.info("Starte Converter."); //$NON-NLS-1$
			for (Converter con : this.converters)
				con.start();
			log.info("Starte Transmitter."); //$NON-NLS-1$
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
			log.info("Beende Transmitter."); //$NON-NLS-1$
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
		}
	}

	public void setFiles(File[] files) {
		this.totalFiles = files.length * 2;
		this.toconvert = new Vector<File>();
		for (File fi : files)
			this.toconvert.add(fi);
	}

	public void setGallery(Gallery gallery) {
		this.gallery = gallery;
		this.transmit.setGallery(gallery);
	}

	public synchronized boolean unLockLocation(Gallery gal) {
		if (null == this.transmit || !this.transmit.isConnected()) {
			return false;
		}
		return this.transmit.unLockLocation(gal);
	}

	private synchronized void reset() {
		int length = this.converters.length;
		for (int i = 0; i < length; i++)
			this.converters[i] = null;
		this.transmit = null;
		TrmEngine.instance = null;
	}

	protected int getNextPicNum() {
		this.picNumLock.lock();
		this.startNum++;
		int retVal = this.startNum;
		this.picNumLock.unlock();
		return retVal;
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
		TeamUlmUpload
			.getInstance()
			.getMainWindow()
			.setUploadProgress(
				(int) ((MainWindow.PROGRESS_BAR_MAX / TrmEngine.this.totalFiles) * TrmEngine.this.transmitedFiles));
		return retVal;
	}

	protected synchronized boolean isThereSomethingToTtansmit() {
		return this.totalFiles > this.transmitedFiles;
	}

	protected void setStartNumber(int start) {
		this.picNumLock.lock();
		this.startNum = start - 1;
		this.startNumSetCond.signal();
		this.picNumLock.unlock();
	}

	protected void setToTransmit(File file) {
		this.picTransmitLock.lock();
		this.totransmit.add(file);
		this.convertedFiles++;
		this.picToTransmit.signal();
		this.picTransmitLock.unlock();
		TeamUlmUpload
			.getInstance()
			.getMainWindow()
			.setConvertProgress(
				(int) ((MainWindow.PROGRESS_BAR_MAX / TrmEngine.this.totalFiles) * TrmEngine.this.convertedFiles));
	}

	protected void unSetStartNumber() {
		this.picNumLock.lock();
		this.startNum = -1;
		this.picNumLock.unlock();
	}
}
