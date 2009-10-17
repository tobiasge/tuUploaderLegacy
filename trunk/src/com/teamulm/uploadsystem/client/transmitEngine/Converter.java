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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
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

	private final String fileSep = System.getProperty("file.separator"); //$NON-NLS-1$

	private int ident;

	private ImageConverter myImageConverter;

	private String savePath;

	private String smallPicName = "s_pic"; //$NON-NLS-1$

	private Dimension smallPicSize = new Dimension(134, 100);

	private boolean stopRequest;

	public Converter(TrmEngine chef, int ident) {
		super();
		this.setName("Converter " + ident); //$NON-NLS-1$
		this.savePath = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		this.chef = chef;
		this.stopRequest = false;
		this.ident = ident;
		this.myImageConverter = ImageConverterFactory.getConverter(smallPicSize, bigPicSize);
	}

	@Override
	public void run() {
		TeamUlmUpload.getInstance().getMainWindow().addStatusLine(
			MessageFormat.format(Messages.getString("Converter.logMessages.startingConvert"), this.ident)); //$NON-NLS-1$
		File outBigPicName = null;
		File outSmaPicName = null;
		File actFile = null;
		int number = 0;
		try {
			while (!this.stopRequest && ((actFile = this.chef.getNextToConvert()) != null)) {
				BufferedImage actPic = ImageIO.read(actFile);
				if ((actPic.getWidth() < this.bigPicSize.width) && (actPic.getHeight() < this.bigPicSize.height)) {
					log.info("Thread: " + this.ident + " Übersprungen wegen Größe " + actFile.getName() + " -> Breit: " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ actPic.getWidth() + " Hoch: " + actPic.getHeight()); //$NON-NLS-1$
					TeamUlmUpload.getInstance().getMainWindow().addStatusLine(
						Messages.getString("Converter.logMessages.picTooSmall")); //$NON-NLS-1$
				} else if ((actPic.getWidth() == this.bigPicSize.width)
					&& (actPic.getHeight() == this.bigPicSize.height)) {
					number = this.chef.getNextPicNum();
					outBigPicName = new File(this.savePath + this.fileSep + this.bigPicName + number + ".jpg"); //$NON-NLS-1$
					outSmaPicName = new File(this.savePath + this.fileSep + this.smallPicName + number + ".jpg"); //$NON-NLS-1$
					if (this.copyFile(actFile, outBigPicName)
						&& this.myImageConverter.createPreview(outBigPicName, outSmaPicName)) {

						this.chef.setToTransmit(outSmaPicName);
						this.chef.setToTransmit(outBigPicName);

					}
				} else {
					if (!this.myImageConverter.isKownFormat(actPic.getWidth(), actPic.getHeight())) {
						TeamUlmUpload.getInstance().getMainWindow().addStatusLine(
							Messages.getString("Converter.logMessages.wrongFormat")); //$NON-NLS-1$
						this.chef.fileWasIgnored();
						continue;
					}
					number = this.chef.getNextPicNum();
					outBigPicName = new File(this.savePath + this.fileSep + this.bigPicName + number + ".jpg"); //$NON-NLS-1$
					outSmaPicName = new File(this.savePath + this.fileSep + this.smallPicName + number + ".jpg"); //$NON-NLS-1$
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
			TeamUlmUpload.getInstance().getMainWindow().addStatusLine(
				MessageFormat.format(Messages.getString("Converter.logMessages.notConverted"), actFile.getName(), //$NON-NLS-1$
					this.ident));
			Helper.getInstance().systemCrashHandler(e);
		}
		TeamUlmUpload.getInstance().getMainWindow().setConvertProgress(MainWindow.PROGRESS_BAR_MAX);
	}

	private boolean copyFile(File in, File out) {
		try {
			FileChannel sourceChannel = new FileInputStream(in).getChannel();
			FileChannel destinationChannel = new FileOutputStream(out).getChannel();
			sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
			sourceChannel.close();
			destinationChannel.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	protected synchronized void requestStop() {
		this.stopRequest = true;
	}
}
