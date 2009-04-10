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

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.Helper;
import com.teamulm.uploadsystem.client.TeamUlmUpload;
import com.teamulm.uploadsystem.client.imageProcessing.ImageConverter;
import com.teamulm.uploadsystem.client.imageProcessing.ImageConverterFactory;

public class Converter extends Thread {

	private static final Logger log = Logger.getLogger(Converter.class);

	private String savePath;

	private TrmEngine chef;

	private boolean stopRequest;

	private int ident;

	private final String fileSep = System.getProperty("file.separator");

	private String bigPicName = "pic";

	private String smallPicName = "s_pic";

	private ImageConverter myImageConverter;

	private Dimension smallPicSize = new Dimension(134, 100);

	private Dimension bigPicSize = new Dimension(596, 447);

	public Converter(TrmEngine chef, int ident) {
		super();
		this.setName("Converter " + ident);
		this.savePath = System.getProperty("java.io.tmpdir");
		this.chef = chef;
		this.stopRequest = false;
		this.ident = ident;
		this.myImageConverter = ImageConverterFactory.getConverter(smallPicSize, bigPicSize);
	}

	public synchronized void requestStop() {
		this.stopRequest = true;
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

	@Override
	public void run() {
		TeamUlmUpload.getInstance().getMainWindow().addStatusLine("Beginne Konvertierung: " + this.ident);
		File outBigPicName = null;
		File outSmaPicName = null;
		File actFile = null;
		int number = 0;
		try {
			while (!this.stopRequest && ((actFile = this.chef.getNextToConvert()) != null)) {
				BufferedImage actPic = ImageIO.read(actFile);
				if ((actPic.getWidth() < this.bigPicSize.width) && (actPic.getHeight() < this.bigPicSize.height)) {
					log.info("Thread: " + this.ident + " Übersprungen wegen Größe " + actFile.getName() + " -> Breit: "
						+ actPic.getWidth() + " Hoch: " + actPic.getHeight());
					TeamUlmUpload.getInstance().getMainWindow().addStatusLine("Bild ignoriert: Bild zu klein.");
				} else if ((actPic.getWidth() == this.bigPicSize.width)
					&& (actPic.getHeight() == this.bigPicSize.height)) {
					number = this.chef.getNextPicNum();
					outBigPicName = new File(this.savePath + this.fileSep + this.bigPicName + number + ".jpg");
					outSmaPicName = new File(this.savePath + this.fileSep + this.smallPicName + number + ".jpg");
					if (this.copyFile(actFile, outBigPicName)
						&& this.myImageConverter.createPreview(outBigPicName, outSmaPicName)) {

						this.chef.setToTransmit(outSmaPicName);
						this.chef.setToTransmit(outBigPicName);

					}
				} else {
					if (!this.myImageConverter.isKownFormat(actPic.getWidth(), actPic.getHeight())) {
						TeamUlmUpload.getInstance().getMainWindow().addStatusLine(
							"Bild ignoriert: Bild hat falsches Format.");
						this.chef.fileWasIgnored();
						continue;
					}
					number = this.chef.getNextPicNum();
					outBigPicName = new File(this.savePath + this.fileSep + this.bigPicName + number + ".jpg");
					outSmaPicName = new File(this.savePath + this.fileSep + this.smallPicName + number + ".jpg");
					if (this.myImageConverter.createPic(actFile, outBigPicName)
						&& this.myImageConverter.createPreview(outBigPicName, outSmaPicName)) {
						this.chef.setToTransmit(outSmaPicName);
						this.chef.setToTransmit(outBigPicName);

					}
				}
			}
			sleep(5);
		} catch (Exception e) {
			TeamUlmUpload.getInstance().getMainWindow().addStatusLine(
				"Thread: " + this.ident + " Konnte " + actFile.getName() + " nicht konvertieren");
			Helper.getInstance().systemCrashHandler(e);
		}
	}
}
