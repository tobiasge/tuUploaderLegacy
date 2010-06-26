/* Converter.java
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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.Helper;
import com.teamulm.uploadsystem.client.TeamUlmUpload;
import com.teamulm.uploadsystem.client.gui.MainWindow;
import com.teamulm.uploadsystem.client.gui.Messages;
import com.teamulm.uploadsystem.client.imageProcessing.ImageConverter;
import com.teamulm.uploadsystem.client.imageProcessing.ImageConverterFactory;

public class Converter extends Thread {

	private static final Logger log = Logger.getLogger(Converter.class);

	private String bigPicName = "pic"; //$NON-NLS-1$

	private Dimension bigPicSize = new Dimension(596, 447);

	private TrmEngine chef;

	private boolean createHqPictures;

	private final String fileSep = System.getProperty("file.separator"); //$NON-NLS-1$

	private String hqPicName = "h_pic"; //$NON-NLS-1$

	private int ident;

	private Dimension maxHqPicSize = new Dimension(1600, 1200);

	private Dimension minHqPicSize = new Dimension(1200, 900);

	private ImageConverter myImageConverter;

	private String savePath;

	private String smallPicName = "s_pic"; //$NON-NLS-1$

	private Dimension smallPicSize = new Dimension(134, 100);

	private boolean stopRequest;

	public Converter(TrmEngine chef, boolean createHqPictures, int ident) {
		super();
		this.setName("Converter " + ident); //$NON-NLS-1$
		this.savePath = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		this.chef = chef;
		this.stopRequest = false;
		this.ident = ident;
		this.createHqPictures = createHqPictures;
		if (createHqPictures) {
			this.myImageConverter = ImageConverterFactory.getConverter(smallPicSize, bigPicSize, minHqPicSize,
				maxHqPicSize);
		} else {
			this.myImageConverter = ImageConverterFactory.getConverter(smallPicSize, bigPicSize);
		}
	}

	@Override
	public void run() {
		TeamUlmUpload
			.getInstance()
			.getMainWindow()
			.addStatusLine(
				MessageFormat.format(Messages.getString("Converter.logMessages.startingConvert"), this.ident)); //$NON-NLS-1$
		File outHqPicName = null;
		File outBigPicName = null;
		File outSmaPicName = null;
		File actFile = null;
		int number = 0;
		try {
			while (!this.stopRequest && ((actFile = this.chef.getNextToConvert()) != null)) {
				if ((actFile = this.myImageConverter.correctPictureOrientation(actFile)) == null) {
					TeamUlmUpload.getInstance().getMainWindow()
						.addStatusLine(Messages.getString("Converter.logMessages.picRotError")); //$NON-NLS-1$
					this.chef.fileWasIgnored();
					continue;
				}

				BufferedImage actPic = ImageIO.read(actFile);
				if (!this.myImageConverter.isPicBigEnough(actPic)) {
					log.info("Thread: " + this.ident + " Übersprungen wegen Größe " + actFile.getName() + " -> Breit: " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ actPic.getWidth() + " Hoch: " + actPic.getHeight()); //$NON-NLS-1$
					TeamUlmUpload.getInstance().getMainWindow()
						.addStatusLine(Messages.getString("Converter.logMessages.picTooSmall")); //$NON-NLS-1$
					this.chef.fileWasIgnored();
					continue;
				}
				if (!this.myImageConverter.isKownFormat(actPic.getWidth(), actPic.getHeight())) {
					TeamUlmUpload.getInstance().getMainWindow()
						.addStatusLine(Messages.getString("Converter.logMessages.wrongFormat")); //$NON-NLS-1$
					this.chef.fileWasIgnored();
					continue;
				}

				number = this.chef.getNextPicNum();
				outHqPicName = new File(this.savePath + this.fileSep + this.hqPicName + number + ".jpg"); //$NON-NLS-1$
				outHqPicName.deleteOnExit();
				outBigPicName = new File(this.savePath + this.fileSep + this.bigPicName + number + ".jpg"); //$NON-NLS-1$
				outBigPicName.deleteOnExit();
				outSmaPicName = new File(this.savePath + this.fileSep + this.smallPicName + number + ".jpg"); //$NON-NLS-1$
				outSmaPicName.deleteOnExit();

				if (this.createHqPictures) {
					if (this.myImageConverter.createHqPic(actFile, outHqPicName)
						&& this.myImageConverter.createPic(outHqPicName, outBigPicName)
						&& this.myImageConverter.createPreview(outBigPicName, outSmaPicName)) {
						this.chef.setToTransmit(outHqPicName);
						this.chef.setToTransmit(outSmaPicName);
						this.chef.setToTransmit(outBigPicName);
					}
				} else {
					if (this.myImageConverter.createPic(actFile, outBigPicName)
						&& this.myImageConverter.createPreview(outBigPicName, outSmaPicName)) {
						this.chef.setToTransmit(outSmaPicName);
						this.chef.setToTransmit(outBigPicName);
					}
				}
			}
			sleep(5);
		} catch (Exception e) {
			log.error("", e); //$NON-NLS-1$
			TeamUlmUpload
				.getInstance()
				.getMainWindow()
				.addStatusLine(
					MessageFormat.format(Messages.getString("Converter.logMessages.notConverted"), actFile.getName(), //$NON-NLS-1$
						this.ident));
			Helper.getInstance().systemCrashHandler(e);
		}
		TeamUlmUpload.getInstance().getMainWindow().setConvertProgress(MainWindow.PROGRESS_BAR_MAX);
	}

	protected synchronized void requestStop() {
		this.stopRequest = true;
	}
}
