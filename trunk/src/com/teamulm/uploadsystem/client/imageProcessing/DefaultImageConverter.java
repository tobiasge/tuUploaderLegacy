package com.teamulm.uploadsystem.client.imageProcessing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.Helper;

public class DefaultImageConverter implements ImageConverter {

	private static final Logger log = Logger.getLogger(DefaultImageConverter.class);

	private Point smallPicSize;

	private Point bigPicSize;

	private Point bigSLRPicSize;

	public DefaultImageConverter(Point smallPicSize, Point bigPicSize) {
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
			BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
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

	private boolean isDefaultPicture(BufferedImage pic) {
		double aspectRatio = (double) pic.getWidth() / (double) pic.getHeight();
		double diff = Math.abs(aspectRatio - ((double) 4 / (double) 3));
		return diff < (double) 0.05;
	}

	private boolean isUprightPicture(BufferedImage pic) {
		return pic.getHeight() > pic.getWidth();
	}

	public BufferedImage createPic(BufferedImage pic) {
		if (this.isSLRPicture(pic)) {
			log.debug("SLR picture found.");
			BufferedImage target = new BufferedImage(this.bigPicSize.x, this.bigPicSize.y, BufferedImage.TYPE_INT_RGB);
			Graphics graf = target.getGraphics();
			graf.setColor(Color.BLACK);
			graf.drawRect(0, 0, this.bigPicSize.x, this.bigPicSize.y);
			Image temp = pic.getScaledInstance(this.bigSLRPicSize.x, this.bigSLRPicSize.y, BufferedImage.SCALE_SMOOTH);
			int yMove = (int) (((double) bigPicSize.y - (double) bigSLRPicSize.y) / (double) 2);
			graf.drawImage(temp, 0, yMove, null);
			graf.dispose();
			return target;
		} else if (this.isUprightPicture(pic)) {
			log.debug("Upright picture found.");
			BufferedImage target = new BufferedImage(this.bigPicSize.x, this.bigPicSize.y, BufferedImage.TYPE_INT_RGB);
			double downRatio = (double) this.bigPicSize.y / (double) pic.getHeight();
			int newWidth = (int) ((double) pic.getWidth() * downRatio);
			Graphics graf = target.getGraphics();
			graf.setColor(Color.BLACK);
			graf.drawRect(0, 0, this.bigPicSize.x, this.bigPicSize.y);
			Image temp = pic.getScaledInstance(newWidth, this.bigPicSize.y, BufferedImage.SCALE_SMOOTH);
			int xMove = (int) (((double) bigPicSize.x - newWidth) / (double) 2);
			graf.drawImage(temp, xMove, 0, null);
			graf.dispose();
			return target;
		} else if (this.isDefaultPicture(pic)) {
			log.debug("Normal picture found.");
			return this.toBufferedImage(pic.getScaledInstance(this.bigPicSize.x, this.bigPicSize.y,
					BufferedImage.SCALE_SMOOTH));
		} else {
			return null;
		}

	}

	public BufferedImage createPreview(BufferedImage pic) {
		return this.toBufferedImage(pic.getScaledInstance(this.smallPicSize.x, this.smallPicSize.y,
				BufferedImage.SCALE_SMOOTH));
	}
}
