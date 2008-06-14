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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.Helper;
import com.teamulm.uploadsystem.client.gui.MainWindow;

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
		this.myImageConverter = new ImageConverter(smallPicSize, bigPicSize);
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
		MainWindow.getInstance().addStatusLine(
				"Beginne Konvertierung: " + this.ident);
		BufferedImage outPic = null;
		File outPicName = null;
		File actFile = null;
		int number = 0;
		try {
			while (!this.stopRequest
					&& ((actFile = this.chef.getNextToConvert()) != null)) {
				BufferedImage actPic = ImageIO.read(actFile);
				if ((actPic.getWidth() < this.bigPicSize.x)
						&& (actPic.getHeight() < this.bigPicSize.y)) {
					log.info("Thread: " + this.ident
							+ " Übersprungen wegen Größe " + actFile.getName()
							+ " -> Breit: " + actPic.getWidth() + " Hoch: "
							+ actPic.getHeight());
					MainWindow.getInstance().addStatusLine(
							"Bild ignoriert: Bild zu klein.");
				} else if ((actPic.getWidth() == this.bigPicSize.x)
						&& (actPic.getHeight() == this.bigPicSize.y)) {
					// Bild hat richtige Größe
					number = this.chef.getNextPicNum();
					outPicName = new File(this.savePath + this.fileSep
							+ this.bigPicName + number + ".jpg");
					this.copyFile(actFile, outPicName);
					this.chef.setToTransmit(outPicName);
					// Großes Bild geschrieben
					// Nur kleines Bild erschaffen
					outPicName = new File(this.savePath + this.fileSep
							+ this.smallPicName + number + ".jpg");
					outPic = this.myImageConverter.createPreview(actPic);
					ImageIO.write(outPic, "jpg", outPicName);
					this.chef.setToTransmit(outPicName);
					// Kleines Bild geschrieben
				} else {
					number = this.chef.getNextPicNum();
					// Großes Bild erschaffen
					outPicName = new File(this.savePath + this.fileSep
							+ this.bigPicName + number + ".jpg");
					outPic = this.myImageConverter.createPic(actPic);
					ImageIO.write(outPic, "jpg", outPicName);
					this.chef.setToTransmit(outPicName);
					// Großes Bild geschrieben
					// Kleines Bild erschaffen
					outPicName = new File(this.savePath + this.fileSep
							+ this.smallPicName + number + ".jpg");
					outPic = this.myImageConverter.createPreview(outPic);
					ImageIO.write(outPic, "jpg", outPicName);
					this.chef.setToTransmit(outPicName);
					// Kleines Bild geschrieben
				}
			}
			sleep(5);
		} catch (Exception e) {
			MainWindow.getInstance().addStatusLine(
					"Thread: " + this.ident + " Konnte " + actFile.getName()
							+ " nicht konvertieren");
			Helper.getInstance().systemCrashHandler(e);
		}
	}
}

class ImageConverter {

	private static final Logger log = Logger.getLogger(ImageConverter.class);

	private Point smallPicSize;

	private Point bigPicSize;

	private Point bigSLRPicSize;

	public ImageConverter(Point smallPicSize, Point bigPicSize) {
		this.smallPicSize = smallPicSize;
		this.bigPicSize = bigPicSize;
		int bigSLRWidth = bigPicSize.x;
		int bigSLRHeight = (int) ((double) 2 / (double) 3 * (double) bigSLRWidth);
		log.debug("bigSLRHeight set to " + bigSLRHeight);
		this.bigSLRPicSize = new Point(bigSLRWidth, bigSLRHeight);

	}

	private BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage)
			return (BufferedImage) image;
		else { // use PixelGrabber to get at the data...
			int w = image.getWidth(null);
			int h = image.getHeight(null);
			if ((w < 0) || (h < 0))
				return null;
			int[] pixels = new int[w * h];
			PixelGrabber pg = new PixelGrabber(image, 0, 0, w, h, pixels, 0, w);
			try {
				pg.grabPixels();
			} catch (InterruptedException e) {
				Helper.getInstance().systemCrashHandler(e);
				return null;
			}
			if ((pg.getStatus() & ImageObserver.ABORT) != 0)
				return null;
			BufferedImage tmp = new BufferedImage(w, h,
					BufferedImage.TYPE_INT_RGB);
			for (int j = 0; j < h; j++) {
				int row = j * w;
				for (int i = 0; i < w; i++)
					tmp.setRGB(i, j, pixels[row + i]);
			}
			return tmp;
		}
	}

	private boolean isSLRPicture(BufferedImage pic) {
		double aspectRatio = (double) pic.getWidth() / (double) pic.getHeight();
		double diff = Math.abs(aspectRatio - ((double) 3 / (double) 2));
		return diff < (double) 0.05;
	}

	private boolean isUprightPicture(BufferedImage pic) {
		return pic.getHeight() > pic.getWidth();
	}

	public BufferedImage createPic(BufferedImage pic) {
		if (this.isSLRPicture(pic)) {
			log.debug("SLR picture found.");
			BufferedImage target = new BufferedImage(this.bigPicSize.x,
					this.bigPicSize.y, BufferedImage.TYPE_INT_RGB);
			Graphics graf = target.getGraphics();
			graf.setColor(Color.BLACK);
			graf.drawRect(0, 0, this.bigPicSize.x, this.bigPicSize.y);
			Image temp = pic.getScaledInstance(this.bigSLRPicSize.x,
					this.bigSLRPicSize.y, BufferedImage.SCALE_SMOOTH);
			int yMove = (int) (((double) bigPicSize.y - (double) bigSLRPicSize.y) / (double) 2);
			graf.drawImage(temp, 0, yMove, null);
			graf.dispose();
			return target;
		} else if (this.isUprightPicture(pic)) {
			log.debug("Upright picture found.");
			BufferedImage target = new BufferedImage(this.bigPicSize.x,
					this.bigPicSize.y, BufferedImage.TYPE_INT_RGB);
			double downRatio = (double) this.bigPicSize.y
					/ (double) pic.getHeight();
			int newWidth = (int) ((double) pic.getWidth() * downRatio);
			Graphics graf = target.getGraphics();
			graf.setColor(Color.BLACK);
			graf.drawRect(0, 0, this.bigPicSize.x, this.bigPicSize.y);
			Image temp = pic.getScaledInstance(newWidth, this.bigPicSize.y,
					BufferedImage.SCALE_SMOOTH);
			int xMove = (int) (((double) bigPicSize.x - newWidth) / (double) 2);
			graf.drawImage(temp, xMove, 0, null);
			graf.dispose();
			return target;
		} else {
			log.debug("Normal picture found.");
			return this.toBufferedImage(pic.getScaledInstance(
					this.bigPicSize.x, this.bigPicSize.y,
					BufferedImage.SCALE_SMOOTH));
		}

	}

	public BufferedImage createPreview(BufferedImage pic) {
		return this.toBufferedImage(pic.getScaledInstance(this.smallPicSize.x,
				this.smallPicSize.y, BufferedImage.SCALE_SMOOTH));
	}
}
