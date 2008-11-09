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

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.Helper;
import com.teamulm.uploadsystem.client.gui.MainWindow;
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

	private Point smallPicSize = new Point(134, 100);

	private Point bigPicSize = new Point(575, 431);

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

	private void copyFile(File in, File out) throws Exception {
		FileChannel sourceChannel = new FileInputStream(in).getChannel();
		FileChannel destinationChannel = new FileOutputStream(out).getChannel();
		sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
		sourceChannel.close();
		destinationChannel.close();
	}

	@Override
	public void run() {
		MainWindow.getInstance().addStatusLine("Beginne Konvertierung: " + this.ident);
		BufferedImage outPic = null;
		File outPicName = null;
		File actFile = null;
		int number = 0;
		try {
			while (!this.stopRequest && ((actFile = this.chef.getNextToConvert()) != null)) {
				BufferedImage actPic = ImageIO.read(actFile);
				if ((actPic.getWidth() < this.bigPicSize.x) && (actPic.getHeight() < this.bigPicSize.y)) {
					log.info("Thread: " + this.ident + " Übersprungen wegen Größe " + actFile.getName() + " -> Breit: "
							+ actPic.getWidth() + " Hoch: " + actPic.getHeight());
					MainWindow.getInstance().addStatusLine("Bild ignoriert: Bild zu klein.");
				} else if ((actPic.getWidth() == this.bigPicSize.x) && (actPic.getHeight() == this.bigPicSize.y)) {
					// Bild hat richtige Größe
					number = this.chef.getNextPicNum();
					outPicName = new File(this.savePath + this.fileSep + this.bigPicName + number + ".jpg");
					this.copyFile(actFile, outPicName);
					this.chef.setToTransmit(outPicName);
					// Großes Bild geschrieben
					// Nur kleines Bild erschaffen
					outPicName = new File(this.savePath + this.fileSep + this.smallPicName + number + ".jpg");
					outPic = this.myImageConverter.createPreview(actPic);
					ImageIO.write(outPic, "jpg", outPicName);
					this.chef.setToTransmit(outPicName);
					// Kleines Bild geschrieben
				} else {
					number = this.chef.getNextPicNum();
					// Großes Bild erschaffen
					outPicName = new File(this.savePath + this.fileSep + this.bigPicName + number + ".jpg");
					outPic = this.myImageConverter.createPic(actPic);
					if (null == outPic) {
						MainWindow.getInstance().addStatusLine("Bild ignoriert: Bild hat falsches Format.");
						continue;
					}
					ImageIO.write(outPic, "jpg", outPicName);
					this.chef.setToTransmit(outPicName);
					// Großes Bild geschrieben
					// Kleines Bild erschaffen
					outPicName = new File(this.savePath + this.fileSep + this.smallPicName + number + ".jpg");
					outPic = this.myImageConverter.createPreview(outPic);
					ImageIO.write(outPic, "jpg", outPicName);
					this.chef.setToTransmit(outPicName);
					// Kleines Bild geschrieben
				}
			}
			sleep(5);
		} catch (Exception e) {
			MainWindow.getInstance().addStatusLine(
					"Thread: " + this.ident + " Konnte " + actFile.getName() + " nicht konvertieren");
			Helper.getInstance().systemCrashHandler(e);
		}
	}
}
